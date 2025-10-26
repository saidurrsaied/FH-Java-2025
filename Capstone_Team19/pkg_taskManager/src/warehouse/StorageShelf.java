package warehouse;

import java.awt.*;

public class StorageShelf extends WarehouseObject implements Locatable {

    private boolean isAvailable;
    private Product storedProduct;

    public StorageShelf(String shelfID, int x, int y, int shelfWidth, int shelfLength) {
        super(shelfID, x, y, shelfWidth, shelfLength);
        this.isAvailable = true;
        this.storedProduct = null;
    }



    public boolean isAvailable() { return isAvailable; }
    public void makeOccupied() { this.isAvailable = false; }
    public void makeAvailable () { this.isAvailable = true; }
    public Product getStoredProduct() { return storedProduct; }
    public void setStoredProduct(Product product) { storedProduct = product; }

    @Override
    public String toString() {
        return  "ID:" + super.getId() + (this.isAvailable ? "  is Available" : " is Occupied by ")
                + (this.isAvailable ? "" : this.storedProduct.getProductID())
                + ", located at X: " + super.getOccupiedArea().x + ", Y: " + super.getOccupiedArea().y
                + ", width" + super.getOccupiedArea().width + ", length " + super.getOccupiedArea().height;
    }
}
