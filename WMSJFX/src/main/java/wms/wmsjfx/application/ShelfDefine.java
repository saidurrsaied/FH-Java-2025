package wms.wmsjfx.application;

import java.util.HashMap;
import java.util.Map;

public class ShelfDefine {

    // Define a map of points with their positions
    private Map<String, Position> shelf = new HashMap<>();
    private Map<String, Position> point = new HashMap<>();

    // Constructor to initialize points
    public ShelfDefine() {
    		updatemapShelf();
    		updatemapPoint();
    }
    
    // Constructor to initialize points
    public void updatemapShelf() {
    		shelf.put("Shelf1", new Position(150, 150));
    		shelf.put("Shelf2", new Position(150, 350));
    		shelf.put("Shelf3", new Position(150, 550));
    		shelf.put("Shelf4", new Position(400, 150));
    		shelf.put("Shelf5", new Position(400, 350));
    		shelf.put("Shelf6", new Position(400, 550));
        shelf.put("Shelf7", new Position(650, 150));
        shelf.put("Shelf8", new Position(650, 350));
        shelf.put("Shelf9", new Position(650, 550));
    }
    
    // Constructor to initialize points
    public void updatemapPoint() {
    		point.put("A", new Position(200, 150));
    		point.put("B", new Position(200, 350));
    		point.put("C", new Position(200, 550));
    		point.put("D", new Position(450, 150));
    		point.put("E", new Position(450, 350));
		point.put("F", new Position(450, 550));
		point.put("G", new Position(700, 150));
		point.put("H", new Position(700, 350));
		point.put("I", new Position(700, 550));
		
		point.put("ab", new Position(200, 230));
		point.put("de", new Position(450, 230));
		point.put("gh", new Position(700, 230));
		point.put("bc", new Position(200, 430));
		point.put("ef", new Position(450, 430));
		point.put("hi", new Position(700, 430));
    }
}
