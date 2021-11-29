import java.util.List;

public interface PlaneIntersection {
    String getGeoGebraString();

    List<Point3D> getPoints(int maxPoints);

    List<Point3D> getDeltaPoints(double delta);

    PlaneIntersection multiplyWithMatrix4x4(Matrix4x4 m);

    Point3D getFirstPoint();

    PlaneIntersection getSubIntersection(double startPercentage, double endPercentage);
}
