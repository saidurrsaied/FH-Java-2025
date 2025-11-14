package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("Product getters and toString")
    void productBasics() {
        Product p = new Product("Widget", "P1");
        assertEquals("Widget", p.getProductName());
        assertEquals("P1", p.getProductID());
        assertTrue(p.toString().contains("Widget"));
        assertTrue(p.toString().contains("P1"));
    }
}
