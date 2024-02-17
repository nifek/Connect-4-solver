package Connect4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//setup methods
public class Setup {
	public static void clearBoard(Board board) {
		board.board = board.player_board = board.reversed_board = board.reversed_player_board = 0;
		board.mover = false;
		board.moves_played = 0;
	}
	
	//initializes all the variables
	public static void createBoard(Board board) {
		board.m++;
		board.board_size = board.n * board.m;
		board.bottom_layer = 0;
		for(int i = 0; i < board.n; i++) {
			board.bottom_layer |= 1l << i * board.m;
		}
		//does not work when board_size = 64 !!!
		board.full_mask = (1l << board.board_size) - 1;
		board.playable_mask = board.full_mask;
		for(int i = 0; i < board.n; i++) {
			board.playable_mask ^= 1l << board.getIndex(0, i);
		}
		board.column_mask = new long[board.n];
		board.column_mask[0] = (1l << board.m) - 1;
		for(int i = 0; i < board.n; i++) {
			board.column_mask[i] = board.column_mask[0] << i * board.m;
		}
		board.playable_board = board.board_size - board.n;
		MoveOrder.column_order = new int[board.board_size];
		int temp_order[] = new int[board.n];
		if(board.n % 2 == 1) {
			temp_order[0] = board.n / 2;
			for(int i = 1; i < board.n; i++) {
				temp_order[i] = (i % 2 == 1? board.n / 2 - (i + 1) / 2 : board.n / 2 + i / 2);
			}
		}
		else {
			for(int i = 0; i < board.n; i++) {
				temp_order[i] = (i % 2 == 1? board.n / 2 - (i + 1) / 2 : board.n / 2 + i / 2);
			}
		}
		int position_order[] = new int[board.n];
		for(int i = 0; i < board.n; i++) position_order[temp_order[i]] = i;
		board.column_index = new int[board.board_size];
		for(int i = 0; i < board.board_size; i++) {
			board.column_index[i] = i / board.m;
			MoveOrder.column_order[i] = position_order[board.column_index[i]];
		}
		board.reversed_index = new int[board.board_size];
		for(int i = 0; i < board.m; i++) {
			for(int j = 0; j < board.n; j++) {
				board.reversed_index[board.getIndex(i, j)] = board.getIndex(i, board.n - j - 1);
			}
		}
		board.left_mask = 0;
		for(int i = 1; i < board.m; i++) {
			for(int j = 0; j < (board.n + 1) / 2; j++) {
				board.left_mask |= 1l << board.getIndex(i, j);
			}
		}
		MoveOrder.move_list = new Integer[board.n];
		MoveOrder.score_list = new int[board.board_size];
		board.inf = board.playable_board + 1;
		clearBoard(board);
	}
	
	public static void setup_position(Board board, String position) {
		clearBoard(board);
		for(int i = 0; i < position.length(); i++) {
			int column = position.charAt(i) - '0' - 1;
			assert(board.isLegal(column));
			board.make_move(board.getMove(column));
		}
	}
	
	//plays a single game with reading all the moves from the keyboard
	public static void playAGame(int n, int m, boolean outputScore) {
		Solver solver = new Solver(n, m);
		createBoard(solver.board);
		while(true) {
			solver.board.displayBoard(); 
			if(outputScore) System.out.println("Evaluation: " + solver.solve(false));
			long legalMoves = solver.board.getLegalMoves();
			if(legalMoves == 0) {
				System.out.println("Draw.");
				break;
			}
			System.out.println("Choose a column to play");
			int column = Runner.commandReader.nextInt() - 1;
			while(!solver.board.isLegal(column)) {
				System.out.println("Invalid move, choose another column");
				column = Runner.commandReader.nextInt() - 1;
			}
			int move = solver.board.getMove(column);
			System.out.println("Making a move " + move);
			if(solver.board.isWinningMove(move)) {
				System.out.println("Player " + (solver.board.mover? 2 : 1) + " won");
				break;
			}
			solver.board.make_move(move);
		}
	}
	
	//outputs a solution
	public static void solve(int n, int m, boolean weak) {
		Solver solver = new Solver(n, m);
		Helper.measureTime();
		int result = solver.solve(weak);
		double time = Helper.getTime();
		System.out.println("Score: " + result);
		System.out.println("Took " + time + " seconds, had to search " + solver.positions_searched + " positions");
		System.out.printf("Positions searched / second: %.10f \n", (double) solver.positions_searched / time);
	}
	
	public static void getPerfectGame(int n, int m) {
		Solver solver = new Solver(n, m);
		Helper.measureTime();
		solver.positions_searched = 0;
		String game = "";
		while(true) {
			int result[] = solver.getBestMove();
			int score = Helper.convertScore(solver.board, result[0]);
			int move = result[1];
			int column = solver.board.column_index[move] + 1;
			System.out.println("Score: " + score + ", best move: " + column);
			game += (char) ('0' + column);
			boolean ended = solver.board.isWinningMove(move);
			solver.board.make_move(move);
			if(ended || solver.board.moves_played == solver.board.playable_board) break;
		}
		double time = Helper.getTime();
		solver.board.displayBoard();
		System.out.println("Perfect game: " + game);
		System.out.println("Took " + time + " seconds, had to search " + solver.positions_searched + " positions");
		System.out.printf("Positions searched / second: %.10f \n", (double) solver.positions_searched / time);
	}
	
	public static void createMoveTablebase(int n, int m, int depth) {
		Solver solver = new Solver(n, m);
		if(solver.precalculation_depth >= depth) {
			System.out.println("Already have a " + solver.precalculation_depth + "-depth table");
			return;
		}
		solver.score_dp.clear(); solver.move_dp.clear();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Helper.direction + "Tables/" + n + "-" + m + ".txt"));
			String s = reader.readLine();
			if(s != null && s.split(" ").length == 3) {
				//need to get saved results
				String curr[] = s.split(" ");
				long key = Long.parseLong(curr[0]);
				byte score = Byte.parseByte(curr[1]);
				byte move = Byte.parseByte(curr[2]);
				solver.score_dp.put(key, score);
				solver.move_dp.put(key, move);
				while((s = reader.readLine()) != null) {
					curr = s.split(" ");
					key = Long.parseLong(curr[0]);
					score = Byte.parseByte(curr[1]);
					move = Byte.parseByte(curr[2]);
					solver.score_dp.put(key, score);
					solver.move_dp.put(key, move);
				}
			}
			reader.close();
		}
		catch(IOException e) {
			System.out.println("First time creating this tablebase");
		}
		try {
			Solver.writer = new BufferedWriter(new FileWriter(Helper.direction + "Tables/" + n + "-" + m + ".txt"));
			System.out.println("Already had " + solver.score_dp.size() + " positions");
			for(long key : solver.score_dp.keySet()) {
				byte score = solver.score_dp.get(key);
				byte move = solver.move_dp.get(key);
				Solver.writer.write(key + " " + score + " " + move + '\n');
			}
			Solver.writer.flush();
		}
		catch(IOException e) {
			System.out.println(e);
		}
		Helper.measureTime();
		solver.searchAllGames(depth);
		double time = Helper.getTime();
		System.out.println("Total positions: " + solver.score_dp.size());
		System.out.println("Took " + time + " seconds, had to search " + solver.positions_searched + " positions");
		System.out.printf("Positions searched / second: %.10f \n", (double) solver.positions_searched / time);
		//store the tablebase 
		try {
			Solver.writer.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(Helper.direction + "Tables/" + n + "-" + m + ".txt"));
			writer.write(solver.score_dp.size() + " " + depth + "\n");
			for(long key : solver.score_dp.keySet()) {
				byte score = solver.score_dp.get(key);
				byte move = solver.move_dp.get(key);
				writer.write(key + " " + score + " " + move + '\n');
			}
			writer.close();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	//plays a single game with reading all the moves from the keyboard
	public static void play(int n, int m, boolean mover) {
		Solver solver = new Solver(n, m);
		while(true) {
			solver.board.displayBoard(); 
			long legalMoves = solver.board.getLegalMoves();
			if(legalMoves == 0) {
				System.out.println("Draw.");
				break;
			}
			int move;
			if(solver.board.mover == mover) {
				System.out.println("Choose a column to play");
				int column = Runner.commandReader.nextInt() - 1;
				while(!solver.board.isLegal(column)) {
					System.out.println("Invalid move, choose another column");
					column = Runner.commandReader.nextInt() - 1;
				}
				move = solver.board.getMove(column);
			}
			else {
				int result[] = solver.getBestMove();
				move = result[1];
				int score = result[0];
				System.out.println("Score: " + Helper.convertScore(solver.board, score));
				System.out.println("Playing column " + (solver.board.column_index[move] + 1));
			}
			if(solver.board.isWinningMove(move)) {
				System.out.println("Player " + (solver.board.mover? 2 : 1) + " won");
				break;
			}
			solver.board.make_move(move);
		}
	}
}

///7 7: 4444444333221335335655555222226666661111117777777
