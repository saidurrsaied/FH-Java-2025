package warehouse;
import java.awt.Point;

public interface Locatable {
    Point getLocation();
    boolean isAvailable();
}
