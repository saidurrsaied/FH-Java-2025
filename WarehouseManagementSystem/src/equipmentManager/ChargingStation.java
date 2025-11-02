package equipmentManager;

import java.awt.Point;
import java.util.concurrent.Semaphore;

public class ChargingStation implements EquipmentInterface {

	private final String ID;
	private final Point location;
    private final Semaphore permit = new Semaphore(1, true);

    // Robot get access to charge station
    public boolean tryAcquire() {
        return permit.tryAcquire(); 
    }
    
    // Robot finish to charge station
    public void release() { permit.release(); }

	public ChargingStation(String id, Point location) {
		super();
		this.ID = id;
		this.location = location;
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
}