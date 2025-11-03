package equipmentManager;

import java.awt.Point;
import java.util.concurrent.Semaphore;

import warehouse.WahouseObjectType;
import warehouse.WarehouseObject;

public class ChargingStation extends WarehouseObject implements EquipmentInterface {

    //TODO: ID is already defined in WarehouseObject. Keep it?
	private final String ID;
    //TODO: Location is already defined in WarehouseObject. Keep it?
	private final Point location;
    private final Semaphore permit = new Semaphore(1, true);

    // Robot gets access to charge station
    public boolean tryAcquire() {
        return permit.tryAcquire(); 
    }
    
    // Robot finish to charge station
    public void release() { permit.release(); }

	public ChargingStation(String id, int x, int y, WahouseObjectType objectType) {
		super(id, x, y, objectType);
		this.ID = id;
		this.location = new Point(x, y);
	}

	public boolean isAvailable() {
		return (permit.availablePermits() > 0);
	}

	@Override
	public String getID() {
		return this.ID;
	}

	@Override
	public String getState() {
		return (permit.availablePermits() > 0) ? "FREE" : "IN_USE";
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