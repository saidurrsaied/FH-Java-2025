package taskManager;

import common.Position;
import equipment.Robot;
import warehouse.WarehousePosition;

public class Order implements Task {
    private final String ID;
    private final Position itemPosition;
    private Position packingStation;
//    private final WarehousePosition packingStation;
//    private boolean completed = false;

    public Order(String ID, Position position) {
        this.ID = ID;
        this.itemPosition = position;
//        this.packingStation = packingStation;
//        this.quantity = quantity;
//        this.packingStation = packingStation;
    }

    @Override
    public void execute(Robot robot) {        
        robot.moveTo(itemPosition);
        robot.pickUpItem(ID);
        packingStation = equipmentManager.requestAvailablePackingStation();
        robot.moveTo(packingStation);
//        completed = true;
    }

    @Override
    public String getDescription() {
        return "PickOrder[" + ID + " from " + itemPosition + " to packing station]";
    }

    @Override
	public String getID() {
		return ID;
	}

//	@Override
//    public boolean isCompleted() {
//        return completed;
//    }
}
