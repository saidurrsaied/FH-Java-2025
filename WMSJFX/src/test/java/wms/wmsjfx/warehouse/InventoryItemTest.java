package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.warehouse.exceptions.InventoryException;

class InventoryItemTest {

    private Product product(String name, String id) {
        return new Product(name, id);
    }

    private StorageShelf shelf(String id, int x, int y) {
        return new StorageShelf(id, x, y, WahouseObjectType.StorageShelf);
    }

    @Test
    @DisplayName("InventoryItem add/remove quantity and toString")
    void addRemoveQuantity_andToString() {
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 1, 2);
        InventoryItem item = new InventoryItem(p, 5, s);

        assertEquals(5, item.getQuantity());
        item.addQuantity(3);
        assertEquals(8, item.getQuantity());

        item.removeQuantity(4);
        assertEquals(4, item.getQuantity());

        assertThrows(InventoryException.class, () -> item.removeQuantity(10));

        String str = item.toString();
        assertTrue(str.contains("Widget"));
        assertTrue(str.contains("S1"));
    }
}
