package equipmentManager;
import java.awt.Point;
import java.util.concurrent.Semaphore;

public class PackingStation implements EquipmentInterface{

	private final String ID;
	private final Point location;
    private final Semaphore permit = new Semaphore(1, true);

    public boolean tryAcquire() {
        return permit.tryAcquire(); 
    }
    public void release() { permit.release(); }

	public PackingStation(String id, Point location) {
		super();
		this.ID = id;
		this.location = location;
	}
	
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
		return this.ID;
	}
}