package code;

import java.util.ArrayDeque;

public class ArtPtsDFS {
	private Node node;
	private double depth;
	private double reachBack;
	private ArtPtsDFS parent;
	private ArrayDeque<Node> children;
	
	public ArtPtsDFS(Node node, double d, ArtPtsDFS root){
		this.node = node;
		this.depth = d;
		this.parent = root;
	}
	
	public Node getNode(){
		return this.node;
	}
	
	public void setNode(Node n){
		this.node = n;
	}
	
	public double getDepth(){
		return this.depth;
	}
	
	public void setDepth(double d){
		this.depth = d;
	}
	
	public double getReachBack(){
		return this.reachBack;
	}
	
	public void setReachBack(double r){
		this.reachBack = r;
	}
	
	public ArtPtsDFS getParent(){
		return this.parent;
	}
	
	public ArrayDeque<Node> getChildren(){
		return this.children;
	}
	
	public void setChildren(){
		this.children = new ArrayDeque<Node>();
	}
}
