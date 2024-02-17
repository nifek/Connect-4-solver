package Connect4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Tester {
	public static final String easy_tests[] = {"7-6-easy-1"};
	public static final String medium_tests[] = {"7-6-medium-1", "7-6-medium-2"};
	public static final String hard_tests[] = {"7-6-hard-1", "7-6-hard-2", "7-6-hard-3"};	
	public static long total_positions = 0;
	public static boolean correct;
	
	public static long run_test(String testPath) {
		int total_tests = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Helper.direction + "Tests/"  + testPath + ".txt"));
			String line = reader.readLine();
			int n = Integer.parseInt(line.split(" ")[0]);
			int m = Integer.parseInt(line.split(" ")[1]);
			Solver solver = new Solver(n, m);
			while((line = reader.readLine()) != null) {
				String position = line.split(" ")[0];					
				Setup.setup_position(solver.board, position);
				int expected_result = Integer.parseInt(line.split(" ")[1]);
				int result = solver.solve(false);
				if(result != expected_result) {
					System.out.println(position + ": excepted " + expected_result + ", got " + result);
					correct = false;
				}
				total_positions += solver.positions_searched;
				total_tests++;
				if(total_tests % 200 == 0) {
					System.out.println("Finished " + total_tests + " tests");
				}
			}
			reader.close();	
		}
		catch(IOException e) {
			System.out.println("Failed to locate a test folder " + testPath);
		}
		return total_tests;
	}
	
	public static void run_test_group(String group_name, String tests[]) {
		System.out.println("Running test group \"" + group_name + "\"\n");
		for(String testPath : tests) {
			total_positions = 0;
			correct = true;
			System.out.println("Running test \"" + testPath + "\"");
			Helper.measureTime();
			long total_tests = run_test(testPath);
			if(!correct) continue;
			double time = Helper.getTime();
			long best_number_of_positions = (long) 1e18;
			double best_time_taken = 1e18;
			try {
				BufferedReader best_reader = new BufferedReader(new FileReader(Helper.direction + "Tests/"  + testPath + ".results.txt"));
				String line = best_reader.readLine();
				if(line != null) {
					best_number_of_positions = Long.parseLong(line);
					line = best_reader.readLine();
					best_time_taken = Double.parseDouble(line);
				}
				best_reader.close();
				BufferedWriter writer = new BufferedWriter(new FileWriter(Helper.direction + "Tests/" + testPath + ".results.txt"));
				writer.write(Math.min(best_number_of_positions, total_positions) + "\n");
				writer.write(Math.min(best_time_taken, time) + "\n");
				writer.close();
			}
			catch(IOException e) {
				System.out.println("Failed to locate a test folder " + testPath);
			}
			System.out.println("Finished test \"" + testPath + "\"\n");
			System.out.println("Took " + time + " seconds, had to search " + total_positions + " positions");
			System.out.printf("Seconds / position: %.10f \n", time / total_tests);
			System.out.printf("Positions searched / second: %.10f \n", (double) total_positions / time);
			if(best_number_of_positions > total_positions) {
				System.out.println("New record: had to search only " + total_positions + " positions, previous best: " + best_number_of_positions);
			}
			if(best_time_taken > time) {
				System.out.println("New record: finished in " + time + " seconds, previous best: " + best_time_taken);
			}
			System.out.println();
		}
		System.out.println("Finished test group \"" + group_name + "\"\n");
	}
	
	//runs easy tests
	public static void run_easy_tests() {
		Tester.run_test_group("Easy", Tester.easy_tests);
	}
	
	//runs medium tests
	public static void run_medium_tests() {
		Tester.run_test_group("Medium", Tester.medium_tests);
	}
	
	//runs hard tests
	public static void run_hard_tests() {
		Tester.run_test_group("Hard", Tester.hard_tests);
	}
	
	public static void solveTest(int n, int m, String position, boolean weak) {
		Solver solver = new Solver(n, m); Setup.setup_position(solver.board, position);
		Helper.measureTime();
		int result = solver.solve(weak);
		double time = Helper.getTime();
		System.out.println("Score: " + result);
		System.out.println("Took " + time + " seconds, had to search " + solver.positions_searched + " positions");
		System.out.printf("Positions searched / second: %.10f \n", (double) solver.positions_searched / time);
	}
}
