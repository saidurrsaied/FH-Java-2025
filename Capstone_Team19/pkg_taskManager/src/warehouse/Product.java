package warehouse;

public class Product implements Locatable {
    private final String productName;
    private WarehousePosition productLocation;
    private int availableQuantity;


    public Product(String productName, WarehousePosition productLocation, int initialStock) {
        this.productName = productName;
        this.productLocation = productLocation;
        this.availableQuantity = initialStock;
    }


    public WarehousePosition getLocation() { return productLocation; }
    public boolean isAvailable() { return availableQuantity > 0; }
    public String getProductName() { return productName; }
    public void setProductLocation(WarehousePosition newLocation) { productLocation = newLocation; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void addStock(int stockQuantity) { availableQuantity += stockQuantity; }
    public void removeStock(int quantity) { availableQuantity -= quantity; }


    @Override
    public String toString() {
        return productName + " at " + productLocation + " (" + availableQuantity + ")";
    }

}
