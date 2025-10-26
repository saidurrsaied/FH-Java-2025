package warehouse;

public class WarehousePosition {
    private final int x;
    private final int y;

    public WarehousePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return "X=" + x + " , Y= " + y ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WarehousePosition)) return false;
        WarehousePosition position = (WarehousePosition) obj;
        return this.x == position.x && this.y == position.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
