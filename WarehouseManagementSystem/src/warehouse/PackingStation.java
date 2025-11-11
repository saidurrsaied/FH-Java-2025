package warehouse;
import equipmentManager.ObjectState;

import java.awt.Point;

public class PackingStation extends WarehouseObject {
	private final Point location;
    private ObjectState state = ObjectState.FREE;
    
    public PackingStation(String id, int x, int y, WahouseObjectType object_TYPE) {
        super(id, x, y, object_TYPE);
        this.location = new Point(x, y);
    }

	@Override
	public Point getLocation() {
		return location;
	}

	public String getState() {
		return state.toString();
	}

    //Overloaded
    public String getId() {
        return super.getId();
    }
    
    @Override
    public String toString() {
        return "PackingStation [ID=" + super.getId() + ", location=" + location + "]";
    }

	public void setState(ObjectState newState) {
		state = newState;
	}

}