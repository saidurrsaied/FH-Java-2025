package warehouse; // (Hoặc package của bạn)

import equipmentManager.ObjectState;
import java.awt.Point;
import java.util.concurrent.Semaphore;

public class LoadingStation extends WarehouseObject {
    
    private ObjectState state = ObjectState.FREE;
    private final Point location;
    private final Semaphore permit = new Semaphore(1, true);

    public LoadingStation(String id, int x, int y, WahouseObjectType objectType) {
        super(id, x, y, objectType);
        this.location = new Point(x, y);
    }

    public void acquire() throws InterruptedException {
        permit.acquire();
        this.state = ObjectState.BUSY;
    }

    public void release() {
        this.state = ObjectState.FREE;
        permit.release();
    }
    
    // --- Getters ---
    public Point getLocation() { 
        return this.location;
    }
 
    public String getState() { 
        return state.toString(); 
    }
    
    public void setState(ObjectState s) { 
        this.state = s; 
    }

	@Override
	public String toString() {
        return "LoadingStation [ID=" + super.getId() + ", location=" + location + "]";
	}
}