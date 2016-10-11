package workloadAnalysis;

public class Config {

	public static int PAGE_BYPT_SIZE = 4096; // 4KB = 4096
	public static String FINE_INPUT = "D:/수업 자료/0. 연구 자료/Traces_MSR/"
			+ "msr-cambridge2/MSR-Cambridge/web_2.csv/CAMRESWEBA03-lvm2.csv";
//	public static String FINE_INPUT = "D:/수업 자료/0. 연구 자료/Traces_MSR/"
//			+ "msr-cambridge2/MSR-Cambridge/web_0.csv/CAMRESWEBA03-lvm0.csv";
	
	/*
	 * public static String FINE_INPUT = "./test.csv";
	 */
	
	public static String FILE_LOG_OUTPUT = "./FILE_LOG_OUTPUT.txt";
	public static String ACCESS_TIMES = "./ACCESS_TIMES.txt";

	public static String FILE_LPN_WRITE_OUTPUT = "./FILE_LPN_WRITE_OUTPUT.txt";
	public static String FILE_LPN_WRITE_OUTPUT_1PAGE = "./FILE_LPN_WRITE_OUTPUT_1PAGE.txt";
	public static String FILE_LPN_READ_OUTPUT = "./FILE_LPN_READ_OUTPUT.txt";
	public static String FILE_LPN_READ_OUTPUT_1PAGE = "./FILE_LPN_READ_OUTPUT_1PAGE.txt";
}