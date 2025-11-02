package taskManager;

import equipmentManager.Robot;
import equipmentManager.EquipmentManager; // <-- Needs reference to the manager

/**
 * Task Interface.
 * Execute method now requires EquipmentManager for Just-in-Time resource requests.
 */
public interface Task { 
    String getID();
    String getDescription();
    TaskType getType();
    /**
     * Core execution logic.
     * The implementation must request necessary resources (like stations) 
     * from the EquipmentManager Just-in-Time using the provided manager reference.
     * @param robot The Robot executing the task.
     * @param manager Reference to the EquipmentManager to request resources.
     * @throws InterruptedException If the robot's thread is interrupted.
     */
    void execute(Robot robot, EquipmentManager manager) throws InterruptedException; 
}