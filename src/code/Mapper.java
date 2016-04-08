package code;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author Tony Butler-Yeoman
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);
	public static final Color HIGHLIGHT_PATH_COLOUR = new Color(102, 255, 102);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	public static Location origin;
	public static double scale;

	//for A* 
	public static Node startNode;
	public static Node goalNode;
	public static Collection<Segment> shortestPath = new ArrayList<Segment>();
	public static boolean activateAStar = false;

	//for Articulation Points
	public static boolean activateArtPts = false;
	public static Collection<Node> articulationPoints;

	// our data structures.
	private Graph graph;
	private Trie trie;

	@Override
	protected void astar() {
		if(activateAStar){
			activateAStar = false;
			shortestPath = null;
			startNode = null;
			goalNode = null;
			System.out.println("not activated");
		}else{
			activateAStar = true;
			shortestPath = AStar(startNode, goalNode);	
			outputShortestPath();
			System.out.println("activated");
		}
	}

	@Override
	protected void artPts() {
		if(activateArtPts){
			activateArtPts = false;
			startNode = null;
			articulationPoints = null;
		}else{
			activateArtPts = true;
			if(startNode != null){
				artPtsHelper(startNode);
			}
		}

	}

	@Override
	protected void deselect() {
		activateAStar = false;
		shortestPath = null;
		startNode = null;
		goalNode = null;
		articulationPoints = null;
		activateArtPts = false;
	}

	@Override
	protected void redraw(Graphics g) {
		if (graph != null){
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
		}
		if(startNode!=null){
			g.setColor(Color.YELLOW);
			Mapper.startNode.draw(g, getDrawingAreaDimension(), origin, scale);
		}
		if(goalNode!=null){
			g.setColor(Color.RED);
			Mapper.goalNode.draw(g, getDrawingAreaDimension(), origin, scale);
		}
		if(shortestPath != null && activateAStar){
			g.setColor(Color.GREEN);
			for(Segment seg : shortestPath){
				seg.draw(g, origin, scale);
			}
		}
		if(articulationPoints != null && activateArtPts){
			g.setColor(Color.MAGENTA);
			for(Node n: articulationPoints){
				n.draw(g, getDrawingAreaDimension(), origin, scale);
			}
		}

	}

	@Override
	protected void onClick(MouseEvent e) {
		if(startNode==null){
			startNode = findNode(e.getPoint());
			getTextOutputArea().setText(startNode.toString());
			System.out.println("startNode contain");
		}else{
			goalNode = findNode(e.getPoint());
			getTextOutputArea().setText(goalNode.toString());
			System.out.println("goalNode contain");
		}
	}


	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<Road>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<String>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			scaleOrigin(false);
			scale /= ZOOM_FACTOR;
		}
	}


	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	/**
	 * Finding the node when you click on the canvas 
	 * @param the point of the mouse click
	 * @return return the node according to the mouse click
	 */
	private Node findNode(Point point) {
		Location mousePlace = Location.newFromPoint(point, origin, scale);
		Node closest = null;
		double mindist = Double.POSITIVE_INFINITY;
		for(Node node : graph.nodes.values()){
			double dist = mousePlace.distance(node.location);
			if(dist<mindist){
				mindist = dist;
				closest = node;
			}
		}
		return closest;
	}

	/**
	 * Use A* algorithm to find the shortest path between two nodes.
	 * @param startNode	The starting point.
	 * @param goalNode		The finishing point.
	 * @return Collection of segment that makes up the shortest path between two nodes
	 */
	public Collection<Segment> AStar(Node startNode, Node goalNode){
		for(Node node : graph.nodes.values()){
			node.visit(false);
			node.pathFrom(null);
		}
		List<Segment> thePath = new ArrayList<Segment>();
		PriorityQueue<AStar> fringe = new PriorityQueue<AStar>();
		fringe.offer(new AStar(startNode, null, 0, startNode.heuristic(goalNode.getLoc())));

		while(!fringe.isEmpty()){
			AStar searchNode = fringe.poll(); //dequeue
			Node current = searchNode.getStartNode();

			if(!current.visited()) { //if not yet visited 
				current.visit(true);
				current.pathFrom(searchNode.getGoalNode());
				current.cost(searchNode.getCostSoFar());
				System.out.println("visited");
				if(current == goalNode){
					System.out.println("n == goalNode");
					break;
				}
				for(Segment edge : current.getSegment()){
					Node neigh = null;
					if(current == edge.getStartNode()){
						neigh = edge.getEndNode();
					}
					if(current == edge.getEndNode()){
						neigh = edge.getStartNode();
					}
					if(!neigh.visited()){
						double costToNeigh = searchNode.getCostSoFar() + edge.getLength();
						double estTotal = costToNeigh + neigh.heuristic(goalNode.getLoc());
						fringe.offer(new AStar(neigh,current,costToNeigh,estTotal));	
					}
				}
			}
		}
		Node backTrack = goalNode;
		System.out.println("backTrack");
		while(backTrack != startNode ){
			System.out.println("backTrack != startNode");
			for(Segment s : backTrack.getSegment()){
				if(s.getStartNode() == backTrack.getPathFrom() || s.getEndNode() == backTrack.getPathFrom()){
					thePath.add(s);
					backTrack = backTrack.getPathFrom();
				}
			}
		}
		Collections.reverse(thePath);
		return thePath;
	}
	
	/**
	 * Shows the information of the shortest path and the paths that have went through
	 */
	public void outputShortestPath(){
		getTextOutputArea().setText("");
		double totalLength = 0;
		
		if(shortestPath != null){
			for(Segment s: shortestPath){
				Road r = s.getRoad();
				String streetName = r.getLabel();
				totalLength += s.getLength();
				getTextOutputArea().append(streetName+": distance "+ (double)Math.round(s.getLength()*1000)/1000+" km\n");
			}
			getTextOutputArea().append("\nTotal distance of the path "+(double)Math.round(totalLength*1000)/1000+"km");
		}
	}

	/**
	 * Use A* algorithm to find the shortest path between two nodes.
	 * @param start	The start node of the articulation point
	 */
	public void artPtsHelper(Node start){
		for(Node node : graph.nodes.values()){
			node.visit(false);
			node.setDepth(Double.POSITIVE_INFINITY);
		}
		articulationPoints = new HashSet<Node>();
		int numSubtrees = 0;

		start.setDepth(0);
		for(Node neighbour : start.getNeighNode()){
			if(neighbour.getDepth() == Double.POSITIVE_INFINITY){
				//recArtPts(neighbour,1,start);
				iterArtPts(neighbour, start);
				numSubtrees++;
			}
			if(numSubtrees > 1){
				articulationPoints.add(start);
			}
		}
	}
	
	/**
	 * Use A* algorithm to find the shortest path between two nodes.
	 * @param node, Node
	 * @param double,  depth
	 * @param fromNode, Node
	 * @return double, that return the reach back
	 */
	public double recArtPts(Node node, double depth, Node fromNode){
		node.setDepth(depth);
		double reachBack = depth;

		for(Node neigh : node.getNeighNode()){
			if(neigh == fromNode) continue;
			if(neigh.getDepth() < Double.POSITIVE_INFINITY){
				reachBack = Math.min(reachBack, neigh.getDepth());
			}
			else{
				double childReach = recArtPts(neigh, depth+1, node);
				reachBack = Math.min(childReach, reachBack);
				if(childReach >= depth){
					articulationPoints.add(node);
				}
				
			}
		}
		return reachBack;
	}
	
	
	public void iterArtPts(Node firstNode, Node root){
		Stack<ArtPtsDFS> fringe = new Stack<ArtPtsDFS>();
		fringe.push(new ArtPtsDFS(firstNode, 1, new ArtPtsDFS(root, 0, null)));
		
		while(!fringe.isEmpty()){
			ArtPtsDFS elem = fringe.peek();
			Node node = elem.getNode();
			
			if(elem.getChildren() == null){
				node.setDepth(elem.getDepth());
				elem.setReachBack(elem.getDepth());
				elem.setChildren();
				for(Node neigh : node.getNeighNode()){
					if(neigh != elem.getParent().getNode()){
						elem.getChildren().push(neigh);
					}
				}
			}
			else if(!elem.getChildren().isEmpty()){
				Node child = elem.getChildren().pop();
				if(child.getDepth() < Double.POSITIVE_INFINITY){
					elem.setReachBack((double)Math.min(elem.getReachBack(), child.getDepth()));
				}else{
					fringe.push(new ArtPtsDFS(child, node.getDepth()+1, elem));
				}
			}
			else{
				if(node != firstNode){
					if(elem.getReachBack() >= elem.getParent().getDepth()){
						articulationPoints.add(elem.getParent().getNode());
					}
					elem.getParent().setReachBack((double)Math.min(elem.getParent().getReachBack(), elem.getReachBack()));
				}
				fringe.pop();
			}
		}
		
		
		
	}

	public static void main(String[] args) {
		new Mapper();
	}



}

// code for COMP261 assignments