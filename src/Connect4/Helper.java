package Connect4;

//a class of general functions used everywhere
public class Helper {
	public static final String direction = "src/Connect4/Files/";
	//size should be a prime number so the number of collisions is minimized
	public static final int advised_table_size = 100000007;
	
	public static int log(long x) {
		return 63 - Long.numberOfLeadingZeros(x);
	}
	
	public static int pop_count(long x) {
		return Long.bitCount(x);
	}
	
	//converts a score to a comprehensible one
	public static int convertScore(Board board, int score) {
		if(score > 0) score = board.inf - score;
		else if(score < 0) score = -board.inf - score;
		return score;
	}
	
	public static long start_time;
	
	public static void measureTime() {
		start_time = System.nanoTime();
	}
	
	public static double getTime() {
		long length = System.nanoTime() - start_time;
		double time = length / 1e9;
		return time;
	}
}
