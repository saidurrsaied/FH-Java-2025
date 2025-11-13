package wms.wmsjfx.equipmentManager;

public enum RobotState {
    IDLE,
    MOVING,
    PICKING,
    WAITING_FOR_AVAILABLE_PACKING_STATION,
    PACKING,
    CHARGING,
    WAITING_FOR_AVAILABLE_CHARGING_STATION,
}
