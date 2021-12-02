import java.util.ArrayList;
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
    public List<Point3D> getDeltaPoints(double delta) {
        List<Point3D> points = new ArrayList<>();
        Point3D p2p1 = p2.subtract(p1);
        double totalDistance = p2p1.distance0();
        int numSegments = (int) (totalDistance / delta + 1);
        for (int i = 0; i < numSegments; i++) {
            points.add(p1.add(p2p1.scale(i / (double)numSegments)));
        }
        return points;
    }

    @Override
    public PlaneIntersection multiplyWithMatrix4x4(Matrix4x4 m) {
        return new Segment(m.multiply(p1), m.multiply(p2));
    }

    @Override
    public Point3D getFirstPoint() {
        return p1;
    }

    @Override
    public Point3D getLastPoint() {
        return p2;
    }

    @Override
    public PlaneIntersection getSubIntersection(double startPercentage, double endPercentage) {
        Point3D p2p1 = p2.subtract(p1);
        return new Segment(p1.add(p2p1.scale(startPercentage)), p1.add(p2p1.scale(endPercentage)));
    }

    @Override
    public PlaneIntersection offsetXYPlane(double offset) {
        Vector3D p1p2 = new Vector3D(p1.subtract(p2));
        Vector3D p1p2Rotated = new Vector3D(-p1p2.getY(), p1p2.getX(), 0);
        p1p2Rotated = p1p2Rotated.normalise().scale(offset);
        return new Segment(p1.add(p1p2Rotated.toPoint3D()), p2.add(p1p2Rotated.toPoint3D()));
    }
}
