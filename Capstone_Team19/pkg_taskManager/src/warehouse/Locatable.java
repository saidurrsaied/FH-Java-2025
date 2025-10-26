package warehouse;

public interface Locatable {
    WarehousePosition getLocation();
    boolean isAvailable();
}
