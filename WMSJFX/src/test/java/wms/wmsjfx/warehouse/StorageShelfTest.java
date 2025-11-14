package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorageShelfTest {

    @Test
    @DisplayName("new shelf is available with no stored product; toggle availability and assign product")
    void shelfAvailabilityAndStoredProduct() {
        StorageShelf shelf = new StorageShelf("S1", 5, 6, WahouseObjectType.StorageShelf);
        assertTrue(shelf.isAvailable());
        assertNull(shelf.getStoredProduct());

        Product p = new Product("Widget", "P1");
        shelf.makeOccupied();
        shelf.setStoredProduct(p);
        assertFalse(shelf.isAvailable());
        assertEquals(p, shelf.getStoredProduct());

        shelf.makeAvailable();
        shelf.setStoredProduct(null);
        assertTrue(shelf.isAvailable());
        assertNull(shelf.getStoredProduct());
    }
}
