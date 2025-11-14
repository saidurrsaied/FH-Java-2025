// Language: java
package warehouse.exceptions.ut;

import org.junit.jupiter.api.Test;
import warehouse.exceptions.DataFileException;
import static org.junit.jupiter.api.Assertions.*;

class DataFileExceptionTest {

    @Test
    void messageIsStored() {
        DataFileException ex = new DataFileException("Inventory CSV not found: path");
        assertEquals("Inventory CSV not found: path", ex.getMessage());
    }

    @Test
    void causeIsStored() {
        java.io.IOException cause = new java.io.IOException("disk error");
        DataFileException ex = new DataFileException("Export failed", cause);
        assertSame(cause, ex.getCause());
    }

    @Test
    void isRuntimeException() {
        DataFileException ex = new DataFileException("any");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void canBeThrownAndCaught() {
        try {
            throw new DataFileException("Invalid row 12");
        } catch (DataFileException ex) {
            assertEquals("Invalid row 12", ex.getMessage());
        }
    }

    @Test
    void uncheckedBehaviorCompileTime() {
        DataFileException ex = new DataFileException("unchecked");
        assertNotNull(ex);
    }
}