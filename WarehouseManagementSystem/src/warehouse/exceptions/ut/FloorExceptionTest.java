// Language: java
package warehouse.exceptions.ut;

import org.junit.jupiter.api.Test;
import warehouse.exceptions.FloorException;
import static org.junit.jupiter.api.Assertions.*;

class FloorExceptionTest {

    @Test
    void messageIsStored() {
        FloorException ex = new FloorException("Shelf not found: SHELF9");
        assertEquals("Shelf not found: SHELF9", ex.getMessage());
    }

    @Test
    void causeIsStored() {
        IllegalArgumentException cause = new IllegalArgumentException("invalid id");
        FloorException ex = new FloorException("Lookup failed", cause);
        assertSame(cause, ex.getCause());
    }

    @Test
    void isRuntimeException() {
        FloorException ex = new FloorException("any");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void canBeThrownAndCaught() {
        try {
            throw new FloorException("Unknown object type");
        } catch (FloorException ex) {
            assertEquals("Unknown object type", ex.getMessage());
        }
    }

    @Test
    void uncheckedBehaviorCompileTime() {
        FloorException ex = new FloorException("unchecked");
        assertNotNull(ex);
    }
}