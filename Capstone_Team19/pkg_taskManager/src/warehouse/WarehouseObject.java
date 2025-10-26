package warehouse;
import java.awt.Rectangle;
import java.awt.Point;

public abstract class WarehouseObject {
    private final String id;
    private Point location;
    private Rectangle occupiedArea;


    public WarehouseObject(String id, int x, int y, int objectWidth, int objectLength) {
        this.id = id;
        this.location = new Point(x, y);
        this.occupiedArea = new Rectangle(x, y, objectWidth, objectLength);
    }

    public String getId() { return id; }
    public Point getLocation() { return location; }
    public Rectangle getOccupiedArea() {return occupiedArea;}
    public void updateLocation(Point newLocation) { this.location = newLocation; }
    public void updateOccupiedArea(Rectangle newArea) { this.occupiedArea = newArea; }

}
