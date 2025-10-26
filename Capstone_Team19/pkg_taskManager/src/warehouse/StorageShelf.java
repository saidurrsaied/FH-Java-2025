package warehouse;

public class StorageShelf  implements Locatable {
    private final String shelfID;
    private final WarehousePosition location;
    private boolean isAvailable;
    private Product storedProduct;

    public StorageShelf(String shelfID, WarehousePosition location) {
        this.shelfID = shelfID;
        this.location = location;
        this.isAvailable = true;
        this.storedProduct = null;
    }


    public WarehousePosition getLocation() { return location; }
    public boolean isAvailable() { return isAvailable; }
    public String getShelfID() { return shelfID; }
    public void makeOccupied() { this.isAvailable = false; }
    public void makeAvailable () { this.isAvailable = true; }
    public Product getStoredProduct() { return storedProduct; }
    public void setStoredProduct(Product product) { storedProduct = product; }


}
