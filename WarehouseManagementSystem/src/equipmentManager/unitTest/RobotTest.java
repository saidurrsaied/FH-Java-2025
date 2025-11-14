package equipmentManager.unitTest;

import equipmentManager.Robot;
import equipmentManager.RobotState;
import warehouse.WahouseObjectType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple console test suite for Robot primitive behaviors without full EquipmentManager.
 * Uses direct method calls (no threading) to keep deterministic.
 */
public class RobotTest {

	public static void main(String[] args) throws Exception {
		System.out.println("===== ROBOT TESTS =====");
		testMoveConsumesBattery();
		testStepMoveSequence();
		testPickAndDropState();
		testChargeRestoresBattery();
		// Exclude threaded/dummy task execution for simplicity
		System.out.println("===== ALL ROBOT TESTS FINISHED =====");
	}

	// ---------- Assert Helpers ----------
	private static void assertTrue(boolean cond, String msg) {
		System.out.println((cond ? "[PASS] " : "[FAIL] ") + msg);
	}

	private static void assertEquals(Object exp, Object act, String msg) {
		boolean ok = (exp == null && act == null) || (exp != null && exp.equals(act));
		System.out.println((ok ? "[PASS] " : "[FAIL] ") + msg + (ok ? "" : " (expected=" + exp + ", actual=" + act + ")"));
	}

	// ---------- Tests ----------

	private static Robot newRobotAtOrigin() {
		return new Robot("R-TST", new Point(0,0), null, WahouseObjectType.Robot);
	}

	private static void testMoveConsumesBattery() throws Exception {
		Robot r = newRobotAtOrigin();
		double before = r.getBatteryPercentage();
		r.moveTo(new Point(3,4)); // distance 5 -> ceil(5)=5 consumed
		double after = r.getBatteryPercentage();
		assertEquals(95.0, after, "moveTo consumes correct battery (5%)");
		assertEquals(new Point(3,4), r.getCurrentPosition(), "Position updated after move");
		assertTrue(before > after, "Battery decreased after move");
	}

	private static void testStepMoveSequence() throws Exception {
		Robot r = newRobotAtOrigin();
		List<Point> path = new ArrayList<>();
		path.add(new Point(1,0));
		path.add(new Point(2,0));
		path.add(new Point(2,1));
		r.stepMove(path); // three steps, each distance 1, battery -3
		assertEquals(new Point(2,1), r.getCurrentPosition(), "Final position after stepMove");
		assertEquals(97.0, r.getBatteryPercentage(), "Battery consumed over 3 unit steps");
		assertEquals(RobotState.MOVING.toString(), r.getState(), "State MOVING during path");
	}

	private static void testPickAndDropState() throws Exception {
		Robot r = newRobotAtOrigin();
		r.pickUpItem("ITEM-1");
		assertEquals(RobotState.PICKING.toString(), r.getState(), "State is PICKING after pickUpItem");
		r.dropItem("ITEM-1");
		assertEquals(RobotState.PACKING.toString(), r.getState(), "State is PACKING after dropItem");
	}

	private static void testChargeRestoresBattery() throws Exception {
		Robot r = newRobotAtOrigin();
		r.setBatteryPercentage(40.0);
		r.charge();
		assertEquals(100.0, r.getBatteryPercentage(), "Charge restores battery to 100%");
		assertEquals(RobotState.CHARGING.toString(), r.getState(), "State CHARGING during charge");
	}

}
