package warehouse;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Rectangle;
import warehouse.datamanager.InventoryDataPacket;
import warehouse.exceptions.InventoryException;


/***
 * Methods:
 *  addProduct: Add a product to the inventory
 *  removeProduct: Remove a product from the inventory
 *  getAllItems: Get all items in the inventory
 *  geInventoryItem: Get a specific item from the inventory
 *  getProductQuantity: Get the quantity of a product in the inventory
 *  increaseProductQuantity: Increase the quantity of a product in the inventory
 *  decreaseProductQuantity: Decrease the quantity of a product in the inventory
 *  isProductInStock: Check if a product is in stock
 *
 */


public class Inventory {

    /**
     *  Product inventory:
     *  Key: Product ID
     *  Value: InventoryItem
     **/
    private final Map<String, InventoryItem> productInventory = new HashMap<>();

    public void addProduct(Product product, int quantity, StorageShelf shelf) {

        /* Only one product can be stored on a shelf at a time */
        if (!shelf.isAvailable() && !shelf.getStoredProduct().getProductID().equals(product.getProductID())) {
            throw new InventoryException("Shelf " + shelf.getId() + " is occupied by a different product");
        }

        /*Increase product quantity if the product is already in the inventory */
        if(productInventory.containsKey(product.getProductID())) {
            productInventory.get(product.getProductID()).addQuantity(quantity);
        }

        /* Add a new product to inventory if it doesn't exist yet */
        else {
            productInventory.put(product.getProductID(), new InventoryItem(product, quantity, shelf));
            shelf.makeOccupied();
            shelf.setStoredProduct(product);

        }
    }

    public Map<String, InventoryItem> getAllItems() {
        return productInventory;
    }

    public InventoryItem geInventoryItem(String productID) {
        if (!productInventory.containsKey(productID)) {
            throw new InventoryException("Product not found: " + productID);
        }
        else return productInventory.get(productID);
    }

    public void removeProduct(String productID) {
        if (!productInventory.containsKey(productID)) {
            throw new InventoryException("Product not found: " + productID);
        }
        else {
            InventoryItem item = productInventory.get(productID);
            item.getShelf().makeAvailable();
            item.getShelf().setStoredProduct(null);
            productInventory.remove(productID);
        }
    }


    public int getProductQuantity(String productID) {
        if (!productInventory.containsKey(productID)) {
            throw new InventoryException("Product not found: " + productID);
        }
        else {
            return productInventory.get(productID).getQuantity();
        }
    }


    public void increaseProductQuantity(String productID, int amount) {
        if (!productInventory.containsKey(productID)) {
            throw new InventoryException("Product not found: " + productID);
        }
        else {
            productInventory.get(productID).addQuantity(amount);
        }
    }
    public void decreaseProductQuantity(String productID, int amount) {
        if (!productInventory.containsKey(productID)) {
            throw new InventoryException("Product not found: " + productID);
        }
        else if (productInventory.get(productID).getQuantity() < amount) {
            throw new InventoryException("Not enough quantity in stock for product " + productID + ": requested " + amount);
        }
        else {
            productInventory.get(productID).removeQuantity(amount);
        }
    }

    public boolean isProductInStock(String productID) {
        if (!productInventory.containsKey(productID)) {
            throw new InventoryException("Product not found: " + productID);        }
        else {
            return productInventory.get(productID).getQuantity() > 0;
        }
    }

    /**
     * Method to export the inventory data as a list of InventoryDataPacket objects
    * */
    public List<InventoryDataPacket> exportInventoryData() {
        List<InventoryDataPacket> dataPacket = new ArrayList<>();

        for (InventoryItem item : productInventory.values()) {
            Product product = item.getProduct();
            StorageShelf shelf = item.getShelf();

            dataPacket.add(new InventoryDataPacket(
                    product.getProductID(),
                    product.getProductName(),
                    item.getQuantity(),
                    shelf.getId(),
                    shelf.getLocation().x, shelf.getLocation().y
            ));
        }
        return dataPacket;
    }






}
