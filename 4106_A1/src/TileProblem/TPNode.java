package TileProblem;

import java.util.Comparator;

public class TPNode {

	///// state
	public int[][] state;

	////parent node
	public TPNode parent;
	
	////action -- represents the action that brought the problem to this state
	public String action;
	
	////depth
	public int depth;
	public boolean isClosed;

	public TPNode(int[][] vals){
		state = vals.clone();
		
		action = "Starting postion";
		parent = null;
		depth = 0;
	}
	
	public TPNode(TPNode par, int startX, int startY, int targX, int targY, String act){
		state = new int[TileProblem.TP_X][TileProblem.TP_Y];
		for(int i = 0; i< par.state.length; ++i){
			for(int j = 0; j < par.state[0].length; ++j){
				state[i][j] = par.state[i][j];
			}
		}
		
		isClosed = false;
		
		
		//state = par.state.clone();
		swap(startX, startY, targX, targY);
		action = "Swap (" + startY + "," + startX + ") with (" + targY + "," + targX + ") -- " + act;
		parent = par;
		depth = par.depth + 1;
	}
	
	public void swap(int startX, int startY, int targX, int targY){
		int temp = state[startX][startY];
		state[startX][startY] = state[targX][targY];
		state[targX][targY] = temp;
	}
	
	public String hashVal(){
		String r = "";
		for(int[] i: state){
			for(int j: i){
				r += j + ".";
			}
		}
		return r;

	}
	
	public String toString(){
		String rv = "\n";
		String hRule = "+";
		for(int i:state[0]){hRule += "----+";}
		hRule += "\n";
		rv += hRule;
		for(int[] j: state){
			rv += "|";
			for(int i: j){
				rv += " " + (i ==0 ? " X" : (String.format("%1$-" + 2 + "s", i))) + " |";
			}
			rv += "\n" + hRule;
		}
		rv += action + "\n" + hRule; 
		rv += "g(x): " + depth + "\n";
		rv += "Tiles out of place h(x): " + hTP() + "\n";
		rv += "Moves left h(x): " + hML(); // + "\n";
		
		return rv;
	}
	
	public Integer hTP(){
		int count = 0;

		for(int j = 0; j < state[0].length; ++j){
			for(int i = 0; i < state.length; ++i){
				count += (state[i][j] == (i * state[0].length + j) ? 0: 1);
			}
		}

		return count;
	}
	
	public Integer heuristicTilesOutOfPlace(){
		return depth + hTP();
	}
	
	public Integer heuristicMovesLeft(){
		return depth + hML();		
	}
	
	public Integer heuristicAvg(){
		return depth + (hML() + hTP())/2;		
	}
	
	public Integer hML(){
		float sum = 0.0f;
		//The board is indexed by [TP_X] [TP_Y]
		//TP_X is the vertical coordinate and TP_Y is the horizontal coordinate
		//j loops from 0 to TP_Y and i loops from 0 to TP_X
		//j counts the horzontal and i counts the vertical
		//so for java -jar TP.jar 4 2, i goes to 2 and j goes to 4
		//TP_X is 4 and TP_Y is 2
//System.out.println("Check heuristic");		
		
		for(int j = 0; j < state[0].length; ++j){
			for(int i = 0; i < state.length; ++i){
				//TileI and TileJ represent the target coordinates of the tile
				int tileI = state[i][j] / TileProblem.TP_Y;
				int tileJ = state[i][j] % TileProblem.TP_Y;
				float di = Math.abs(tileI -i);
				float dj = Math.abs(tileJ - j);
				//int dist = 0;
//System.out.printf("i: %d j: %d Tile: %d Tilei: %d Tilej: %d di: %d dj: %d\n", i,j,state[i][j],tileI, tileJ, di, dj);				
				//tile is in the right place -- add 0
				if (di == 0 && dj == 0) sum += 0.0f;
				//tile is a knightmove away - estimate 1
				else if( (di == 1 && dj == 2) || (di == 2 && dj == 1)){
					// look to see if the target knightmove is also correct
					if(state[tileI][tileJ] == (i * state[0].length + j) )
						sum += 0.5; //add 0.5 since this will trigger twice
					else sum += 1.0; 
				}
				//tile is just the maximum of x and y distance since we allow diagonals
				else {
					float max = Math.max(di,  dj);
					//if(max > 2) max *= 0.75; //correction factor for anticipated knightmoves
					sum += max;
				}
//System.out.printf("Tile value: %d  Distance: %d\n", state[i][j], dist);				
			}
		}
		return (int)sum;
		
	}
	
	public boolean isWinner(){
		/*
		 * Looks for a winner with this configuration:
		 *  +---+---+---+
		 *    0   1   2
		 *  +---+---+---+
		 *    3   4   5
		 *  +---+---+---+
		 *    6   7   8
		 *  +---+---+---+ 
		 * 
		 * */
		
		
		boolean rv = true;
		//int count = 0;

		for(int j = 0; j < state[0].length; ++j){
			for(int i = 0; i < state.length; ++i){
				rv = rv && (state[i][j] == (i * state[0].length + j));
			}
		}
		return rv;
	}	
	
}
