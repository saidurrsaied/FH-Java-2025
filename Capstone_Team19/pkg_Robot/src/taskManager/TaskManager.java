package taskManager;

import common.Position;
import equipment.*;

public class TaskManager {
    private EquipmentManager equipmentManager;

    public TaskManager(EquipmentManager equipmentManager) {
        this.equipmentManager = equipmentManager;
    }

    public void createOrder(Position orderPosition, String itemId, int priority) {
        System.out.printf("[%s] New order %s", this.getClass().getName(), itemId);
        
        Task newTask = new Order(itemId, orderPosition);
        
        equipmentManager.assignTask(newTask);
    }
}
