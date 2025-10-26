package warehouse;

public class Station implements Locatable {
    private final String stationName;
    private final WarehousePosition location;
    private boolean isAvailable;

    public Station(String name,  WarehousePosition location) {
        this.stationName = name;
        this.location = location;
        this.isAvailable = true;
    }


    public WarehousePosition getLocation() {return location;}
    public boolean isAvailable() { return isAvailable; }
    public void setOccupied() { this.isAvailable = false; }
    public void setFree() { this.isAvailable = true; }
    public String getStationName() { return stationName; }



    @Override
    public String toString() {
        return super.toString() + (isAvailable ? " (Occupied)" : "(Free)");
    }


}
