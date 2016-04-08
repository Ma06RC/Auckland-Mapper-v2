package code;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author Tony Butler-Yeoman
 */
public class Node {


	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;

	//For A*
	private boolean visited;
	private Node pathFrom;
	private double cost;

	//For Articulation Points
	private double depth;


	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<Segment>();
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	public Collection<Segment> getSegment(){
		return this.segments;
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}

	public Location getLoc(){
		return this.location;
	}

	public double heuristic(Location otherLoc){
		return this.location.distance(otherLoc);
	}

	public boolean visited(){
		return this.visited;
	}

	public boolean visit(boolean visited){
		return this.visited = visited;
	}

	public void pathFrom(Node from){
		this.pathFrom = from;
	}

	public void cost(double costToHere){
		this.cost = costToHere;
	}

	public Node getPathFrom(){
		return this.pathFrom;
	}

	public double getCost(){
		return this.cost;
	}

	public double getDepth(){
		return this.depth;
	}

	public void setDepth(double d){
		this.depth = d;
	}

	public Collection<Node> getNeighNode(){
		Collection<Node> neigh = new ArrayList<Node>();

		for(Segment s : this.segments){
			neigh.add(s.getStartNode());
		}
		for(Segment s : this.segments){
			neigh.add(s.getEndNode());
		}
		return neigh;
	}
}

// code for COMP261 assignments