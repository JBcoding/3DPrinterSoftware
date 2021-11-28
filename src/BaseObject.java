import java.util.List;
import java.util.Optional;

public interface BaseObject {
    Optional<List<PlaneIntersection>> getPlaneIntersection(Plane p);
}
