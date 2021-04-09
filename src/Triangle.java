import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Triangle {
    protected Point3D p1, p2, p3;

    public Triangle(Point3D p1, Point3D p2, Point3D p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Segment getPlaneIntersection(Plane plane) {
        List<Point3D> intersectionPoints = new ArrayList<>();
        intersectionPoints.add((new Segment(p1, p2)).getPlaneIntersection(plane));
        intersectionPoints.add((new Segment(p2, p3)).getPlaneIntersection(plane));
        intersectionPoints.add((new Segment(p3, p1)).getPlaneIntersection(plane));

        intersectionPoints = intersectionPoints.stream().filter(Objects::nonNull).collect(Collectors.toList());
        intersectionPoints = Point3D.removeDuplicatesFromList(intersectionPoints);

        switch (intersectionPoints.size()) {
            case 0: // no intersection
            case 1: // we hit a corner, so we just call it no intersection
                return null;
            case 2: // we have 2 distinct points so we have an segment
                return new Segment(intersectionPoints.get(0), intersectionPoints.get(1));
            default: // Should never happen
                throw new IllegalStateException("We have more than 2 distinct intersection points between a triangle and a plane");
        }
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", p3=" + p3 +
                '}';
    }
}
