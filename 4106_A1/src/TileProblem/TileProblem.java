package TileProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

public class TileProblem {
	
	//Size of the board
	static int TP_X; // = 3;
	static int TP_Y; // = 3;
	
	public ArrayList<TPNode> fringe;
	public PriorityQueue<TPNode> heuristicFringe;
	public HashMap<String,TPNode> statesExplored;
	public ArrayList<TPNode> buffer;
	public TPNode root;
	public static boolean solved;
	public static int nodesExplored;
	public static int peakFringeSize;
	public static int search;
	static String usage = "Usage: TP [-b -d] <x> <y> -r: random x by y board \nb = BFS d = DFS h1 = misplaced tiles heuristic, h2 = moves left heuristic, a = average heuristic\n"
			+ "TileProblem [-b -d] <x> <y> a1,a2,a3,a4,a5,a6,a7,a8: board with a1-n, b = BFS, d = DFS";
	
	static int[][] demo1 = {{2 , 1, 6},{0 ,4 , 8},{5, 3, 7}};	
	static int[][] demo2 = {{0 , 1, 2, 3},{4 ,5 , 6, 7},{8, 9, 10, 11}};
	static int[][] demo3 = {{2 , 1, 6, 9},{0 ,4 , 8, 11},{5, 3, 7, 10}};
	//static int[][] demo4;
	
	public Comparator<TPNode> heurMoves = ((n1, n2) -> n1.heuristicMovesLeft().compareTo(n2.heuristicMovesLeft())); 
	public Comparator<TPNode> heurTiles = ((n1, n2) -> n1.heuristicTilesOutOfPlace().compareTo(n2.heuristicTilesOutOfPlace())); 
	public Comparator<TPNode> heurAvg = ((n1, n2) -> n1.heuristicAvg().compareTo(n2.heuristicAvg())); 
	
	public static void main(String[] args){
		if(args.length != 4){
			System.out.println(usage);
			System.exit(-1);
		}
		
		if(args[0].equals("-b")) search = 1; 
		else if(args[0].equals("-d")) search = 0;
		else if(args[0].equals("-h1")) search = 2; //tiles out of place
		else if(args[0].equals("-h2")) search = 3; //moves left
		else if(args[0].equals("-a")) search = 4; //average
		else {
			System.out.println(usage);
			System.exit(-1);
		}
		
		try{
			TP_X = Integer.parseInt(args[1]);
		} catch(NumberFormatException e){
			System.out.println(usage);
			System.exit(-1);
		}
		
		try{
			TP_Y = Integer.parseInt(args[2]);
		} catch(NumberFormatException e){
			System.out.println(usage);
			System.exit(-1);
		}
		
		int[][] vals = demo1;
		if(args[3].equals("-r")){
			ArrayList<Integer> rand = new ArrayList<Integer>();
			for(int i = 0; i < TP_X * TP_Y; ++i) rand.add(i);
			long seed = System.nanoTime();
			Collections.shuffle(rand, new Random(seed));
			vals = new int[TP_X][TP_Y];
			int count=0;
			for(int i=0;i<TP_X;i++){
				for(int j=0;j<TP_Y;j++){
						vals[i][j]=rand.get(count);
						count++;
				}
			}
		} else{
			try{
				String[] pieces = args[3].split(",");
				vals = new int[TP_X][TP_Y];
				int count=0;
				for(int i=0;i<TP_X;i++){
					for(int j=0;j<TP_Y;j++){
							vals[i][j]=Integer.parseInt(pieces[count]);
							count++;
					}
				}
				
			} catch(NumberFormatException e){
				System.out.println(usage);
				System.exit(-1);
			}
		}
		
		
		TileProblem theProblem = new TileProblem();

		theProblem.run(vals);
		
	}
	
	public TileProblem(){
		fringe = new ArrayList<TPNode>();
		buffer = new ArrayList<TPNode>();
		if(search == 2)
			heuristicFringe = new PriorityQueue<TPNode>(heurTiles);
		else if (search == 3)
			heuristicFringe = new PriorityQueue<TPNode>(heurMoves);
		else if (search == 4)
			heuristicFringe = new PriorityQueue<TPNode>(heurAvg);
		else
			heuristicFringe = new PriorityQueue<TPNode>();
		
		statesExplored = new HashMap<String, TPNode>();
		solved = false;
		nodesExplored = 0;
		peakFringeSize = 0;
	}
	
	public void run(int[][] v){

		root = new TPNode(v);
		System.out.println("Initial board:");
		System.out.println(root);
		if(search == 0 || search == 1) fringe.add(root);
		else heuristicFringe.add(root);
		nodesExplored++;
		
		if(search == 0 || search == 1){
			while(!fringe.isEmpty() && !solved){
				if(fringe.size() > peakFringeSize) peakFringeSize = fringe.size();
				explore(fringe.remove(0));
			}
		} else if (search == 2 || search == 3 || search == 4){
			while(!heuristicFringe.isEmpty() && !solved){
				if(heuristicFringe.size() > peakFringeSize) peakFringeSize = heuristicFringe.size();
				TPNode node = heuristicFringe.poll();
				if(!node.isClosed) explore(node);
			}
		}
		
		handleFailure();
		
	}
	
	public void explore(TPNode n){
		//look for a win
		if(n.isWinner()){
			handleDone(n);
		}
		
		n.isClosed = true;
		int x = 0;
		int y = 0;
		int i = 0;
		int j = 0;
		for(int[] c: n.state){
			for(int r: c){
				if(r == 0){
					i = x;
					j = y;
				}
				y++;
			}
			x++;
			y = 0;
		}
		
		//search the board for knight moves
				for(int m = 0; m < TP_Y; ++m ){
					for(int k = 0; k < TP_X; ++k){
						if(n.state[k][m] != 0){
							
							//1< 2^ 
							if(k > 0 && m > 1 && n.state[k-1][m-2] != 0){
								TPNode newNode = new TPNode(n, k, m, k-1, m-2, "KnightMove (" + m + "," + k + ")-(" + (m-2) + "," + (k-1) + ")" );
								processNode(newNode);
							}
							
							//1> 2^ 
							if(k < TP_X -2 && m > 1 && n.state[k+1][m-2] != 0){
								TPNode newNode = new TPNode(n, k, m, k+1, m-2, "KnightMove (" + m + "," + k + ")-(" + (m-2) + "," + (k+1) + ")" );
								processNode(newNode);
							}
							
							//2> 1^ 
							if(k < TP_X -3 && m > 0 && n.state[k+2][m-1] != 0){
								TPNode newNode = new TPNode(n, k, m, k+2, m-1, "KnightMove (" + m + "," + k + ")-(" + (m-1) + "," + (k+2) + ")" );
								processNode(newNode);
							}
							
							//2< 1^ 
							if(k > 1 && m > 0 && n.state[k-2][m-1] != 0){
								TPNode newNode = new TPNode(n, k, m, k-2, m-1, "KnightMove (" + m + "," + k + ")-(" + (m-1) + "," + (k-2) + ")" );
								processNode(newNode);
							}
							

							//1< 2v 
							if(k > 0 && m < TP_Y -3 && n.state[k-1][m+2] != 0){
								TPNode newNode = new TPNode(n, k, m, k-1, m+2, "KnightMove (" + m + "," + k + ")-(" + (m+2) + "," + (k-1) + ")" );
								processNode(newNode);
							}
							
							//1> 2v 
							if(k < TP_X -2 && m < TP_Y -3 && n.state[k+1][m+2] != 0){
								TPNode newNode = new TPNode(n, k, m, k+1, m+2, "KnightMove (" + m + "," + k + ")-(" + (m+2) + "," + (k+1) + ")" );
								processNode(newNode);
							}
							
							//2> 1v 
							if(k < TP_X -3 && m < TP_Y - 2 && n.state[k+2][m+1] != 0){
								TPNode newNode = new TPNode(n, k, m, k+2, m+1, "KnightMove (" + m + "," + k + ")-(" + (m+1) + "," + (k+2) + ")" );
								processNode(newNode);
							}
							
							//2< 1v 
							if(k > 1 && m < TP_Y - 2 && n.state[k-2][m+1] != 0){
								TPNode newNode = new TPNode(n, k, m, k-2, m+1, "KnightMove (" + m + "," + k + ")-(" + (m+1) + "," + (k-2) + ")" );
								processNode(newNode);
							}					
							
						}
					} ////// End of exploring knight moves
		
		//*/look at diagonals
		
		if( i < TP_X-1 && j != 0){
			TPNode newNode = new TPNode(n, i, j, i+1, j-1, "Down-Left");
			processNode(newNode);
		}
		
		if( i < TP_X-1 && j < TP_Y-1){
			TPNode newNode = new TPNode(n, i, j, i+1, j+1, "Down-Right");
			processNode(newNode);		
		}
		
		if( i != 0 && j < TP_Y-1){
			TPNode newNode = new TPNode(n, i, j, i-1, j+1, "Up-Right");
			processNode(newNode);			
		}
		
		if( i != 0 && j < 0){
			TPNode newNode = new TPNode(n, i, j, i-1, j-1, "Up-Left");
			processNode(newNode);			
		} /// end of diagonals
		
		//Up-down-left-right
		
		if( i != 0){
			TPNode newNode = new TPNode(n, i, j, i-1, j, "Up");
			processNode(newNode);			
		}
		
		if( j != 0){
			TPNode newNode = new TPNode(n, i, j, i, j-1, "Left");
			processNode(newNode);			
		}
		
		if( j < TP_Y-1){
			TPNode newNode = new TPNode(n, i, j, i, j+1, "Right");
			processNode(newNode);			
		}
		
		if( i < TP_X-1){
			TPNode newNode = new TPNode(n, i, j, i+1, j, "Down");
			processNode(newNode);			
		} // */ // end of up-down-left-right 
		
		
			
		}
		
		
	}
	
	public void processNode(TPNode newNode){
		//look for a win
		/*if(newNode.isWinner()){
			handleDone(newNode);
		}*/
		
		if(!statesExplored.keySet().contains(newNode.hashVal())){
			statesExplored.put(newNode.hashVal(), newNode);
			nodesExplored++;
			//fringe is a linked list
			if(search == 0)	{
				fringe.add(0, newNode); //the linked list is being used as a stack
			} else if(search == 1){
				fringe.add(newNode); //the linked list is being used as a queue
			} else{
				heuristicFringe.add(newNode);
			}
		} else if(search > 1){
			updateExploredNodeDepth(newNode);
		}
	}
	
	public static void handleDone(TPNode n){
		System.out.println("All done!!");
		System.out.println("----------\nPath: ");
		ArrayList<String> path = new ArrayList<String>();
		while(n != null){
			path.add(0,n.toString());
			//n.heuristicMovesLeft();
			n = n.parent;
		}		
		for(String s: path) System.out.println(s);
		
		solved = true;
		
		System.out.println("----------\nPeak fringe size: " + peakFringeSize);
		System.out.println("Nodes explored: " + nodesExplored);
		if(search == 0) System.out.println("Search method: DFS");
		else if(search == 1) System.out.println("Search method: BFS");
		else if(search == 2) System.out.println("Search method: A* tiles out of place heuristic");
		else if(search == 3) System.out.println("Search method: A* remaining moves heuristic");
		else if(search == 4) System.out.println("Search method: Average heuristic");

		
		System.exit(0);
	}
	
	////Need to reorder both the fringe and explored nodes.
	//Have: Potential new parent and child
	//If the child is in the fringe:
	//	- If the new parent's depth is less than the existing parent
	//		Change the child's parent pointer to the new parent
	//	Next:
	//		{Search the fringe for nodes whose parent si the child
	//			If found, remove from fringe
	//			Set the depth to the parent's depth +1
	//			Add to the fringe
	//			Recurse}
	//	Next:
	//		Search the explored nodes for the child 
	//		If the child is in the explored:
	//			- If the new parent is less than the existing parent:
	//				- change the child's parent to the new parent
	//			{- Search explored for nodes whose parent is the child
	//				- If found, set the depth to the parent's depth +1
	//				- recurse
	//					- if not, call the recursive fringe search above}				

	//New node is created - find its copy in StatesExplored
	//Note - the new node will get garbage collected after this function returns
	public void updateExploredNodeDepth(TPNode newNode){
		TPNode existingCopyOfN = statesExplored.get(newNode.hashVal());
		if(existingCopyOfN.depth > newNode.depth){
			existingCopyOfN.parent = newNode.parent;
			existingCopyOfN.depth = newNode.depth;
			if(!existingCopyOfN.isClosed) {
				//heuristicFringe.remove(existingCopyOfN);				
				heuristicFringe.add(existingCopyOfN);
			}
			updateChildren(existingCopyOfN);

		}
	}
	
	//parent pointers are all right -- recursively update depths of all children in explored
	//then update depth of children in fringe
	//then reorder fringe
	public void updateChildren(TPNode parent){
		//update the explored nodes
		for(TPNode n: statesExplored.values()){
			if(n.parent.equals(parent)){
				n.depth = parent.depth +1;
				updateChildren(n);
				if(!n.isClosed) heuristicFringe.add(n);
			}
		}
		
	}
	
	public void handleFailure(){
		System.out.println("Cannot solve problem.");
		System.out.println("Peak fringe size: " + peakFringeSize);
		System.out.println("Nodes explored: " + nodesExplored);
		System.exit(0);
	}
	
	
}
