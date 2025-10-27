package equipment;

import java.util.concurrent.Semaphore;

import common.Position;

public class ChargingStation implements EquipmentInterface{

	private final String ID;
	private final Position position;
    private final Semaphore permit = new Semaphore(1, true);

    // Robot get access to charge station
    public boolean tryAcquire() {
        return permit.tryAcquire(); 
    }
    
    // Robot finish to charge station
    public void release() { permit.release(); }

	public ChargingStation(String id, Position position) {
		super();
		this.ID = id;
		this.position = position;
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
	public Position getPosition() {
		return position;
	}
}