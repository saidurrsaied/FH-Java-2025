package warehouse;


import java.util.HashMap;
import java.util.Map;

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
            throw new IllegalStateException("Shelf is occupied!");
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
            throw new IllegalArgumentException("Product not found!");
        }
        else return productInventory.get(productID);
    }

    public void removeProduct(String productID) {
        if (!productInventory.containsKey(productID)) {
            throw new IllegalArgumentException("Product not found!");
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
            throw new IllegalArgumentException("Product not found!");
        }
        else {
            return productInventory.get(productID).getQuantity();
        }
    }


    public void increaseProductQuantity(String productID, int amount) {
        if (!productInventory.containsKey(productID)) {
            throw new IllegalArgumentException("Product not found!");
        }
        else {
            productInventory.get(productID).addQuantity(amount);
        }
    }
    public void decreaseProductQuantity(String productID, int amount) {
        if (!productInventory.containsKey(productID)) {
            throw new IllegalArgumentException("Product not found!");
        }
        else if (productInventory.get(productID).getQuantity() < amount) {
            throw new IllegalArgumentException("Not enough quantity in stock!");
        }
        else {
            productInventory.get(productID).removeQuantity(amount);
        }
    }

    public boolean isProductInStock(String productID) {
        if (!productInventory.containsKey(productID)) {
            throw new IllegalArgumentException("Product not found!");
        }
        else {
            return productInventory.get(productID).getQuantity() > 0;
        }
    }




}
