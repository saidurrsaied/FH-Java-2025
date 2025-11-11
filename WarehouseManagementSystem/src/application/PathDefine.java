package application;

import java.util.HashMap;
import java.util.Map;

public class PathDefine {

    // Define a map of points with their positions
    private Map<String, Position> shelf = new HashMap<>();
    private double ab, de, gh, bc, ef, hi;
    private double xa, xb, xc, xd, xe, xf, xg, xh, xi;
    private double ya, yb, yc, yd, ye, yf, yg, yh, yi;
   

    // Constructor to initialize points
    public void updatemapPoint() {
		shelf.put("ab", new Position(xa, (ya + yb)/2));
		shelf.put("de", new Position(xd, (yd + yd)/2));
		shelf.put("gh", new Position(xg, (yg + yd)/2));
		shelf.put("bc", new Position(xd, yd));
		shelf.put("ef", new Position(xe, ye));
		shelf.put("hi", new Position(xf, yf));

    }
    
    public void updatemapShelf() {
		shelf.put("A", new Position(xa, ya));
		shelf.put("B", new Position(xb, yb));
		shelf.put("C", new Position(xc, yc));
		shelf.put("D", new Position(xd, yd));
		shelf.put("E", new Position(xe, ye));
		shelf.put("F", new Position(xf, yf));
		shelf.put("G", new Position(xg, yg));
		shelf.put("H", new Position(xh, yh));
    		shelf.put("J", new Position(xi, yi));
    	}
}
