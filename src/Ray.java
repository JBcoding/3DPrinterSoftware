public class Ray {
    protected Point3D origin;
    protected Vector3D direction;

    public Ray(Point3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction.normalise();
    }

    public Point3D getPointOnRay(double distance) {
        return origin.add(direction.scale(distance).toPoint3D());
    }
}
