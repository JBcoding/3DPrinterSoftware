import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubtractionObject extends MultiPartObject {
    MultiPartObject object1, object2;

    public SubtractionObject(MultiPartObject object1, MultiPartObject object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    @Override
    protected Optional<List<PlaneIntersectionCycle>> getPlaneIntersectionInternal(Plane p) {
        Optional<List<PlaneIntersectionCycle>> planeIntersections1 = object1.getPlaneIntersection(p);
        Optional<List<PlaneIntersectionCycle>> planeIntersections2 = object2.getPlaneIntersection(p);
        if (!planeIntersections1.isPresent()) {
            return Optional.empty();
        } else if (!planeIntersections2.isPresent()) {
            return planeIntersections1;
        }
        List<PlaneIntersectionCycle> allPlaneIntersectionCycles = new ArrayList<>();
        allPlaneIntersectionCycles.addAll(planeIntersections1.get());
        allPlaneIntersectionCycles.addAll(planeIntersections2.get());
        return Optional.of(subtractPlaneIntersections(allPlaneIntersectionCycles));

    }

    @Override
    protected boolean isPointContainedInternal(Point3D p) {
        return object1.isPointContained(p) && !object2.isPointContainedOrOnSurface(p);
    }

    @Override
    protected boolean isPointContainedOrOnSurfaceInternal(Point3D p) {
        return object1.isPointContainedOrOnSurface(p) && !object2.isPointContained(p);
    }

    public List<PlaneIntersectionCycle> subtractPlaneIntersections(List<PlaneIntersectionCycle> planeIntersectionCycles) {
        return combinePlaneIntersections(planeIntersectionCycles, this::isPointContainedOrOnSurfaceInternal);
    }
}
