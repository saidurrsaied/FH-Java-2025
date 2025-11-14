package wms.wmsjfx.taskManager;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import wms.wmsjfx.equipmentManager.ChargingStation;
import wms.wmsjfx.equipmentManager.EquipmentManager;
import wms.wmsjfx.equipmentManager.ObjectState;
import wms.wmsjfx.equipmentManager.Robot;
import wms.wmsjfx.equipmentManager.RobotState;

public class GoToChargingStationAndWaitTask implements Task {
    private final ChargingStation targetChargingStation;
    private final TaskType taskType = TaskType.GO_TO_CHARGING_STATION_AND_WAIT;
    private final String ID;

    public GoToChargingStationAndWaitTask(ChargingStation targetChargingStation) {
        super();
        this.targetChargingStation = targetChargingStation;
        this.ID = "GoToChargingStatuinAndWait-";
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDescription() {
        return String.format("Task: Move to (%.1f, %.1f) and waiting for available charging station.",
                targetChargingStation.getLocation().getX(),
                targetChargingStation.getLocation().getY());
    }

    @Override
    public TaskType getType() {
        return taskType;
    }

    @Override
    public void execute(Robot robot, EquipmentManager manager) throws InterruptedException, FindChargeTimeoutException {
        // 1. Ask equipment manager for path finding
        List<Point> steps = manager.requestPath(robot, targetChargingStation.getLocation());
        robot.stepMove(steps);

        // 2. Wait for 15 minutes for available charging station
        robot.setState(RobotState.WAITING_FOR_AVAILABLE_CHARGING_STATION);
        ChargingStation foundChargingStation = manager.requestAvailableChargingStation(15);

        if (foundChargingStation != null) {
            System.out.printf("[charging][%s] Found charging station %s for %s%n", this.getClass().getSimpleName(), foundChargingStation.getId(), robot.getId());
            foundChargingStation.setState(ObjectState.BUSY);
            // Check if this charging station is the charging station robot stands
            if (foundChargingStation.getLocation().equals(robot.getCurrentPosition())) {
                robot.charge();
            } else {
                // Finding ways to charging station
                steps = manager.requestPath(robot, foundChargingStation.getLocation());
                robot.stepMove(steps);
                robot.charge();
            }
            // Finally remove charging station when robot finishes charging
            manager.releaseChargeStation(foundChargingStation);
        } else {
            System.err.printf("[charging][%s] No station found in timeout%n", robot.getId());
            throw new FindChargeTimeoutException("Can not find charge in 15 minutes");
        }
    }



}