package wms.wmsjfx.warehouse;

import wms.wmsjfx.warehouse.exceptions.InventoryException;

public class InventoryItem {
    private final Product product;
    private final StorageShelf shelf;
    private int availableQuantity;


    public InventoryItem(Product product, int quantity, StorageShelf shelf) {
        this.product = product;
        this.availableQuantity = quantity;
        this.shelf = shelf;
    }

    //getInventoryItem(pid).getProduct().getProductName()

    public Product getProduct() { return product; }
    public StorageShelf getShelf() { return shelf; }
    public int getQuantity() { return availableQuantity; }

    public void addQuantity(int amount) { this.availableQuantity += amount; }
    public void removeQuantity(int amount) {
        if (amount > availableQuantity)
            throw new InventoryException("Not enough quantity in stock for " + product.getProductID()
                    + ": requested " + amount + ", available " + availableQuantity);
        this.availableQuantity -= amount;
    }

    @Override
    public String toString() {
        return product.getProductName() + " : available" + availableQuantity + " pcs on " + shelf.getId();
    }
}
