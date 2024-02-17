package Connect4;

//board representation
public class Board {
	//columns and rows
	public int n, m;
	//mask of all non-empty squares
	public long board;
	//mask of all squares of the currently moving player
	public long player_board;
	//currently moving player (false - 1, true - 2)
	public boolean mover;
	//moves played from the beginning
	public int moves_played;
	
	//we store reversed key (from right to left) in order to not have to calculate everything twice (the board is symmetrical)
	public long reversed_board, reversed_player_board;
	
	///help variables
	
	//total number of cells
	public int board_size;
	/*
		The board is arranged like this:
		3 7 11
		2 6 10
		1 5 9
		0 4 8
		An extra row is added to the top in order to check for a win faster
	 */
	//mask of the bottom layer of cells (is used to uniquely store a position in a long)
	public long bottom_layer;
	//mask containing all cells
	public long full_mask;
	//all playable cells (all except for the first row)
	public long playable_mask;
	//masks for all columns
	public long column_mask[];
	//total number of playable cells (n * m excluding the first row)
	public int playable_board;
	//inf constant
	public int inf;
	//column (not to make slow divisions)
	public int column_index[];
	//reversed cells indexing
	public int reversed_index[];
	//mask corresponding only to the left half of the board (so we can avoid playing symmetrical moves)
	public long left_mask;
	
	public Board(int N, int M) {
		n = N; m = M;
		if(n * (m + 1) > 64) {
			System.out.println("Error: too big n * (m + 1)");
			System.exit(0);
		}
		Setup.createBoard(this);
	}
	
	public Board(int N, int M, String position) {
		n = N; m = M;
		if(n * (m + 1) > 64) {
			System.out.println("Error: too big n * (m + 1)");
			System.exit(0);
		}
		Setup.createBoard(this);
		Setup.setup_position(this, position);
	}
	
	//gets a unique long key to store a position
	public long getKey() {
		return player_board + board;
	}
	
	public long getReversedKey() {
		return reversed_player_board + reversed_board;
	}
	
	//gets a mask of all legal moves
	public long getLegalMoves() {
		return (board + bottom_layer) & playable_mask;
	}
	
	public boolean isLegal(int column) {
		return (column_mask[column] & getLegalMoves()) != 0;
	}
	
	//a static private variables so that checking for a win becomes faster (no need to create a new variable inside of a function)
	private static long temp;
	
	public boolean hasWon(long mask) {
		/*
			horizontal line checking
			need to check whether there are 4 ones in a row
			we can compress 2 adjacent into one and then check one after that
			the other 3 cases are similar
			the top zero-row prevents us from checking in a non-sense way
		*/
		temp = mask & (mask >> m);
		if((temp & (temp >> 2 * m)) != 0) return true;
		//vertical
		temp = mask & (mask >> 1);
		if((temp & (temp >> 2)) != 0) return true;
		//diagonal \
		temp = mask & (mask >> m + 1);
		if((temp & (temp >> 2 * m + 2)) != 0) return true;
		//diagonal /
		temp = mask & (mask >> m - 1);
		if((temp & (temp >> 2 * m - 2)) != 0) return true;
		return false;
	}
	
	public boolean isDraw() {
		return (moves_played == n * (m - 1));
	}
	
	public boolean isWinningMove(int move) {
		return hasWon(player_board ^ (1l << move));
	}
	
	//gets positions of all 4 in a row except for 1 missing
	public long AlmostFour(long mask) {
		//vertical (only 1 possible configuration)
		long ans = (mask << 1) & (mask << 2) & (mask << 3);
		//horizontal 
		temp = (mask << m) & (mask << 2 * m);
		ans |= temp & (mask << 3 * m); //xxx.
		ans |= (mask >> m) & temp; //xx.x
		temp >>= 3 * m;
		ans |= temp & (mask >> 3 * m); //.xxx
		ans |= (mask << m) & temp; //x.xx
		m--;
		//diagonal \
		temp = (mask << m) & (mask << 2 * m);
		ans |= temp & (mask << 3 * m); //xxx.
		ans |= (mask >> m) & temp; //xx.x
		temp >>= 3 * m;
		ans |= temp & (mask >> 3 * m); //.xxx
		ans |= (mask << m) & temp; //x.xx
		m++; m++;
		//diagonal /
		temp = (mask << m) & (mask << 2 * m);
		ans |= temp & (mask << 3 * m); //xxx.
		ans |= (mask >> m) & temp; //xx.x
		temp >>= 3 * m;
		ans |= temp & (mask >> 3 * m); //.xxx
		ans |= (mask << m) & temp; //x.xx
		m--;
		return ans & playable_mask;
	}
	
	public long getNonLosingMoves() {
		long legalMoves = getLegalMoves();
		long badMoves = AlmostFour(board ^ player_board);
		long forcedMoves = legalMoves & badMoves;
		if(forcedMoves != 0) {
			//if there are more 1 forced move we lose on the next turn :(
			if((forcedMoves & (forcedMoves - 1)) != 0) return 0;
			//otherwise it is required to play it
			legalMoves = forcedMoves;
		}
		//we should not play below opponents winning move
		return legalMoves & ~(badMoves >> 1);
	}
	
	//assumes the move is legal
	public void make_move(int index) {
		player_board ^= board;
		board ^= 1l << index;
		//update reversed board
		index = reversed_index[index];
		reversed_player_board ^= reversed_board;
		reversed_board ^= 1l << index;
		moves_played++;
		mover ^= true;
	}
	
	//assumes the move was legal
	public void unmake_move(int index) {
		board ^= 1l << index;
		player_board ^= board;
		index = reversed_index[index];
		reversed_board ^= 1l << index;
		reversed_player_board ^= reversed_board;
		moves_played--;
		mover ^= true;
	}
	
	//returns the index of the cell
	public int getIndex(int i, int j) {
		return j * m + m - 1 - i;
	}
	
	//displays the indices of cells
	public void displayIndexing() {
		for(int i = 0; i < m; i++) {
			for(int j = 0; j < n; j++) {
				System.out.print(getIndex(i, j) + " ");
			}
			System.out.println();
		}
	}
	
	//displays to the console the mask as it is stored
	public void displayMask(long mask) {
		for(int i = 0; i < m; i++) {
			for(int j = 0; j < n; j++) {
				System.out.print(mask >> getIndex(i, j) & 1);
			}
			System.out.println();
		}
	}
	
	//displays the board (x - first, o - second)
	public void displayBoard() {
		for(int i = 1; i < m; i++) {
			for(int j = 0; j < n; j++) {
				if((board >> getIndex(i, j) & 1) != 1) System.out.print(0);
				else {
					char c = 'x';
					if(((player_board >> getIndex(i, j) & 1) != 1) ^ mover) c = 'o';
					System.out.print(c);
				}
			}
			System.out.println();
		}
	}
	
	public int getMove(int column) {
		return Helper.log(getLegalMoves() & column_mask[column]);
	}
}
