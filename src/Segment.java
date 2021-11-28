import java.util.Arrays;
import java.util.List;

public class Segment implements PlaneIntersection {
    protected Point3D p1, p2;

    public Segment(Point3D p1, Point3D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    // If the line is on the plane we just return the first point
    public Point3D getPlaneIntersection(Plane plane) {
        double d1 = plane.distanceToPoint(p1);
        double d2 = plane.distanceToPoint(p2);

        if (Utils.isZero(d1)) {
            return p1; // Point one is on the plane
        }
        if (Utils.isZero(d2)) {
            return p2; // Point two is on the plane
        }

        if (Utils.isPositive(d1 * d2)) {
            return null; // points are one the same side of the plane
        }

        double t = d1 / (d1 - d2); // 'time' of intersection
        return p1.add((p2.subtract(p1)).scale(t));
    }

    @Override
    public String toString() {
        return "Segment{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }

    @Override
    public String getGeoGebraString() {
        return String.format("Segment((%s, %s, %s), (%s, %s, %s))", p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    @Override
    public List<Point3D> getPoints(int maxPoints) {
        return Arrays.asList(p1, p2);
    }

    @Override
    public PlaneIntersection multiplyWithMatrix4x4(Matrix4x4 m) {
        return new Segment(m.multiply(p1), m.multiply(p2));
    }
}
