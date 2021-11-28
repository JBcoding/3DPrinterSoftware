import java.util.List;

public interface PlaneIntersection {
    String getGeoGebraString();

    List<Point3D> getPoints(int maxPoints);

    PlaneIntersection multiplyWithMatrix4x4(Matrix4x4 m);
}
