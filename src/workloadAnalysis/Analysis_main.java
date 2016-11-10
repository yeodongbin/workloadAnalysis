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
			oltpWorkload();// have to evaluate!
			break;
		case 3:
			intervalAnalysis();// have to evaluate!
			break;
		default:
			System.out.println("�ش� ������ �����ϴ�.");
			break;
		}

		
		System.out.println("** Workload Analysis End **");
	}

	public static void intervalAnalysis() throws IOException {
		int req = 0;
		int writeReq = 0;
		int writeReq1Page = 0;
		
		int numOfItem = 0;
		int sumOfAvgInterval = 0;
		
		long lPN = 0; // logical page number from the workload
		int pageSize = 0; // physical page size from the workload
		long tempLong = -1;

		// LPN, Write-Count , Read-Count
		TreeMap<Long, Integer> lpnLastTime = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> write1PageCount = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> sumIntervalNum = new TreeMap<Long, Integer>();
		TreeMap<Long, Float> avgIntervalNum = new TreeMap<Long, Float>();
		
		// I/O trace file format (FILE_INPUT)
		// .csv:Timestamp,Hostname,DiskNumber,Type,Offset,Size,ResponseTime
		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_INPUT))) {
			for (String line; (line = br.readLine()) != null;) {
				req++;
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

				if (elements[3].equals("Write")) {
					writeReq++;

					// write 1page lpn ������
					if (pageSize == 1) {
						writeReq1Page++;

						// interval �ѷ�
						if (false == sumIntervalNum.containsKey(lPN)) {
							sumIntervalNum.put(lPN, 0);
						} else {
							sumIntervalNum.put(lPN, sumIntervalNum.get(lPN)
									+ (writeReq1Page - lpnLastTime.get(lPN)));
						}

						// �ֱ� 1page write ���� �ð�
						lpnLastTime.put(lPN, writeReq1Page);

						// LPN�� 1page write Ƚ��
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

		//Average of Interval each LPN 
		for (Long key : write1PageCount.keySet()) {
			if (1 != write1PageCount.get(key)) {
				avgIntervalNum.put(key,
						(float) (sumIntervalNum.get(key) / (write1PageCount
								.get(key) - 1)));
				numOfItem++;
			}
		}

		//Total average of Interval 
		for (Long key : avgIntervalNum.keySet()) {
			sumOfAvgInterval += avgIntervalNum.get(key);
		}

		MapToTxt(lpnLastTime, "lpnLastTime.txt");
		MapToTxt(write1PageCount, "write1PageCount.txt");
		MapToTxt(sumIntervalNum, "sumIntervalNum.txt");
		floatMapToTxt(avgIntervalNum, Config.FILE_AVG_INTERVAL);
		
		System.out.println(">> Request Count : " + req);
		System.out.println("> Write Request           : " + writeReq);
		System.out.println("> Write Request (Sequtial): "
				+ (writeReq - writeReq1Page));
		System.out.println("> Write Request (Random)  : " + writeReq1Page);
		// System.out.println("> Read Request :" + readReq);
		System.out.println("\n> Sum of Avg Interval     : " + sumOfAvgInterval);
		System.out.println("> Number of Avg Interval  : " + numOfItem);
		System.out.println(">> Total Average of Interval : "
				+ (sumOfAvgInterval / numOfItem));
		System.out.println("");
	}

	
	public static void oltpWorkload() throws IOException {
		int writeReq = 0;
		int writeReq1Page = 0;
		int readReq = 0;

		int size = 0;
		int req = 0;
		long startLPN = 0;
		long endLPN = 0;
		long tempLong = 0;
		long tempLong2 = 0;

		TreeMap<Long, Integer> lpnLastTime = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> write1PageCount = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> sumIntervalNum = new TreeMap<Long, Integer>();

		FileWriter fw = new FileWriter(new File(Config.FILE_OUTPUT));
		BufferedWriter bw = new BufferedWriter(fw);

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_OLTP_INPUT))) {
			for (String line; (line = br.readLine()) != null;) {
				String[] elements = line.split(",");
				req++;
				tempLong = Long.valueOf(elements[1]).longValue();
				tempLong2 = Long.valueOf(elements[2]).longValue();

				startLPN = Long.valueOf(
						((tempLong * Config.LBA_SIZE) / Config.PAGE_BYPT_SIZE))
						.longValue();

				endLPN = Long
						.valueOf(
								(((tempLong * Config.LBA_SIZE) + tempLong2) / Config.PAGE_BYPT_SIZE))
						.longValue();

				size = (int) ((endLPN - startLPN) + 1);

				if (1048576 < endLPN) {
				}

				// Write Read �м�
				if (elements[3].equals("w")) {
					bw.write("Write" + "," + startLPN + "," + size);
					bw.newLine();
					writeReq++;

					// average Interval Calculation
					if (size == 1) {
						writeReq1Page++;

						// interval �ѷ�
						if (false == sumIntervalNum.containsKey(startLPN)) {
							sumIntervalNum.put(startLPN, 0);
						} else {
							sumIntervalNum.put(
									startLPN,
									sumIntervalNum.get(startLPN)
											+ (writeReq1Page - lpnLastTime
													.get(startLPN)));
						}

						// �ֱ� 1page write ���� �ð�
						lpnLastTime.put(startLPN, writeReq1Page);

						// LPN�� 1page write Ƚ��
						if (false == write1PageCount.containsKey(startLPN)) {
							write1PageCount.put(startLPN, 1);
						} else {
							write1PageCount.put(startLPN,
									(int) write1PageCount.get(startLPN) + 1);
						}
					}

				} else if (elements[3].equals("r")) {
					bw.write("Read" + "," + startLPN + "," + size);
					bw.newLine(); // �ٹٲ�
					readReq++;
					// System.out.println("Read" + "," + startLPN + "," + size);
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while reading spc file: " + e);
		}

		bw.close();
		fw.close();

		TreeMap<Long, Float> avgIntervalNum = new TreeMap<Long, Float>();

		int numOfItem = 0;
		int sumOfAvgInterval = 0;

		for (Long key : write1PageCount.keySet()) {
			if (1 != write1PageCount.get(key)) {
				avgIntervalNum.put(key,
						(float) (sumIntervalNum.get(key) / (write1PageCount
								.get(key) - 1)));
				numOfItem++;
			}
		}

		for (Long key : avgIntervalNum.keySet()) {
			sumOfAvgInterval += avgIntervalNum.get(key);
		}

		MapToTxt(lpnLastTime, "lpnLastTime.txt");
		MapToTxt(write1PageCount, "write1PageCount.txt");
		MapToTxt(sumIntervalNum, "sumIntervalNum.txt");
		floatMapToTxt(avgIntervalNum, Config.FILE_AVG_INTERVAL);

		System.out.println(">> Request Count: " + req);
		System.out.println("> Write Request : " + writeReq);
		System.out.println("> Write Request (Sequtial): "
				+ (writeReq - writeReq1Page));
		System.out.println("> Write Request (Random)  : " + writeReq1Page);
		// System.out.println("> Read Request :" + readReq);
		System.out.println("\n> Sum of Avg Interval    : " + sumOfAvgInterval);
		System.out.println("> Number of Avg Interval : " + numOfItem);
		System.out.println(">> Total Average of Interval : "
				+ (sumOfAvgInterval / numOfItem));
		System.out.println("");
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
					// write page ������ �м�(pageSize)
					if (false == eachPages.containsKey(pageSize)) {
						eachPages.put(pageSize, 1);
					} else {
						eachPages.put(pageSize, eachPages.get(pageSize) + 1);
					}

					// write 1page lpn ������
					if (pageSize == 1) {
						write_1PageCount++;
						if (false == lpnWritePage.containsKey(lPN)) {
							lpnWritePage.put(lPN, 1);
						} else {
							lpnWritePage.put(lPN, lpnWritePage.get(lPN) + 1);
						}
					} else {
						// in case of multi-pages�� ��� ������ ���� ȸ��
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

		bw.newLine(); // �ٹٲ�
		bw.write("Total Request Count        = " + ReqCount);
		bw.newLine(); // �ٹٲ�
		bw.write("Write Request Count        = " + writeReqCount);
		bw.newLine(); // �ٹٲ�
		bw.write("Read Request Count         = " + readReqCount);
		bw.write("\n-----------------------------------------\n");
		bw.write("Total Write Pages Count    = " + totalWritePages);
		bw.newLine(); // �ٹٲ�
		bw.write("Total Read Pages Count     = " + totalReadPages);
		bw.write("\n-----------------------------------------\n");
		bw.write("Write 1-Page Request Count = " + write_1PageCount);
		bw.newLine(); // �ٹٲ�
		bw.write("Read 1-Page Request Count  = " + read_1PageCount);
		bw.write("\n-----------------------------------------\n");
		bw.write("LPN WRITE LIST SIZE        = " + lpnWriteConut.size());
		bw.newLine(); // �ٹٲ�
		bw.write("LPN READ LIST SIZE         = " + lpnReadConut.size());
		bw.newLine(); // �ٹٲ�
		bw.write("LPN WRITE LIST SIZE(1page) = " + lpnWritePage.size());
		bw.newLine(); // �ٹٲ�
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