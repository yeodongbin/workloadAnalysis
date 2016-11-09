package workloadAnalysis;

public class Config {

	public static int WORK_LOAD = 2; 
	//1:MSR , 2:oltp financial1??, 3: Interval Analysis??
	
	public static int LBA_SIZE = 512; // 
	public static int PAGE_BYPT_SIZE = 4096; // 4KB = 4096
	public static int LOGICAL_BLOCK_NUM = 4096;
	
	public static String FILE_INPUT = "./Financial1.spc";
//	public static String FILE_INPUT = "C:/Users/ajou/git/workloadAnalysis/CAMRESWMSA03-lvm0.csv";
//	public static String FINE_INPUT = "D:/���� �ڷ�/0. ���� �ڷ�/Traces_MSR/"
//			+ "msr-cambridge2/MSR-Cambridge/web_0.csv/CAMRESWEBA03-lvm0.csv";
	
	
	public static String FILE_OUTPUT = "./Financial1.txt";
	public static String FILE_LOG_OUTPUT = "./FILE_LOG_OUTPUT.txt";
	public static String ACCESS_TIMES = "./ACCESS_TIMES.txt";

	public static String FILE_LPN_WRITE_OUTPUT = "./FILE_LPN_WRITE_OUTPUT.txt";
	public static String FILE_LPN_WRITE_OUTPUT_1PAGE = "./FILE_LPN_WRITE_OUTPUT_1PAGE.txt";
	public static String FILE_LPN_READ_OUTPUT = "./FILE_LPN_READ_OUTPUT.txt";
	public static String FILE_LPN_READ_OUTPUT_1PAGE = "./FILE_LPN_READ_OUTPUT_1PAGE.txt";
	
	public static String FILE_EACH_WRITE_PAGES = "./FILE_EACH_WRITE_PAGES.txt";
}