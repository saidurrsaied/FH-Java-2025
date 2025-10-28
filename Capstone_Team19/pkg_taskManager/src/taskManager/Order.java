package taskManager;

import java.awt.Point;
import equipments.Robot;


public class Order implements Task {
    private final String itemName;
    private final Point itemLocation;
    private final int quantity;
    private final Point packingStation;
    private boolean completed = false;

    public Order(String itemName, Point itemLocation, int quantity, Point packingStation) {
        this.itemName = itemName;
        this.itemLocation = itemLocation;
        this.quantity = quantity;
        this.packingStation = packingStation;
    }

    @Override
    public void execute(Robot robot) {
        robot.setStatus("Picking up order: " + itemName);
        System.out.println(robot.getName() + " is now " + robot.getStatus());

        robot.moveTo(itemLocation);
        System.out.println(robot.getName() + " collecting " + quantity + "x " + itemName);
        try {
            Thread.sleep(2000); // simulate pickup delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.moveTo(packingStation);
        System.out.println(robot.getName() + " delivered " + itemName + " to packing station");
        robot.setStatus("Idle");
        completed = true;
    }

    @Override
    public String getDescription() {
        return "PickOrder[" + itemName + " x" + quantity + " from " + itemLocation + " to packing station]";
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}
