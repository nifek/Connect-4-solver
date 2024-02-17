package Connect4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

//search and similar
public class Solver {
	//the number of iterations of dfs
	public long positions_searched = 0;
	public Board board;
	private Transpositions table;
	
	public Solver(int n, int m) {
		board = new Board(n, m);
		table = new Transpositions();
		precalculation_depth = -1;
		read();
	}
	
	public static int bestMove;
	
	
	//gets a strong solution using dfs and alpha-beta pruning
	//modifies global variables
	/*
		Let return = the returned value and score = the actual score of the position
		We are only interested in scores in an interval [alpha, beta] 
		Then:
		if alpha <= score <= beta, return = score
		if score < alpha, return = alpha
		if beta < score, beta <= return <= score
	 */
	//DOES NOT WORK IF THERE IS A MATE ON THE NEXT MOVE
	public int solve(int alpha, int beta, boolean saveMove) {
		positions_searched++;
		long key = board.getKey();
		long reversed_key = board.getReversedKey();
		//tightens alpha and beta using transposition table
		int index = (int) (key % table.table_size);
		if(!saveMove) {
			alpha = table.adjust_alpha(alpha, key, index); 
			beta = table.adjust_beta(beta, key, index);
		}
		//iff the checkmate is in a move
		beta = Math.min(beta, board.inf - board.moves_played - 3);
		if(alpha >= beta) return alpha;
		long legalMoves = board.getNonLosingMoves();
		if(key == reversed_key) legalMoves &= board.left_mask;
		//we lose on the next turn
		if(legalMoves == 0) return board.moves_played - board.inf + 2;
		//we cant lose on the next turn (nor win immediately)
		if(board.moves_played + 2 >= board.playable_board) return 0;
		MoveOrder.order_moves(board, legalMoves);
		int count = MoveOrder.move_count;
		Integer move_order[] = new Integer[count];
		for(int i = 0; i < count; i++) move_order[i] = MoveOrder.move_list[i];
		byte score_type = Transpositions.stored_upper_const;
		for(int i = 0; i < count; i++) {
			int move = move_order[i];
			board.make_move(move);
			int score = -solve(-beta, -alpha, false);
			if(alpha < score) {
				alpha = score;
				score_type = Transpositions.stored_exact_const;
				if(saveMove) bestMove = move;
			}
			board.unmake_move(move);
			if(alpha >= beta) {
				table.store_score(key, (byte) alpha, Transpositions.stored_lower_const, index);
				if(key != reversed_key) table.store_score(reversed_key, (byte) alpha, Transpositions.stored_lower_const, (int) (reversed_key % table.table_size));
				return alpha;
			}
		}
		table.store_score(key, (byte) alpha, score_type, index);
		if(key != reversed_key) table.store_score(key, (byte) alpha, score_type, (int) (reversed_key % table.table_size));
		return alpha;
	}
	
	
	public int null_window_binary_search(int l, int r) {
		while(l <= r) {
			int mid = l + r >> 1;
			if(mid <= 0 && l / 2 < mid) mid = l / 2;
			else if(mid >= 0 && r / 2 > mid) mid = r / 2;
			int score = solve(mid, mid + 1, false);
			if(score <= mid) r = mid - 1;
			else l = mid + 1;
		}
		return l;
	}
	
	public int startSearch(boolean weak) {
		long legalMoves = board.getLegalMoves();
		while(legalMoves != 0) {
			int move = Helper.log(legalMoves);
			legalMoves ^= 1l << move;
			if(board.isWinningMove(move)) return board.inf - board.moves_played - 1;
		}
		if(board.moves_played + 1 >= board.playable_board) return 0;
		if(weak) return solve(-1, 1, false);
		else return null_window_binary_search(-board.inf, board.inf);
	}
	
	public int solve(boolean weak) {
		long key = board.getKey();
		if(score_dp.containsKey(key)) {
			int result = score_dp.get(key);
			if(weak && result != 0) result = (result > 0? 1 : -1);
			else if(!weak) result = Helper.convertScore(board, result);
			return result;
		}
		positions_searched = 0;
		if(weak) {
			return startSearch(true);
		}
		else {
			int result = startSearch(false);
			return Helper.convertScore(board, result);
		}
	}
	
	//returns {score, move}
	public int[] getBestMove() {
		long key = board.getKey();
		if(score_dp.containsKey(key)) {
			int result[] = {score_dp.get(key), move_dp.get(key)};
			return result;
		}
		long legalMoves = board.getLegalMoves();
		int result[] = {-board.inf, -1};
		while(legalMoves != 0) {
			int move = Helper.log(legalMoves);
			legalMoves ^= 1l << move;
			if(board.isWinningMove(move)) {
				result[0] = board.inf - board.moves_played - 1;
				result[1] = move;
				return result;
			}
		}
		legalMoves = board.getLegalMoves();
		if(board.moves_played + 1 >= board.playable_board) {
			result[0] = 0;
			result[1] = Helper.log(legalMoves);
			return result;
		}
		int l = -board.inf, r = board.inf;
		while(l <= r) {
			int mid = l + r >> 1;
			if(mid <= 0 && l / 2 < mid) mid = l / 2;
			else if(mid >= 0 && r / 2 > mid) mid = r / 2;
			bestMove = -1;
			int score = solve(mid, mid + 1, true);
			if(score <= mid) {
				r = mid - 1;
				result[0] = mid;
			}
			else {
				l = mid + 1;
				result[1] = bestMove;
			}
		}
		if(result[1] == -1) result[1] = Helper.log(legalMoves);
		return result;
	}
	
	int getScore() {
		return Helper.convertScore(board, getBestMove()[0]);
	}
	
	public int precalculation_depth;
	
	//storing encountered positions
	public Map<Long, Byte> score_dp = new TreeMap<Long, Byte>();
	public Map<Long, Byte> move_dp = new TreeMap<Long, Byte>();
	
	private void read() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Helper.direction + "Tables/" + board.n + "-" + (board.m - 1) + ".txt"));
			String curr[] = reader.readLine().split(" ");
			if(curr.length != 2) {
				reader.close();
				return;
			}
			int N = Integer.parseInt(curr[0]);
			precalculation_depth = Integer.parseInt(curr[1]);
			for(int i = 0; i < N; i++) {
				curr = reader.readLine().split(" ");
				long key = Long.parseLong(curr[0]);
				byte score = Byte.parseByte(curr[1]);
				byte move = Byte.parseByte(curr[2]);
				score_dp.put(key, score); move_dp.put(key, move);
			}
			reader.close();
		}
		catch(IOException e) {
			System.out.println("No precalculation found");
		}
	}
	
	public static BufferedWriter writer;
	
	private void saveScore(long key, int score, int move) {
		score_dp.put(key, (byte) score);
		move_dp.put(key, (byte) move);
		try {
			writer.write(key + " " + score + " " + move + '\n');
			writer.flush();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public int searchAllGames(int depth) {
		positions_searched++;
		long key = board.getKey();
		if(score_dp.containsKey(key)) return score_dp.get(key);
		long legalMoves = board.getLegalMoves();
		int score = -board.inf;
		if(board.hasWon(board.board ^ board.player_board)) score = board.moves_played - board.inf;
		else if(legalMoves == 0) score = 0;
		if(depth == 0) {
			int result[] = getBestMove();
			saveScore(key, result[0], result[1]);
			return result[0];
		}
		if(score != -board.inf) {
			saveScore(key, score, -1);
			return score;
		}
		int bestMove = -1;
		while(legalMoves != 0) {
			int move = Helper.log(legalMoves);
			legalMoves ^= 1l << move;
			board.make_move(move);
			int curr = -searchAllGames(depth - 1);
			board.unmake_move(move);
			if(curr > score) {
				score = curr;
				bestMove = move;
			}
		}
		saveScore(key, score, bestMove);
		return score;
	}
	
	public void clear() {
		table.actual_position = null;
		table.information_stored = null;
		table.stored_score = null;
	}
}
