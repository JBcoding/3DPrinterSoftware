import java.util.List;

public abstract class PlaneIntersection {
    abstract String getGeoGebraString();

    abstract List<Point3D> getPoints(int maxPoints);

    abstract List<Point3D> getDeltaPoints(double delta);

    abstract PlaneIntersection multiplyWithMatrix4x4(Matrix4x4 m);

    abstract Point3D getFirstPoint();

    abstract Point3D getLastPoint();

    abstract Point3D getPoint(double percentage);

    public abstract Vector3D getDirection(double percentage);

    abstract PlaneIntersection getSubIntersection(double startPercentage, double endPercentage);

    abstract PlaneIntersection offsetXYPlane(double offset);

    double getShortestDistance(Point3D point, double deltaPrecision) {
        double shortestDistance = Double.MAX_VALUE;
        for (Point3D p : getDeltaPoints(deltaPrecision)) {
            shortestDistance = Math.min(shortestDistance, p.subtract(point).distance0());
        }
        return shortestDistance;
    }

    public abstract PlaneIntersection reverse();
}
