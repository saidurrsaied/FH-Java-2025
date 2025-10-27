package common;

public class Position {
    private final double x;
    private final double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    /** 
     * Distance calculation between  2 positions (2D)
     */
    public static double distance(Position a, Position b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
