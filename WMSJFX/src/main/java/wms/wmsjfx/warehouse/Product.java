package wms.wmsjfx.warehouse;

import java.awt.*;

public class Product  {
    private final String productName;
    private final String productID;


    public Product(String productName, String productID) {
        this.productName = productName;
        this.productID = productID;
    }

    public String getProductName() { return productName; }
    public String getProductID() { return productID; }


    @Override
    public String toString() {
        return productName + " with ID: " + productID;
    }

}
