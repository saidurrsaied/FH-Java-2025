package equipmentManager;

import java.awt.Point;
import warehouse.WahouseObjectType;
import warehouse.WarehouseObject;

public class ChargingStation extends WarehouseObject implements EquipmentInterface {

    //TODO: ID is already defined in WarehouseObject. Keep it?
	private final String ID;
	
    //TODO: Location is already defined in WarehouseObject. Keep it?
	private final Point location;
	private ObjectState state = ObjectState.FREE;

	public ChargingStation(String id, int x, int y, WahouseObjectType objectType) {
		super(id, x, y, objectType);
		this.ID = id;
		this.location = new Point(x, y);
	}

	@Override
	public String getID() {
		return this.ID;
	}

	public void setState(ObjectState newState) {
		state = newState;
	}
	
	@Override
	public String getState() {
		return state.toString();
	}

	@Override
	public Point getLocation() {
		return location;
	}

    @Override
    public String toString() {
        return "ChargingStation [ID=" + ID + ", location=" + location + "]";
    }

}