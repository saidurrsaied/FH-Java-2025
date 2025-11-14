package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;
import wms.wmsjfx.warehouse.exceptions.InventoryException;

class InventoryTest {

    private Product product(String name, String id) {
        return new Product(name, id);
    }

    private StorageShelf shelf(String id, int x, int y) {
        return new StorageShelf(id, x, y, WahouseObjectType.StorageShelf);
    }

    @Test
    @DisplayName("addProduct adds new item and occupies shelf")
    void addProduct_newItem() {
        Inventory inv = new Inventory();
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 1, 2);

        inv.addProduct(p, 5, s);

        InventoryItem item = inv.geInventoryItem("P1");
        assertEquals(5, item.getQuantity());
        assertFalse(s.isAvailable());
        assertEquals(p, s.getStoredProduct());
    }

    @Test
    @DisplayName("addProduct on same product increases quantity")
    void addProduct_existingIncreasesQuantity() {
        Inventory inv = new Inventory();
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 1, 2);

        inv.addProduct(p, 5, s);
        inv.addProduct(p, 3, s);

        assertEquals(8, inv.getProductQuantity("P1"));
    }

    @Test
    @DisplayName("addProduct on occupied shelf with different product throws")
    void addProduct_occupiedShelfDifferentProduct_throws() {
        Inventory inv = new Inventory();
        StorageShelf s = shelf("S1", 1, 2);
        Product p1 = product("Widget", "P1");
        Product p2 = product("Gadget", "P2");

        inv.addProduct(p1, 2, s);
        assertThrows(InventoryException.class, () -> inv.addProduct(p2, 1, s));
    }

    @Test
    @DisplayName("removeProduct frees shelf and clears stored product")
    void removeProduct_freesShelf() {
        Inventory inv = new Inventory();
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 1, 2);

        inv.addProduct(p, 5, s);
        inv.removeProduct("P1");

        assertTrue(s.isAvailable());
        assertNull(s.getStoredProduct());
        assertThrows(InventoryException.class, () -> inv.getProductQuantity("P1"));
    }

    @Test
    @DisplayName("increase/decrease quantity and boundary checks")
    void increaseDecreaseQuantity() {
        Inventory inv = new Inventory();
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 1, 2);
        inv.addProduct(p, 5, s);

        inv.increaseProductQuantity("P1", 2);
        assertEquals(7, inv.getProductQuantity("P1"));

        inv.decreaseProductQuantity("P1", 4);
        assertEquals(3, inv.getProductQuantity("P1"));

        assertThrows(InventoryException.class, () -> inv.decreaseProductQuantity("P1", 5));
    }

    @Test
    @DisplayName("isProductInStock true when qty > 0 and throws for missing product")
    void isProductInStock_tests() {
        Inventory inv = new Inventory();
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 1, 2);
        inv.addProduct(p, 1, s);
        assertTrue(inv.isProductInStock("P1"));

        inv.decreaseProductQuantity("P1", 1);
        assertFalse(inv.isProductInStock("P1"));
    }

    @Test
    @DisplayName("exportInventoryData produces expected packet")
    void exportInventoryData_producesPacket() {
        Inventory inv = new Inventory();
        Product p = product("Widget", "P1");
        StorageShelf s = shelf("S1", 3, 4);
        inv.addProduct(p, 9, s);

        List<InventoryDataPacket> packets = inv.exportInventoryData();
        assertEquals(1, packets.size());
        InventoryDataPacket dp = packets.get(0);

        assertEquals("P1", dp.getProductId());
        assertEquals("Widget", dp.getProductName());
        assertEquals(9, dp.getQuantity());
        assertEquals("S1", dp.getShelfId());
        assertEquals(3, dp.getX());
        assertEquals(4, dp.getY());
    }
}
