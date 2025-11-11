package pathFinding;
import java.awt.Point;

public class Node {
	public NodeType nodeType;
    public boolean walkable;
    public Point position;
    public Node parent;
    public int gCost;
    public int hCost;
     
	public Node(NodeType nodeType, boolean walkable, Point position) {
		super();
		this.nodeType = nodeType;
		this.walkable = walkable;
		this.position = position;
	}
	
	public int fCost() {
		return gCost + hCost;
	}
	
	public boolean getWalkable() {
		return walkable;
	}
}
