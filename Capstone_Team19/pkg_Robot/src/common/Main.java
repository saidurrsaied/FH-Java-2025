package common;
import equipments.ChargingStation;
import equipments.Robot;
import taskManager.Order;
import taskManager.Stock;
import warehouse.WarehousePosition;

public class Main {

    public static void main(String[] args) {
        ChargingStation station = new ChargingStation();
        Robot robot1 = new Robot("Shuttle-1", station);
        Robot robot2 = new Robot("Shuttle-2", station);

        new Thread(robot1).start();
        new Thread(robot2).start();

        WarehousePosition shelfA = new WarehousePosition(5, 10);
        WarehousePosition shelfB = new WarehousePosition(8, 15);
        WarehousePosition packingStation = new WarehousePosition(0, 0);
        WarehousePosition unloading = new WarehousePosition(2, 3);

        Order order1 = new Order("Widget", shelfA, 5, packingStation);
        robot1.addTask(order1);
        robot2.addTask(new Stock("Gadget", unloading, shelfB));

    }
}
