package warehouse;

import java.awt.*;

public class Station extends WarehouseObject implements Locatable {
    private final String stationName;
    private boolean isAvailable;

    public Station(String name, String id,  int x, int y) {
        super(id, x, y);
        this.stationName = name;
        this.isAvailable = true;
    }


    public boolean isAvailable() { return isAvailable; }
    public void setOccupied() { this.isAvailable = false; }
    public void setFree() { this.isAvailable = true; }
    public String getStationName() { return stationName; }



    @Override
    public String toString() {
        return  "ID:" + super.getId() + (this.isAvailable ? "  is Available" : " is Occupied");
    }


}
