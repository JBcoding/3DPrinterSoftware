import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UnionObject extends MultiPartObject {
    MultiPartObject object1, object2;

    public UnionObject(MultiPartObject object1, MultiPartObject object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    @Override
    public Optional<List<PlaneIntersection>> getPlaneIntersectionInternal(Plane p) {
        Optional<List<PlaneIntersection>> planeIntersections1 = object1.getPlaneIntersection(p);
        Optional<List<PlaneIntersection>> planeIntersections2 = object2.getPlaneIntersection(p);
        if (!planeIntersections1.isPresent()) {
            return planeIntersections2;
        } else if (!planeIntersections2.isPresent()) {
            return planeIntersections1;
        }
        List<PlaneIntersection> finalPlaneIntersections = new ArrayList<>();
        finalPlaneIntersections.addAll(planeIntersections1.get());
        finalPlaneIntersections.addAll(planeIntersections2.get());
        return Optional.of(finalPlaneIntersections);
    }
}
