import java.util.Objects;

public class Vector3D {
    protected Point3D endPoint;

    public Vector3D(double x, double y, double z) {
        this.endPoint = new Point3D(x, y, z);
    }

    public Vector3D(Point3D point) {
        this.endPoint = point;
    }

    public Vector3D normalise() {
        return new Vector3D(endPoint.scale(1d / endPoint.distance0()));
    }

    public double dot(Vector3D v1) {
        return endPoint.x * v1.endPoint.x + endPoint.y * v1.endPoint.y + endPoint.z * v1.endPoint.z;
    }

    public double dot(Point3D p1) {
        return dot(new Vector3D(p1));
    }

    @Override
    public String toString() {
        return "Vector3D{" +
                "endPoint=" + endPoint +
                '}';
    }

    public Vector3D scale(double s) {
        return new Vector3D(endPoint.scale(s));
    }

    public Point3D toPoint3D() {
        return endPoint;
    }

    public Vector3D copy() {
        return new Vector3D(endPoint.copy());
    }

    public double getX() {
        return endPoint.x;
    }

    public double getY() {
        return endPoint.y;
    }

    public double getZ() {
        return endPoint.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3D vector3D = (Vector3D) o;
        return Objects.equals(endPoint, vector3D.endPoint);
    }

    public Vector3D cross(Vector3D v) {
        Point3D a = this.endPoint;
        Point3D b = v.endPoint;
        return new Vector3D(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
    }

    public Vector3D subtract(Vector3D v) {
        return new Vector3D(endPoint.subtract(v.endPoint));
    }
}
