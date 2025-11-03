package warehouse;
import equipmentManager.EquipmentInterface;

import java.awt.Point;
import java.util.concurrent.Semaphore;

public class PackingStation extends WarehouseObject implements EquipmentInterface {
	private final Point location;
    private final Semaphore permit = new Semaphore(1, true);

    public PackingStation(String id, int x, int y, WahouseObjectType object_TYPE) {
        super(id, x, y, object_TYPE);
        this.location = new Point(x, y);
    }

    public boolean tryAcquire() {
        return permit.tryAcquire();
    }
    public void release() { permit.release(); }



	@Override
	public Point getLocation() {
		return location;
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
		return super.getId();
	}
    //Overloaded
    public String getId() {
        return super.getId();
    }
    @Override
    public String toString() {
        return "PackingStation [ID=" + super.getId() + ", location=" + location + "]";
    }

}