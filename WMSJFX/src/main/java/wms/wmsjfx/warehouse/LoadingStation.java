package wms.wmsjfx.warehouse;

public class LoadingStation extends WarehouseObject implements Locatable {

    private boolean isAvailable;

    public LoadingStation( String id, int x, int y, WahouseObjectType object_TYPE) {
        super(id, x, y, object_TYPE);
        this.isAvailable = true;
    }


    public boolean isAvailable() { return isAvailable; }
    public void setOccupied() { this.isAvailable = false; }
    public void setFree() { this.isAvailable = true; }




    @Override
    public String toString() {
        return  "ID:" + super.getId() + (this.isAvailable ? "  is Available" : " is Occupied");
    }


}
