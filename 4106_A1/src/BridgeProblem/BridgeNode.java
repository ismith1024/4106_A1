package BridgeProblem;

import java.util.ArrayList;

public class BridgeNode {
	///// state
	
	//Represents the N persons:
	//True = has crossed
	//False = has not crossed
	public ArrayList<Boolean> persons;
	//Is the torch on the other side?
	public Boolean torch;
	
	////parent node
	public BridgeNode parent;
	
	////action -- represents the action that brought the problem to this state
	// number = person who crossed
	// positive = crossed
	// negative = came back
	public String action;
	
	////path cost g(x) // the time it took from the parent
	public int pathCost;
	
	////depth g(x) for the crossingsLeft heuristic
	public int depth;
	public boolean closed;

	public BridgeNode(int size){
		persons = new ArrayList<Boolean>();
		for(int i = 0; i < size; ++i){
			persons.add(false);
		}
		torch = false;
		action = "Starting postion";
		pathCost = 0;
		parent = null;
		depth = 0;
		closed = false;
	}
	
	public BridgeNode(int crossed1, int crossed2, BridgeNode par){
		parent = par;
		closed = false;
		persons = new ArrayList<Boolean>();
		for(int i = 0; i < parent.persons.size(); ++i){
			persons.add(parent.persons.get(i));
		}
		//switch the state of the crossing person
		persons.set(crossed1, !persons.get(crossed1));
		persons.set(crossed2, !persons.get(crossed2));		
		torch = !parent.torch;
		pathCost = Math.max(BridgeProblem.crossingTimes.get(crossed1),BridgeProblem.crossingTimes.get(crossed2));
		if(persons.get(crossed1)) action = "Crossing : "; else action = "Returning : ";
		action += crossed1 + ", " + crossed2;
		depth = parent.depth + 1;
		
		boolean done = true;
		for(Boolean v: persons){
			done = done && v;
		}
		
		if(done) BridgeProblem.handleDone(this);
	}
	
	public BridgeNode(int crossed, BridgeNode par){
		parent = par;
		closed = false;
		persons = new ArrayList<Boolean>();
		for(int i = 0; i < parent.persons.size(); ++i){
			persons.add(parent.persons.get(i));
		}
		//switch the state of the crossing person
		persons.set(crossed, !persons.get(crossed));
		torch = !parent.torch;
		pathCost = BridgeProblem.crossingTimes.get(crossed);
		if(persons.get(crossed)) action = "Crossing : "; else action = "Returning : ";
		action += crossed;
		depth = parent.depth + 1;
		
		boolean done = true;
		for(Boolean v: persons){
			done = done && v;
		}
		
		if(done) BridgeProblem.handleDone(this);
	}
	
	public String toString(){
		return "Step: " + depth + " -- " + action + " | Time: " + pathCost;
	}
	
	public Integer hashVal(){
		String rVal = torch ? "1" : "0";
		for(Boolean v: persons) rVal += v? "1":"0";
		try{
			return(Integer.parseInt(rVal));
		} catch(NumberFormatException e){
			return 0;
		}		
	}
	
	public boolean isWinner(){
		boolean win = true;
		for(Boolean b: persons){
			win = win && b;
		}
		return win;
	}
	
	public boolean equals(BridgeNode b){
		for(int i = 0; i < persons.size(); ++i){
			if(persons.get(i) == b.persons.get(i)) return false;	
		} 			
		return true;		
	}
	
	public Integer heuristicCrossingsLeft(){
		float count = 0.0f;
		for(boolean b: persons){count += b? 0.0f:0.5;}
		return pathCost + (int) count;
	}
	
	public Integer heuristicWeightLeft(){
		int sum = 0;
		int tot = 0;
		int count = 0;
		for(boolean b: persons){
			sum += b? 0: BridgeProblem.crossingTimes.get(count);
			tot += BridgeProblem.crossingTimes.get(count);
			++count;
		}			
		return pathCost + (sum/2); //(sum/count);	
	}
	
	public Integer heuristicAvg(){
		return (heuristicWeightLeft() + heuristicCrossingsLeft()) /2;
	}

}
