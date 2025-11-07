package equipmentManager;

import java.awt.Point;

public interface EquipmentInterface {
    String getID();
    Point getLocation();
    String getState();
	void setState(ObjectState newState);
}
