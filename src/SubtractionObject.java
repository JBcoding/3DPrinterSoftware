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
    protected Optional<List<PlaneIntersection>> getPlaneIntersectionInternal(Plane p) {
        Optional<List<PlaneIntersection>> planeIntersections1 = object1.getPlaneIntersection(p);
        Optional<List<PlaneIntersection>> planeIntersections2 = object2.getPlaneIntersection(p);
        if (!planeIntersections1.isPresent()) {
            return Optional.empty();
        } else if (!planeIntersections2.isPresent()) {
            return planeIntersections1;
        }
        List<PlaneIntersection> allPlaneIntersections = new ArrayList<>();
        allPlaneIntersections.addAll(planeIntersections1.get());
        allPlaneIntersections.addAll(planeIntersections2.get());
        return Optional.of(subtractPlaneIntersections(allPlaneIntersections));

    }

    @Override
    protected boolean isPointContainedInternal(Point3D p) {
        return object1.isPointContained(p) && !object2.isPointContainedOrOnSurface(p);
    }

    @Override
    protected boolean isPointContainedOrOnSurfaceInternal(Point3D p) {
        return object1.isPointContainedOrOnSurface(p) && !object2.isPointContained(p);
    }

    public List<PlaneIntersection> subtractPlaneIntersections(List<PlaneIntersection> planeIntersections) {
        double deltaPrecision = 0.01;
        List<PlaneIntersection> finalPlaneIntersections = new ArrayList<>();
        for (PlaneIntersection pi : planeIntersections) {
            boolean currentlyIn = isPointContainedOrOnSurfaceInternal(pi.getFirstPoint());
            List<Point3D> points = pi.getDeltaPoints(deltaPrecision);
            int startPointIndex = 0;
            for (int i = 0; i < points.size(); i ++) {
                Point3D p = points.get(i);
                boolean isPointContained = isPointContainedOrOnSurfaceInternal(p);
                if (currentlyIn && !isPointContained) {
                    finalPlaneIntersections.add(pi.getSubIntersection(
                            startPointIndex / (double) points.size(),
                            i / (double) points.size()
                    ));
                } else if (!currentlyIn && isPointContained) {
                    startPointIndex = i;
                }
                currentlyIn = isPointContained;
            }
            if (currentlyIn && startPointIndex != points.size() - 1) {
                finalPlaneIntersections.add(pi.getSubIntersection(
                        startPointIndex / (double) points.size(),
                        1
                ));
            }
        }
        return finalPlaneIntersections;
    }
}
