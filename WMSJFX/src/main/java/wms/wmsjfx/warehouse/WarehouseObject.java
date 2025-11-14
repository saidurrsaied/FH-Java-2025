package wms.wmsjfx.warehouse;
import java.awt.Point;

public abstract class WarehouseObject {
    private final String id;
    private Point location;
    private WahouseObjectType Object_TYPE ;

    public WarehouseObject(String id, int x, int y, WahouseObjectType object_TYPE) {
        this.id = id;
        this.location = new Point(x, y);
        this.Object_TYPE = object_TYPE;

    }

    public String getId() { return id; }
    public Point getLocation() { return location; }
    public void updateLocation(Point newLocation) { this.location = newLocation; }
    public abstract String toString();
    public WahouseObjectType getObjectType() { return Object_TYPE; }
}

