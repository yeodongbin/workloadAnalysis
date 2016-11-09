package workloadAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Analysis_main {

	public static void main(String args[]) throws Exception {

		System.out.println(" ** Workload Analysis Start ** \n");

		switch (Config.WORK_LOAD) {
		case 1:
			msrWorkload();
			break;
		case 2:
			oltpWorkload();//have to evaluate!
			break;
		case 3:
			intervalAnalysis();//have to evaluate!
			break;
		default:
			System.out.println("해당 파일이 없습니다.");
			break;
		}

		System.out.println("** Workload Analysis End **");
	}
	
	
	public static void intervalAnalysis() throws IOException {
		
		int writeReq1Page = 0;
		int lastReq1Page = 0;

		long lPN = 0; // logical page number from the workload
		int pageSize = 0; // physical page size from the workload
		
		long tempLong = -1;

		// LPN, Write-Count , Read-Count
		TreeMap<Long, Integer> lpnLastTime = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> write1PageCount = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> sumIntervalNum = new TreeMap<Long, Integer>();
		

		// I/O trace file format (FILE_INPUT)
		// .csv:Timestamp,Hostname,DiskNumber,Type,Offset,Size,ResponseTime

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_INPUT))) {
			for (String line; (line = br.readLine()) != null;) {
				
				String[] elements = line.split(",");

				tempLong = Long.valueOf(elements[4]).longValue();
				lPN = (tempLong / Config.PAGE_BYPT_SIZE);

				tempLong = Long.valueOf(elements[5]).longValue();
				pageSize = (int) (tempLong / Config.PAGE_BYPT_SIZE);

				if (0 == pageSize) {
					pageSize = 1;
				} else if (0 < (tempLong % Config.PAGE_BYPT_SIZE)) {
					pageSize = pageSize + 1;
				}
				
				reqCount++;
				
				if (elements[3].equals("Write")) {
					writeReqCount++;
					
					// write 1page lpn 모으기
					if (pageSize == 1) {

						writeReq1Page++;
						// interval 총량
						if (false == sumIntervalNum.containsKey(lPN)) {
							sumIntervalNum.put(lPN, 0);
						} else {
							sumIntervalNum.put(lPN,
									writeReq1Page - lpnLastTime.get(lPN));
						}

						// 최근 1page write 접근 시간
						lpnLastTime.put(lPN, writeReq1Page);

						// LPN별 1page write 횟수
						if (false == write1PageCount.containsKey(lPN)) {
							write1PageCount.put(lPN, 1);
						} else {
							write1PageCount.put(lPN,
									(int) write1PageCount.get(lPN) + 1);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while reading csv file: " + e);
		}

		TreeMap<Long, Float> avgIntervalNum = new TreeMap<Long, Float>();
		for (int i = 0; i < avgIntervalNum.size(); i++) {
			avgIntervalNum
					.put(lPN,
							(float) (sumIntervalNum.get(lPN) / write1PageCount
									.get(lPN)));
		}

		MapToTxt(write1PageCount, Config.FILE_LPN_WRITE_OUTPUT_1PAGE);
		floatMapToTxt(avgIntervalNum, Config.FILE_LPN_WRITE_OUTPUT_1PAGE);
	}
	
	
	public static void oltpWorkload() throws IOException {

		int size = 0;
		long startLPN = 0;
		long endLPN = 0;
		long tempLong = 0;
		long tempLong2 = 0;

		FileWriter fw = new FileWriter(new File(Config.FILE_OUTPUT));
		BufferedWriter bw = new BufferedWriter(fw);

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_INPUT))) {

			for (String line; (line = br.readLine()) != null;) {

				String[] elements = line.split(",");

				tempLong = Long.valueOf(elements[1]).longValue();
				tempLong2 = Long.valueOf(elements[2]).longValue();

				startLPN = Long.valueOf(
						((tempLong * Config.LBA_SIZE) / Config.PAGE_BYPT_SIZE))
						.longValue();

				endLPN = Long.valueOf(
						((tempLong * Config.LBA_SIZE) / Config.PAGE_BYPT_SIZE)
								+ tempLong2).longValue();

				size = (int) ((endLPN - startLPN) + 1);

				if ("w" == elements[3]) {
					bw.write("Write" + "," + startLPN + "," + size);
					bw.newLine(); // 줄바꿈
				} else if ("r" == elements[3]) {
					bw.write("Read" + "," + startLPN + "," + size);
					bw.newLine(); // 줄바꿈
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while reading spc file: " + e);
		}

		bw.close();
		fw.close();
	}
	
	
	public static void msrWorkload() throws IOException {

		int ReqCount = 0; // Total Request Count (Read +Write)
		int writeReqCount = 0; // Write Request Count
		int readReqCount = 0; // Read Request Count

		int totalWritePages = 0;
		int totalReadPages = 0;
		int read_1PageCount = 0;
		int write_1PageCount = 0;

		long lPN = 0; // logical page number from the workload
		int pageSize = 0; // physical page size from the workload
		// int pageOffset = 0;

		long tempLong = -1;

		// LPN, Write-Count , Read-Count
		TreeMap<Long, Integer> lpnWriteConut = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> lpnReadConut = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> lpnWritePage = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> lpnReadPage = new TreeMap<Long, Integer>();
		TreeMap<Integer, Integer> eachPages = new TreeMap<Integer, Integer>();

		// I/O trace file format (FILE_INPUT)
		// .csv:Timestamp,Hostname,DiskNumber,Type,Offset,Size,ResponseTime

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_INPUT))) {

			for (String line; (line = br.readLine()) != null;) {
				String[] elements = line.split(",");

				tempLong = Long.valueOf(elements[4]).longValue();
				lPN = (tempLong / Config.PAGE_BYPT_SIZE);
				// pageOffset = (int) (tempLong % Config.PAGE_BYPT_SIZE);

				tempLong = Long.valueOf(elements[5]).longValue();
				pageSize = (int) (tempLong / Config.PAGE_BYPT_SIZE);

				if (0 == pageSize) {
					pageSize = 1;
				} else if (0 < (tempLong % Config.PAGE_BYPT_SIZE)) {
					pageSize = pageSize + 1;
				}

				ReqCount++;
				if (elements[3].equals("Write")) {
					writeReqCount++;
					// write page 갯수별 분석(pageSize)
					if (false == eachPages.containsKey(pageSize)) {
						eachPages.put(pageSize, 1);
					} else {
						eachPages.put(pageSize, eachPages.get(pageSize) + 1);
					}

					// write 1page lpn 모으기
					if (pageSize == 1) {
						write_1PageCount++;
						if (false == lpnWritePage.containsKey(lPN)) {
							lpnWritePage.put(lPN, 1);
						} else {
							lpnWritePage.put(lPN, lpnWritePage.get(lPN) + 1);
						}
					} else {
						// in case of multi-pages의 경우 페이지 접근 회수
						for (int i = 0; i < pageSize; i++) {
							totalWritePages++;

							if (false == lpnWriteConut.containsKey(lPN + i)) {
								lpnWriteConut.put(lPN + i, 1);
							} else {
								lpnWriteConut.put(lPN + i,
										lpnWriteConut.get(lPN + i) + 1);
							}
						}
					}
				} else if (elements[3].equals("Read")) {
					readReqCount++;

					if (pageSize == 1) {
						read_1PageCount++;
						/*
						 * if (false == lpnWritePage.containsKey(lPN)) {
						 * lpnReadPage.put(lPN, 1); } else {
						 * lpnReadPage.put(lPN, lpnReadPage.get(lPN) + 1); }
						 */
					} else {
						for (int i = 0; i < pageSize; i++) {
							totalReadPages++;
							/*
							 * if (false == lpnWriteConut.containsKey(lPN + i))
							 * { lpnReadConut.put(lPN + i, 1); } else {
							 * lpnReadConut.put(lPN + i, lpnWriteConut.get(lPN +
							 * i) + 1); }
							 */
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while reading csv file: " + e);
		}

		MapToTxt(lpnWriteConut, Config.FILE_LPN_WRITE_OUTPUT);
		MapToTxt(lpnReadConut, Config.FILE_LPN_READ_OUTPUT);
		MapToTxt(lpnWritePage, Config.FILE_LPN_WRITE_OUTPUT_1PAGE);
		MapToTxt(lpnReadPage, Config.FILE_LPN_READ_OUTPUT_1PAGE);
		intMapToTxt(eachPages, Config.FILE_EACH_WRITE_PAGES);

		FileWriter fw = new FileWriter(new File(Config.FILE_LOG_OUTPUT));
		BufferedWriter bw = new BufferedWriter(fw);

		bw.newLine(); // 줄바꿈
		bw.write("Total Request Count        = " + ReqCount);
		bw.newLine(); // 줄바꿈
		bw.write("Write Request Count        = " + writeReqCount);
		bw.newLine(); // 줄바꿈
		bw.write("Read Request Count         = " + readReqCount);
		bw.write("\n-----------------------------------------\n");
		bw.write("Total Write Pages Count    = " + totalWritePages);
		bw.newLine(); // 줄바꿈
		bw.write("Total Read Pages Count     = " + totalReadPages);
		bw.write("\n-----------------------------------------\n");
		bw.write("Write 1-Page Request Count = " + write_1PageCount);
		bw.newLine(); // 줄바꿈
		bw.write("Read 1-Page Request Count  = " + read_1PageCount);
		bw.write("\n-----------------------------------------\n");
		bw.write("LPN WRITE LIST SIZE        = " + lpnWriteConut.size());
		bw.newLine(); // 줄바꿈
		bw.write("LPN READ LIST SIZE         = " + lpnReadConut.size());
		bw.newLine(); // 줄바꿈
		bw.write("LPN WRITE LIST SIZE(1page) = " + lpnWritePage.size());
		bw.newLine(); // 줄바꿈
		bw.write("LPN READ LIST SIZE(1page)  = " + lpnReadPage.size());
		bw.close();
		fw.close();
	}

	public static void MapToTxt(int[][] map, String output) throws IOException {
		// create your file writer and buffered reader
		FileWriter fw = new FileWriter(new File(output));
		BufferedWriter bw = new BufferedWriter(fw);

		for (int i = 0; i < map.length; i++) {
			bw.write(i + "," + map[i][0] + "," + map[i][1] + "\n");
		}

		bw.close();
		fw.close();
		return;
	}

	public static void intMapToTxt(Map<Integer, Integer> map, String output)
			throws IOException {

		// create your file writer and buffered reader
		FileWriter fstream = new FileWriter(new File(output));
		BufferedWriter bw = new BufferedWriter(fstream);

		// create your iterator for your map
		Iterator<Entry<Integer, Integer>> it = map.entrySet().iterator();

		while (it.hasNext()) {
			// the key/value pair is stored here in pairs
			Entry<Integer, Integer> pairs = it.next();
			// since you only want the value, we only care about
			// pairs.getValue(), which is written to out
			bw.write(pairs.getKey() + "	" + pairs.getValue() + "\n");
		}

		// lastly, close the file and end
		bw.close();
		return;
	}

	public static void floatMapToTxt(Map<Long, Float> map, String output)
			throws IOException {

		// create your file writer and buffered reader
		FileWriter fstream = new FileWriter(new File(output));
		BufferedWriter bw = new BufferedWriter(fstream);

		// create your iterator for your map
		Iterator<Entry<Long, Float>> it = map.entrySet().iterator();

		while (it.hasNext()) {
			// the key/value pair is stored here in pairs
			Entry<Long, Float> pairs = it.next();
			// since you only want the value, we only care about
			// pairs.getValue(), which is written to out
			bw.write(pairs.getKey() + "	" + pairs.getValue() + "\n");
		}

		// lastly, close the file and end
		bw.close();
		return;
	}

	public static void MapToTxt(Map<Long, Integer> map, String output)
			throws IOException {

		// create your file writer and buffered reader
		FileWriter fstream = new FileWriter(new File(output));
		BufferedWriter bw = new BufferedWriter(fstream);

		// create your iterator for your map
		Iterator<Entry<Long, Integer>> it = map.entrySet().iterator();

		while (it.hasNext()) {
			// the key/value pair is stored here in pairs
			Map.Entry<Long, Integer> pairs = it.next();
			// since you only want the value, we only care about
			// pairs.getValue(), which is written to out
			bw.write(pairs.getKey() + "	" + pairs.getValue() + "\n");
		}

		// lastly, close the file and end
		bw.close();
		return;
	}
}