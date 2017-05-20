package BridgeProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

public class BridgeProblem {
	public ArrayList<BridgeNode> fringe;
	public ArrayList<BridgeNode> buffer;
	public PriorityQueue<BridgeNode> heuristicFringe;
	public HashMap<Integer, BridgeNode> statesExplored;
	public static ArrayList<Integer> crossingTimes;
	public BridgeNode root;
	public static boolean solved;
	public static int nodesExplored;
	public static int peakFringeSize;
	public static int search;
	static String usage = "Usage: BP [-b -d -h1 -h2 -a] -r: six people with random crossing times; b = BFS d = DFS\n"
			+ "BP [-b -d] a1,a2,a3,a4,a5,a6,a7,a8: board with a1-n, "
			+ "\nb = BFS, d = DFS, h1 = crossings left heuristic, h2 = time left heuristic a = average heuristic";
	
	public Comparator<BridgeNode> heurMoves = ((n1, n2) -> n1.heuristicCrossingsLeft().compareTo(n2.heuristicCrossingsLeft())); 
	public Comparator<BridgeNode> heurTimes = ((n1, n2) -> n1.heuristicWeightLeft().compareTo(n2.heuristicWeightLeft()));
	public Comparator<BridgeNode> heurAvg = ((n1, n2) -> n1.heuristicAvg().compareTo(n2.heuristicAvg())); 
		
	public static ArrayList<Integer> getDemo1(){
		ArrayList<Integer> demo1 = new ArrayList<Integer>();
		demo1.add(1);
		demo1.add(2);
		demo1.add(5);
		demo1.add(8);
		demo1.add(13);
		demo1.add(11);
		
		return demo1;
	}
	
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println(usage);
			System.exit(-1);
		}
		
		if(args[0].equals("-b")) search = 1; 
		else if(args[0].equals("-d")) search = 0;
		else if(args[0].equals("-h1")) search = 2;
		else if(args[0].equals("-h2")) search = 3;
		else if(args[0].equals("-a")) search = 4;
		else {
			System.out.println(usage);
			System.exit(-1);
		}
		
		BridgeProblem theProblem = new BridgeProblem();
		

		
		crossingTimes = new ArrayList<Integer>();
		if(args[1].equals("-r")){
			for(int i = 0; i < 6; ++i) {
				long seed = System.nanoTime();
				crossingTimes.add(new Random(seed).nextInt(20) +1);
			}
		} else {
			try{
				String[] pieces = args[1].split(",");
				for(String s: pieces)
						crossingTimes.add(Integer.parseInt(s));
				}				
			catch(NumberFormatException e){
				System.out.println(usage);
				System.exit(-1);
			}
		}
		Collections.sort(crossingTimes);
		
		theProblem.run();
	}
	
	public BridgeProblem(){
		solved = false;
		fringe = new ArrayList<BridgeNode>();
		buffer = new ArrayList<BridgeNode>();
		if(search == 2)	heuristicFringe = new PriorityQueue<BridgeNode>(heurMoves);
		else if(search == 3) heuristicFringe = new PriorityQueue<BridgeNode>(heurTimes);
		else if(search == 4) heuristicFringe = new PriorityQueue<BridgeNode>(heurAvg);
		//else heuristicFringe = new PriorityQueue<BridgeNode>();
		statesExplored = new HashMap<Integer, BridgeNode>();
		nodesExplored = 0;
		peakFringeSize = 0;
	}
	
	public void run(){
		root = new BridgeNode(crossingTimes.size());
		if(search == 0 || search == 1) fringe.add(root);
		else heuristicFringe.add(root);
		nodesExplored++;
		
		if(search == 0 || search == 1){
			while(!fringe.isEmpty() && !solved){
				if(fringe.size() > peakFringeSize) peakFringeSize = fringe.size();
				explore(fringe.remove(0));
			}
		} else{
			while(!heuristicFringe.isEmpty() && !solved){
				if(heuristicFringe.size() > peakFringeSize) peakFringeSize = heuristicFringe.size();
				BridgeNode node = heuristicFringe.poll();
				if(!node.closed) explore(node);
			}
		}
		
		handleFailure();		
	}
	
	public void explore(BridgeNode b){
		
		if(b.isWinner()) handleDone(b);
		
		b.closed = true;
		
		System.out.println("......\nExploring: " + b.hashVal());
		
		System.out.println("Remaining fringe is:\n......");
		for(BridgeNode bb: fringe){
			System.out.println(bb.hashVal());
		}
		
		if(search > 1) {
			for(BridgeNode bb: heuristicFringe){
				System.out.println(bb.hashVal());
			}
		}
		
		
		//any single person on the side of the river with the torch can cross
		for(int i = 0; i < b.persons.size(); ++i){
			
			//can only cross if on same side as torch
			if(b.persons.get(i) != b.torch) continue;
			
			BridgeNode newState = new BridgeNode(i, b);
			processNode(newState);

		}
		
		//explore the double crossings
		//any two persons both on the side of the river with the torch can cross
		for(int i = 0; i < b.persons.size() -2; ++i){
			for(int j = i + 1; j < b.persons.size() -1 ; ++j ){
				
				//can only cross if on same side as torch
				if(b.persons.get(i) != b.torch || b.persons.get(j) != b.torch) continue;
				
				BridgeNode newState = new BridgeNode(i, j, b);
				processNode(newState);
			}
		}
		
		//make a new node for each non-terminating state
		//enqueue the node in nodesTBD	
	}
	
	public void processNode(BridgeNode b){
		//make sure no duplicate state is created - check against fringe
		if(!statesExplored.keySet().contains(b.hashVal())){
			if(search == 0){ fringe.add(0, b);} //DFS	
			else if (search == 1){ fringe.add(b);} //BFS
			else heuristicFringe.add(b); //A* search
			//System.out.println(((search == 0)? "Push -- " : "Enqueue: -- ") + newState.hashVal());
			nodesExplored++;
			statesExplored.put(b.hashVal(),b);
		} else if(search > 1) {			
			updateExploredNodeDepth(b);
		}
	}
	
	public void updateExploredNodeDepth(BridgeNode newNode){
		BridgeNode existingCopyOfN = statesExplored.get(newNode.hashVal());
		if(existingCopyOfN.depth > newNode.depth){
			existingCopyOfN.parent = newNode.parent;
			existingCopyOfN.depth = newNode.depth;
			//buffer.add(existingCopyOfN);
			if(!existingCopyOfN.closed) heuristicFringe.add(existingCopyOfN);
			updateChildren(existingCopyOfN);
		}
		
		/*for(BridgeNode m: buffer){
			//heuristicFringe.remove(m);
			//m.isClosed = true;
			heuristicFringe.add(m);			
		}
		
		buffer.clear();*/
	}
	
	//parent pointers are all right -- recursively update depths of all children in explored
	//then update depth of children in fringe
	//then reorder fringe
	public void updateChildren(BridgeNode parent){
		//update the explored nodes
		for(BridgeNode n: statesExplored.values()){
			if(n.parent.equals(parent)){
				n.depth = parent.depth +1;
				updateChildren(n);
				if(!n.closed) heuristicFringe.add(n);
			}
		}
		
		/*for(TPNode n: heuristicFringe){
			if(n.parent.equals(parent)){
				n.depth = parent.depth + 1;
				buffer.add(n);
				//heuristicFringe.remove(n);
				updateChildren(n);				
			}
		}*/
	}
	
	
	public static void handleDone(BridgeNode b){
		System.out.println("All done!!");
		System.out.println("----------\nPeak fringe size: " + peakFringeSize);
		System.out.println("Nodes explored: " + nodesExplored);
		if(search == 0) System.out.println("Search method: DFS");
		else if(search == 1) System.out.println("Search method: BFS");
		else if(search == 2) System.out.println("Search method: A*, Number of crossings heuristic");
		else if(search == 3) System.out.println("Search method: A*, Crossing time heuristic");
		else System.out.println("Search method: average heuristic");
		for(int i = 0; i < crossingTimes.size(); ++i) System.out.printf("Person %d: %d ", i, crossingTimes.get(i));
		System.out.println("\n----------\nPath: ");
		int totalTime = 0;
		while(b != null){
			totalTime += b.pathCost;
			System.out.println(b);
			b = b.parent;
		}
		System.out.println("----------\nTotal crossing time: " + totalTime);
		solved = true;
		
		System.exit(0);
	}
	
	public void handleFailure(){
		System.out.println("Cannot solve problem.");
		System.out.println("Peak fringe size: " + peakFringeSize);
		System.out.println("Nodes explored: " + nodesExplored);
		System.exit(0);
	}

}
