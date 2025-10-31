package warehouse.datamanager;

public class WarehouseDataPacket {
    private final String id;
    private final String type;
    private final int x;
    private final int y;
    private final boolean available;

    public WarehouseDataPacket(String id, String type, int x, int y, boolean available) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.available = available;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isAvailable() { return available; }
}
