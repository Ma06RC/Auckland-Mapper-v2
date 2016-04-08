package code;

public class AStar implements Comparable<AStar> {
	Node startNode;
	Node goalNode;
	double costSoFar;
	double heuristic;

	public AStar(Node s, Node g, double csf, double h){
		this.startNode = s;
		this.goalNode = g;
		this.costSoFar = csf;
		this.heuristic = csf+h;
	}


	public Node getStartNode(){
		return this.startNode;
	}

	public Node getGoalNode(){
		return this.goalNode;
	}

	public double getCostSoFar(){
		return this.costSoFar;
	}

	public double getHeuristic(){
		return this.heuristic;
	}


	@Override
	public int compareTo(AStar other) {
		if(this.getHeuristic() < other.getHeuristic()){
			return -1;
		}else if(this.getHeuristic() > other.getHeuristic()){
			return 1;
		}else{
			return 0;
		}

	}
}
