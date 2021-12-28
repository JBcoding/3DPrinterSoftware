import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnionObject extends MultiPartObject {
    MultiPartObject object1, object2;

    public UnionObject(MultiPartObject object1, MultiPartObject object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    @Override
    protected Optional<List<PlaneIntersection>> getPlaneIntersectionInternal(Plane p) {
        Optional<List<PlaneIntersection>> planeIntersections1 = object1.getPlaneIntersection(p);
        Optional<List<PlaneIntersection>> planeIntersections2 = object2.getPlaneIntersection(p);
        if (!planeIntersections1.isPresent()) {
            return planeIntersections2;
        } else if (!planeIntersections2.isPresent()) {
            return planeIntersections1;
        }
        List<PlaneIntersection> allPlaneIntersections = new ArrayList<>();
        allPlaneIntersections.addAll(planeIntersections1.get());
        allPlaneIntersections.addAll(planeIntersections2.get());
        return Optional.of(unionPlaneIntersections(allPlaneIntersections));
    }

    @Override
    protected boolean isPointContainedInternal(Point3D p) {
        return object1.isPointContained(p) || object2.isPointContained(p);
    }

    @Override
    protected boolean isPointContainedOrOnSurfaceInternal(Point3D p) {
        return object1.isPointContainedOrOnSurface(p) || object2.isPointContainedOrOnSurface(p);
    }

    public List<PlaneIntersection> unionPlaneIntersections(List<PlaneIntersection> planeIntersections) {
        return combinePlaneIntersections(planeIntersections, p -> !isPointContainedInternal(p));
    }
}
