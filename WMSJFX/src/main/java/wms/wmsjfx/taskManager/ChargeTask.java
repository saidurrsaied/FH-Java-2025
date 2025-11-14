package wms.wmsjfx.taskManager;

import java.awt.Point;
import java.util.List;

import wms.wmsjfx.equipmentManager.ChargingStation;
import wms.wmsjfx.equipmentManager.EquipmentManager;
import wms.wmsjfx.equipmentManager.Robot;

public class ChargeTask implements Task{
	private final ChargingStation chargingStation;
	private final String ID;
	private final TaskType taskType = TaskType.CHARGE_ROBOT;
	
	public ChargeTask(ChargingStation chargeStation, String robotId) {
		this.chargingStation = chargeStation;
		this.ID = "Charge-" + robotId;
	}
	
	@Override
	public String getID() {
		return this.ID;
	}

	@Override
	public String getDescription() {
		return String.format("Task: Move to (%.1f, %.1f) and charge.", 
				chargingStation.getLocation().getX(), 
				chargingStation.getLocation().getY());
	}

	@Override
	public TaskType getType() {
		return taskType;
	}

	@Override
	public void execute(Robot robot, EquipmentManager manager) throws InterruptedException {
		System.out.printf("[%s] Executing %s...%n", robot.getID(), this.ID);
		List<Point> steps = manager.requestPath(robot, chargingStation.getLocation());
		robot.stepMove(steps);
		robot.charge();
		// Release charging station
		manager.releaseChargeStation(chargingStation);
	}
}
