package equipmentManager;

import java.awt.Point;
import java.util.concurrent.Semaphore;
import warehouse.WarehouseObject;

public class ChargingStation extends WarehouseObject implements EquipmentInterface {

	private final String ID;
    private String STATION_TYPE ;
	private final Point location;
    private final Semaphore permit = new Semaphore(1, true);

    // Robot get access to charge station
    public boolean tryAcquire() {
        return permit.tryAcquire(); 
    }
    
    // Robot finish to charge station
    public void release() { permit.release(); }

	public ChargingStation(String id, String objectType, int x, int y) {
		super(id, x, y);
		this.ID = id;
		this.location = new Point(x, y);
        this.STATION_TYPE = objectType;
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