package wms.wmsjfx.equipmentManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.warehouse.WahouseObjectType;

class ChargingStationTest {

    @Test
    @DisplayName("ChargingStation setState/getState toggles between FREE and BUSY")
    void stateTransitions() {
        ChargingStation cs = new ChargingStation("C1", 2, 2, WahouseObjectType.ChargingStation);

        assertEquals(ObjectState.FREE.toString(), cs.getState());

        cs.setState(ObjectState.BUSY);
        assertEquals(ObjectState.BUSY.toString(), cs.getState());

        cs.setState(ObjectState.FREE);
        assertEquals(ObjectState.FREE.toString(), cs.getState());
    }
}
