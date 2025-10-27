package equipment;
import java.util.concurrent.Semaphore;

import common.Position;

public class PackingStation implements EquipmentInterface{

	private final String ID;
	private final Position position;
    private final Semaphore permit = new Semaphore(1, true);

    public boolean tryAcquire() {
        return permit.tryAcquire(); 
    }
    public void release() { permit.release(); }

	public PackingStation(String id, Position position) {
		super();
		this.ID = id;
		this.position = position;
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	
	public boolean isAvailable() {
		return (permit.availablePermits() > 0);
	}
	
	@Override
	public String getState() {
		return (permit.availablePermits() > 0) ? "FREE" : "IN_USE";
	}
	
	@Override
	public String getID() {
		return this.ID;
	}
}