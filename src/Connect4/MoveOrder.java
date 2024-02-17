package Connect4;

import java.util.Arrays;
import java.util.Comparator;

public class MoveOrder {
	//an array of available moves
	public static Integer move_list[];
	//list of scores (not to calculate it many times!)
	public static int score_list[];
	//count of available moves
	public static int move_count;
	//a permutation of the order of moves
	public static int column_order[];
	
	//assigns each move a score value
	public static int getScore(Board board, int move) {
		long good_mask = board.AlmostFour(board.player_board ^ (1l << move)) & ~(board.board ^ board.player_board);
		return Helper.pop_count(good_mask);
	}
	
	//comparator for sorting
	public static Comparator<Integer> comp = new Comparator<Integer>() {
	    public int compare(Integer a, Integer b) {
	    	if(score_list[a] == score_list[b]) return (column_order[a] < column_order[b]? -1 : 1);
	    	return (score_list[a] > score_list[b]? -1 : 1);
	    }
	};
	
	public static void order_moves(Board board, long legalMoves) {
		move_count = 0;
		while(legalMoves != 0) {
			int move = Helper.log(legalMoves);
			legalMoves ^= 1l << move;
			move_list[move_count++] = move;
			score_list[move] = getScore(board, move);
		}
		Arrays.sort(move_list, 0, move_count, comp);
	}
}
