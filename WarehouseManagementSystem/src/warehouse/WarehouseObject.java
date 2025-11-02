package warehouse;
import java.awt.Rectangle;
import java.awt.Point;

public abstract class WarehouseObject {
    private final String id;
    private Point location;



    public WarehouseObject(String id, int x, int y) {
        this.id = id;
        this.location = new Point(x, y);

    }

    public String getId() { return id; }
    public Point getLocation() { return location; }
    public void updateLocation(Point newLocation) { this.location = newLocation; }
    public abstract String toString();
}

