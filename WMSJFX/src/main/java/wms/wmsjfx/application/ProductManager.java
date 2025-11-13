package wms.wmsjfx.application;

import wms.wmsjfx.warehouse.InventoryItem;
import wms.wmsjfx.warehouse.Product;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import wms.wmsjfx.warehouse.WahouseObjectType;
public class ProductManager {
    
    private ObservableList<InventoryDataPacket> productList = FXCollections.observableArrayList();
    
    // Add a new product or update quantity if the product already exists
    public void addProduct(Product product, int quantity, StorageShelf shelf) {
        InventoryDataPacket existingProduct = findProductById(product.getProductID());
        
        if (existingProduct != null) {
            // If the product exists, update the quantity using the InventoryItem's addQuantity method
            InventoryItem item = findInventoryItemById(existingProduct.getProductId());  // Find the related InventoryItem
            if (item != null) {
                item.addQuantity(quantity);  // Add the quantity
            }
        } else {
            // If the product doesn't exist, create a new InventoryDataPacket and add it
            InventoryItem newItem = new InventoryItem(product, quantity, shelf);
            InventoryDataPacket newProduct = new InventoryDataPacket(product.getProductID(), product.getProductName(), newItem.getQuantity(), shelf.getId(), shelf.getLocation().x, shelf.getLocation().y);
            productList.add(newProduct);
        }
    }
    
    // Find a product by ID
    public InventoryDataPacket findProductById(String productId) {
        for (InventoryDataPacket product : productList) {
            if (product.getProductId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    // Get the current list of products
    public ObservableList<InventoryDataPacket> getProductList() {
        return productList;
    }

    // Find InventoryItem by product ID
    private InventoryItem findInventoryItemById(String productId) {
        for (InventoryDataPacket dataPacket : productList) {
            if (dataPacket.getProductId().equals(productId)) {
                return new InventoryItem(new Product(dataPacket.getProductId(), dataPacket.getProductName()), dataPacket.getQuantity(), new StorageShelf(dataPacket.getShelfId(), dataPacket.getX(), dataPacket.getY(), WahouseObjectType.StorageShelf));
            }
        }
        return null;
    }
}