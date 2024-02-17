package Connect4;

public class Transpositions {
	/*
		Gets the index in the table by key % table_size
		We have to store the actual position in order to not have non-sense collisions
		
	 */
	public int table_size;
	public int actual_position[];
	//[-64, 64], can't compress into 7 bits, need 8 :(
	public byte stored_score[];
	//what is stored: exact evaluation, lower_bound, upper_bound, nothing
	//needs 2 bytes, maybe I should make 2 booleans for less memory
	public byte information_stored[];
	
	public Transpositions(int size) {
		table_size = size;
		actual_position = new int[table_size];
		stored_score = new byte[table_size];
		information_stored = new byte[table_size];
	}
	
	public Transpositions() {
		table_size = Helper.advised_table_size;
		actual_position = new int[table_size];
		stored_score = new byte[table_size];
		information_stored = new byte[table_size];
	}
	
	public static final byte stored_null_const = 0;
	public static final byte stored_lower_const = 1;
	public static final byte stored_upper_const = 2;
	public static final byte stored_exact_const = 3;
	
	public void clear() {
		for(int i = 0; i < table_size; i++) {
			information_stored[i] = stored_null_const;
		}
	}
	
	//maximizes alpha
	public int adjust_alpha(int alpha, long key, int index) {
		if(actual_position[index] == (int) key && information_stored[index] != stored_null_const) {
			if(information_stored[index] == stored_exact_const) alpha = stored_score[index];
			if(information_stored[index] == stored_lower_const && alpha < stored_score[index]) alpha = stored_score[index];
		}
		return alpha;
	}
	
	//minimizes beta
	public int adjust_beta(int beta, long key, int index) {
		if(actual_position[index] == (int) key && information_stored[index] != stored_null_const) {
			if(information_stored[index] == stored_exact_const) beta = stored_score[index];
			if(information_stored[index] == stored_upper_const && beta > stored_score[index]) beta = stored_score[index];
		}
		return beta;
	}
	
	public void store_score(long key, byte score, byte score_type, int index) {
		actual_position[index] = (int) (key);
		information_stored[index] = score_type;
		stored_score[index] = score;
	}
}

/*
1248889011
*/
