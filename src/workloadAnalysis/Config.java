package workloadAnalysis;

public class Config {

	public static int WORK_LOAD = 3; 
	//1:MSR , 2:oltp financial1, 3: Interval Analysis_MSR
	
	public static int LBA_SIZE = 512; // 
	public static int PAGE_BYPT_SIZE = 4096; // 4KB = 4096
	public static int LOGICAL_BLOCK_NUM = 4096;
	public static int LOGICAL_PAGE_NUM = 256;
	
	// 1: msr
	public static String FILE_INPUT = "C:/Users/ajou/git/workloadAnalysis/CAMRESWMSA03-lvm0.csv";
//	public static String FINE_INPUT = "D:/수업 자료/0. 연구 자료/Traces_MSR/"
//			+ "msr-cambridge2/MSR-Cambridge/web_0.csv/CAMRESWEBA03-lvm0.csv";
	
	public static String FILE_LOG_OUTPUT = "./FILE_LOG_OUTPUT.txt";
	public static String ACCESS_TIMES = "./ACCESS_TIMES.txt";

	public static String FILE_LPN_WRITE_OUTPUT = "./FILE_LPN_WRITE_OUTPUT.txt";
	public static String FILE_LPN_WRITE_OUTPUT_1PAGE = "./FILE_LPN_WRITE_OUTPUT_1PAGE.txt";
	public static String FILE_LPN_READ_OUTPUT = "./FILE_LPN_READ_OUTPUT.txt";
	public static String FILE_LPN_READ_OUTPUT_1PAGE = "./FILE_LPN_READ_OUTPUT_1PAGE.txt";
	public static String FILE_EACH_WRITE_PAGES = "./FILE_EACH_WRITE_PAGES.txt";
	
	// 2: oltp finanical1 
	public static String FILE_OLTP_INPUT = "./Financial1.txt"
			+ "";
	public static String FILE_OUTPUT = "./Financial1_out.txt";
	
	// 3: Interval 
	public static String FILE_AVG_INTERVAL = "./FILE_AVG_INTERVAL.txt";
	
}