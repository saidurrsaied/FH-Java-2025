package equipment;

import java.util.concurrent.Semaphore;

import common.Position;

public class HoldingStation implements EquipmentInterface {

    private final String ID;
    private final Position position;
    private final Semaphore permit = new Semaphore(1, true);

    public HoldingStation(String id, Position position) {
        this.ID = id;
        this.position = position;
    }

    public boolean tryAcquire() {
        return permit.tryAcquire();
    }

    public void release() {
        permit.release();
    }

	public boolean isAvailable() {
		return (permit.availablePermits() > 0);
	}
	
    @Override
    public String getID() {
        return ID;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public String getState() {
        return (permit.availablePermits() > 0) ? "FREE" : "IN_USE";
    }
}