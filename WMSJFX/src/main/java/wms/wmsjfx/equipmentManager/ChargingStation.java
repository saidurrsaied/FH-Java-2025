package wms.wmsjfx.equipmentManager;

import java.awt.Point;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseObject;

public class ChargingStation extends WarehouseObject {

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
	public String getId() {
		return this.ID;
	}

	public void setState(ObjectState newState) {
		state = newState;
	}
	
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