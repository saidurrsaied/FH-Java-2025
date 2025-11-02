package warehouse.datamanager;


public class InventoryDataPacket {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final String shelfId;
    private final int x;
    private final int y;


    public InventoryDataPacket(String productId, String productName, int quantity,
                               String shelfId, int x, int y) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.shelfId = shelfId;
        this.x = x;
        this.y = y;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getShelfId() { return shelfId; }
    public int getX() { return x; }
    public int getY() { return y; }
}
