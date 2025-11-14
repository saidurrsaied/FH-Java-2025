package wms.wmsjfx.equipmentManager;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.pathFinding.PathFinding;
import wms.wmsjfx.taskManager.Task;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseManager;

/**
 * EquipmentManager charger allocation edge cases.
 * These tests avoid starting Robot threads by creating a WarehouseManager with 0 robots.
 */
class EquipmentManagerContentionTest {

    private EquipmentManager newEMWithEmptyFloor() {
        WarehouseManager wm = new WarehouseManager(6, 6);
        // No robots, no charging stations added to floor
        BlockingQueue<Task> sharedQueue = new ArrayBlockingQueue<>(32);
        PathFinding path = new PathFinding(wm);
        return new EquipmentManager(wm, sharedQueue, path);
    }

    @Test
    @DisplayName("requestAvailableChargingStation returns null when no stations exist (bounded wait)")
    void requestCharge_noStations_returnsNull() throws Exception {
        EquipmentManager em = newEMWithEmptyFloor();

        long timeoutSec = 0; // immediate poll
        assertNull(em.requestAvailableChargingStation(timeoutSec));

        timeoutSec = 1; // bounded wait still returns null
        assertNull(em.requestAvailableChargingStation(timeoutSec));
    }

    @Test
    @DisplayName("Two robots request charge but no stations exist -> EM keeps robots available (no assignment)")
    void multipleRobots_requestCharge_noStations() {
        EquipmentManager em = newEMWithEmptyFloor();

        // Create two robots not registered on the floor to avoid EM auto-thread start
        Robot r1 = new Robot("R1", new java.awt.Point(0,0), null, WahouseObjectType.Robot);
        Robot r2 = new Robot("R2", new java.awt.Point(1,1), null, WahouseObjectType.Robot);
        r1.setEquipmentManager(em);
        r2.setEquipmentManager(em);

        // Make them "request" charge while idle
        em.idleRobotRequestsCharge(r1);
        em.idleRobotRequestsCharge(r2);

        // With no stations on the floor, EM adds them back to availableRobots list
        assertTrue(em.getRobot().contains(r1));
        assertTrue(em.getRobot().contains(r2));
    }

    @Test
    @DisplayName("One charging station: first request succeeds, second waits (null) until release, then succeeds")
    void oneStation_twoRequests_exclusiveThenRelease() throws Exception {
        // Arrange a floor with exactly one charging station
        WarehouseManager wm = new WarehouseManager(6, 6);
        ChargingStation cs = new ChargingStation("C1", 1, 1, WahouseObjectType.ChargingStation);
        wm.addObjectToFloor(cs);

        BlockingQueue<Task> sharedQueue = new ArrayBlockingQueue<>(32);
        PathFinding path = new PathFinding(wm);
        EquipmentManager em = new EquipmentManager(wm, sharedQueue, path);

        // Act: First request should get the only station immediately
        ChargingStation got1 = em.requestAvailableChargingStation(0);
        assertNotNull(got1, "First request should obtain the station");

        // Second immediate request should return null (none available now)
        ChargingStation got2 = em.requestAvailableChargingStation(0);
        assertNull(got2, "Second request should not obtain a station before release");

        // Release station back to EM pool
        em.releaseChargeStation(got1);

        // Now a subsequent request should obtain it again within a short timeout
        ChargingStation got3 = em.requestAvailableChargingStation(1);
        assertNotNull(got3, "Station should be available after release");
        assertEquals("C1", got3.getId());
    }
}
