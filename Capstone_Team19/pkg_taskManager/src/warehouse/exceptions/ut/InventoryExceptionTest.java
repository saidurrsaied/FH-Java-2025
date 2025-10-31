// Language: java
package warehouse.exceptions.ut;


import org.junit.jupiter.api.Test;
import warehouse.exceptions.InventoryException;
import static org.junit.jupiter.api.Assertions.*;

class InventoryExceptionTest {

    @Test
    void messageIsStored() {
        InventoryException ex = new InventoryException("Product not found: P-001");
        assertEquals("Product not found: P-001", ex.getMessage());
    }

    @Test
    void causeIsStored() {
        NumberFormatException cause = new NumberFormatException("bad number");
        InventoryException ex = new InventoryException("Parse failed", cause);
        assertSame(cause, ex.getCause());
    }

    @Test
    void isRuntimeException() {
        InventoryException ex = new InventoryException("any");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void canBeThrownAndCaught() {
        try {
            throw new InventoryException("Not enough quantity");
        } catch (InventoryException ex) {
            assertEquals("Not enough quantity", ex.getMessage());
        }
    }

    @Test
    void uncheckedBehaviorCompileTime() {
        InventoryException ex = new InventoryException("unchecked");
        assertNotNull(ex);
    }
}