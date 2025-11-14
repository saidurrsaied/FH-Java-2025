package wms.wmsjfx.equipmentManager;

import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseObject;

public class ChargingStation extends WarehouseObject {
    private ObjectState state = ObjectState.FREE;

    public ChargingStation(String id, int x, int y, WahouseObjectType objectType) {
        super(id, x, y, objectType);
    }

    public void setState(ObjectState newState) {
        state = newState;
    }

    public String getState() {
        return state.toString();
    }

    @Override
    public String toString() {
        return "ChargingStation [ID=" + super.getId() + ", location=" + super.getLocation() + "]";
    }
}