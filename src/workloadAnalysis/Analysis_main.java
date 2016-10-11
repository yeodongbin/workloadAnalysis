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

		int totalWritePagesCount = 0;
		int totalReadPagesCount = 0;

		int totalCount = 0; // Total Request Count (Read +Write)
		int writeCount = 0; // Write Request Count
		int readCount = 0; // Read Request Count
		int read_1pageCount = 0;
		int write_1pageCount = 0;
		int total_1pageCount = 0;
		 
		
		long tempLong = -1;
		long tempAddr = -1;
		long tempFirst = 0;
		int temp = 0;
		long page_number = 0; // physical page number from the workload
		int page_offset = 0;
		int page_size = 0; // physical page size from the workload

		// LPN, Write-Count , Read-Count
		TreeMap<Long, Integer> lpnWriteConut = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> lpnReadConut = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> lpnWriteConut1page = new TreeMap<Long, Integer>();
		TreeMap<Long, Integer> lpnReadConut1page = new TreeMap<Long, Integer>();

		// I/O trace file format (FILE_INPUT)
		// .csv:Timestamp,Hostname,DiskNumber,Type,Offset,Size,ResponseTime

		System.out.println("** Workload Analysis Start **\n");

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FINE_INPUT))) {

			for (String line; (line = br.readLine()) != null;) {

				String[] elements = line.split(",");
				totalCount++;

				tempLong = Long.valueOf(elements[4]).longValue();
				page_number = (int) (tempLong / Config.PAGE_BYPT_SIZE);
				page_offset = (int) (tempLong % Config.PAGE_BYPT_SIZE);

				tempLong = Long.valueOf(elements[5]).longValue();
				page_size = (int) (tempLong / Config.PAGE_BYPT_SIZE);

				if (page_size == 0) {
					page_size = 1;
				}

				System.out.println("\nplease wait....\n");

				// in case of 1page
				if (page_size == 1) {
					total_1pageCount++;

					/*
					 * // ∆Ø¡§ Address µø¿€ Ω√∞£ √ﬂ√‚ if ((total_1pageCount == 1)) {
					 * tempFirst = Long.valueOf(elements[0]).longValue();
					 * System.out.println(tempFirst+ " 00"); }
					 */

					if (elements[3].equals("Write")) {
						write_1pageCount++;

						if (false == lpnWriteConut1page
								.containsKey(page_number)) {
							lpnWriteConut1page.put(page_number, 1);
						} else {
							temp = lpnWriteConut1page.get(page_number);
							lpnWriteConut1page.put(page_number, ++temp);
						}
//
//						// ∆Ø¡§ Address µø¿€ Ω√∞£ √ﬂ√‚
//						if ((page_number == 770055)) {
//							tempAddr = Long.valueOf(elements[0]).longValue();
//							System.out.println((tempAddr - tempFirst) + " 1");
//
//						} else {
//							tempAddr = Long.valueOf(elements[0]).longValue();
//							System.out.println((tempAddr - tempFirst));
//						}
//						// **

					} else if (elements[3].equals("Read")) {
						read_1pageCount++;

						if (false == lpnWriteConut1page
								.containsKey(page_number)) {
							lpnReadConut1page.put(page_number, 1);
						} else {
							temp = lpnWriteConut1page.get(page_number);
							lpnReadConut1page.put(page_number, ++temp);
						}
					}
				}

				// in case of multi-pages
				for (int i = 0; i < page_size; i++) {
					if (elements[3].equals("Write")) {
						writeCount++;
						totalWritePagesCount++;

						if (false == lpnWriteConut.containsKey(page_number + i)) {
							lpnWriteConut.put(page_number + i, 1);
						} else {
							temp = lpnWriteConut.get(page_number + i);
							lpnWriteConut.put(page_number + i, ++temp);
						}
					} else if (elements[3].equals("Read")) {
						readCount++;
						totalReadPagesCount++;
						if (false == lpnWriteConut.containsKey(page_number + i)) {
							lpnReadConut.put(page_number + i, 1);
						} else {
							temp = lpnWriteConut.get(page_number + i);
							lpnReadConut.put(page_number + i, ++temp);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while reading csv file: " + e);
		}

		MapToTxt(lpnWriteConut, Config.FILE_LPN_WRITE_OUTPUT);
		MapToTxt(lpnReadConut, Config.FILE_LPN_READ_OUTPUT);
		MapToTxt(lpnWriteConut1page, Config.FILE_LPN_WRITE_OUTPUT_1PAGE);
		MapToTxt(lpnReadConut1page, Config.FILE_LPN_READ_OUTPUT_1PAGE);

		FileWriter fw = new FileWriter(new File(Config.FILE_LOG_OUTPUT));
		BufferedWriter bw = new BufferedWriter(fw);

		bw.write("Total Request Count        = " + totalCount);
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("Total Write Pages Count    = " + totalWritePagesCount);
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("Total Read Pages Count     = " + totalReadPagesCount);
		bw.write("\n\n-----------------------------------------\n\n");
		bw.write("Write Request Count        = " + writeCount);
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("Read Request Count         = " + readCount);
		bw.write("\n\n-----------------------------------------\n\n");
		bw.write("Write 1-Page Request Count = " + write_1pageCount);
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("Read 1-Page Request Count  = " + read_1pageCount);
		bw.write("\n\n-----------------------------------------\n\n");
		bw.write("LPN WRITE LIST SIZE        = " + lpnWriteConut.size());
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("LPN READ LIST SIZE         = " + lpnReadConut.size());
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("LPN WRITE LIST SIZE(1page) = " + lpnWriteConut1page.size());
		bw.newLine(); // ¡ŸπŸ≤ﬁ
		bw.write("LPN READ LIST SIZE(1page)  = " + lpnReadConut1page.size());
		bw.newLine(); // ¡ŸπŸ≤ﬁ

		bw.close();
		fw.close();

		System.out.println("** Workload Analysis End **");
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
			bw.write(pairs.getKey() + "," + pairs.getValue() + "\n");
		}

		// lastly, close the file and end
		bw.close();
		return;
	}
}