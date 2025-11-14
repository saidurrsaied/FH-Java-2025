package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.equipmentManager.ObjectState;

class PackingStationTest {

    @Test
    @DisplayName("PackingStation getters, toString, and state transitions")
    void basicsAndState() {
        PackingStation ps = new PackingStation("P1", 0, 4, WahouseObjectType.PackingStation);

        assertEquals("P1", ps.getId());
        assertEquals(new Point(0, 4), ps.getLocation());

        String s = ps.toString();
        assertTrue(s.contains("P1"));

        ps.setState(ObjectState.BUSY);
        // No getter for state, but ensure it doesn't throw and toString still valid
        assertTrue(ps.toString().contains("PackingStation"));

        ps.setState(ObjectState.FREE);
        assertTrue(ps.toString().contains("PackingStation"));
    }
}
