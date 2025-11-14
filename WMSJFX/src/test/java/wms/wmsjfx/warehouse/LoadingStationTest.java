package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.equipmentManager.ObjectState;

class LoadingStationTest {

    @Test
    @DisplayName("LoadingStation acquire/release toggles state and is exclusive")
    void acquireRelease() throws InterruptedException {
        LoadingStation ls = new LoadingStation("L1", 4, 0, WahouseObjectType.LoadingStation);

        assertEquals(ObjectState.FREE.toString(), ls.getState());

        ls.acquire();
        assertEquals(ObjectState.BUSY.toString(), ls.getState());

        ls.release();
        assertEquals(ObjectState.FREE.toString(), ls.getState());
    }
}
