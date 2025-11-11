package pathFinding;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class WarehouseMap {
    public Node nodeArray[][];
    public int mapSizeX;
    public int mapSizeY;
    
    public WarehouseMap(int mapSizeX, int mapSizeY) {
    	this.mapSizeX = mapSizeX;
    	this.mapSizeY = mapSizeY;
    	nodeArray = new Node[mapSizeX][mapSizeY];
    }
    
    public void addWarehouseObject(NodeType nodeType, boolean walkable, Point position) {
    	Node newNode = new Node(nodeType, walkable, position);
    	nodeArray[position.x][position.y] = newNode;
    }
    
    public Node getWarehouseObject(Point position) {
    	return nodeArray[position.x][position.y];
    }
    
    public void showMap() {
    	// 1. Print map contents and Y-axis (row) headers
        // Loop 'y' backwards (from mapSizeY - 1 down to 0) so Y-axis '0' is at the bottom.
    	for (int y = mapSizeY - 1; y >= 0; y--) { 
    		
    		// Print the Y-axis header (e.g., "9 | ", "8 | ")
    		System.out.print(String.format("%-2d| ", y)); 

    		// Loop through each column (x) for the current row (y)
    		for (int x = 0; x < mapSizeX; x++) {
    			Node currentNode = nodeArray[x][y];
    			
    			if (currentNode == null) {
    				System.out.print("? "); // '?' for uninitialized/null cells
    			} else {
    				// Use a switch to print the correct character based on the Node's type
    				switch (currentNode.nodeType) {
    					case Robot:
    						System.out.print("R ");
    						break;
    					case PackingStation:
    						System.out.print("P ");
    						break;
    					case Shelf:
    						System.out.print("S ");
    						break;
    					case ChargingStation:
    						System.out.print("C ");
    						break;
    					case None:
    					default:
    						System.out.print(". "); // '.' for default paths (NodeType.None)
    						break;
    				}
    			}
    		}
    		System.out.println(); // Newline at the end of the row
    	}

    	// 2. Print the horizontal separator line
    	System.out.print("   -"); // 3-space padding to align with Y-axis
    	for (int x = 0; x < mapSizeX; x++) {
    		System.out.print("--"); // Two dashes per column
    	}
    	System.out.println();

    	// 3. Print the X-axis (column) headers
    	System.out.print("   "); // 3-space padding
    	for (int x = 0; x < mapSizeX; x++) {
    		System.out.print(String.format("%-2d", x)); // Print column index (e.g., "0 ", "1 ")
    	}
    	System.out.println();
    }
    
    public List<Node> getNeighbors(Node currentNode) {
    	List<Node> neighborsList = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }

                int checkX = currentNode.position.x + x;
                int checkY = currentNode.position.y + y;

                if (checkX >= 0 && checkX < mapSizeX && checkY >= 0 && checkY < mapSizeY) {
                    neighborsList.add(nodeArray[checkX][checkY]);
                }
            }
        }
        return neighborsList;
    }
 }
