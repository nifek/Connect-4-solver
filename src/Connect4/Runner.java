package Connect4;

import java.util.Scanner;

public class Runner {
		
	public static Scanner commandReader = new Scanner(System.in);
	
	public static void main(String[] args) {
		while(true) {
			String command = commandReader.next();
			if(command.equals("exit") || command.equals("stop")) break;
			else if(command.equals("UI")) {
				int n, m;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				if(n * (m + 1) >= 64) {
					System.out.println("n * (m + 1) has to be less than 64");
				}
				else {
					UI ui = new UI(n, m);
					ui.run();
				}
			}
			else if(command.equals("solve")) {
				int n, m;
				boolean weak;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				weak = (commandReader.nextInt() != 0);
				Setup.solve(n, m, weak);
			}
			else if(command.equals("solve-position")) {
				int n, m;
				boolean weak;
				String position;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				position = commandReader.next();
				weak = (commandReader.nextInt() != 0);
				Tester.solveTest(n, m, position, weak);
			}
			else if(command.equals("play")) {
				int n, m;
				boolean outputScore;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				outputScore = (commandReader.nextInt() != 0);
				Setup.playAGame(n, m, outputScore);
			}
			else if(command.equals("run-test-group")) {
				String group = commandReader.next();
				if(group.equals("easy")) Tester.run_easy_tests();
				else if(group.equals("medium")) Tester.run_medium_tests();
				else if(group.equals("hard")) Tester.run_hard_tests();
				else System.out.println("Unknown test group \"" + group + "\"");
			}
			else if(command.equals("show-perfect-game")) {
				int n, m;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				Setup.getPerfectGame(n, m);
			}
			else if(command.equals("create-table")) {
				int n, m, depth;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				depth = commandReader.nextInt();
				Setup.createMoveTablebase(n, m, depth);
			}
			else if(command.equals("1v1")) {
				int n, m;
				boolean mover;
				n = commandReader.nextInt();
				m = commandReader.nextInt();
				mover = (commandReader.nextInt() != 0);
				Setup.play(n, m, mover);
			}
			else if(command.equals("help")) {
				System.out.println("Available commands:");
				System.out.println("exit / stop: terminates the program.");
				System.out.println("UI [n] [m] [mover (0 / 1)]: opens a user interface with the ability to play an n by m Connect-4 game");
				System.out.println("solve-position [n] [m] [weak (0 / 1)] [position]: solves the given position of a n by m Connect-4 game (outputs strong solution if weak = 0, weak solution otherwise).");
				System.out.println("play [n] [m] [output_score (0 / 1)]: allows a user to play a n by m Connect-4 game with himself. Also outputs a strong score of a position of output_score is not 0.");	
				System.out.println("run-test-group [group_game ({easy, medium, hard})]: runs a Connect-4 solver on a chosen test group and outputs its peformance.");
				System.out.println("show-perfect-game [n] [m]: calculates and outputs a perfect n by m Connect-4 game.");
				System.out.println("create-table [n] [m] [depth]: calculates the scores and best moves of all different positions of n by m Connect-4 to a desired depth. Stores the results in a file named \"n-m\" (where n and m are from the input).");
				System.out.println("1v1 [n] [m] [mover (0 / 1)]: plays a n by m Connect-4 game against the solver, where user is the \"mover\" player (0 - first, 1 - second).");
			}
			else {
				System.out.println("Unknown command \"" + command + "\"");
			}
		}
		commandReader.close();
	}
}
