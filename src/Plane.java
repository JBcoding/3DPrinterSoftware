public class Plane {
    private Vector3D planeNormal;
    protected double planeDistance;

    public Plane(Vector3D planeNormal, double planeDistance) {
        this.planeNormal = planeNormal;
        this.planeDistance = planeDistance;

        this.planeNormal = this.planeNormal.normalise();
    }

    public double distanceToPoint(Point3D p1) {
        return planeNormal.dot(p1) - planeDistance;
    }

    @Override
    public String toString() {
        return "Plane{" +
                "planeNormal=" + planeNormal +
                ", planeDistance=" + planeDistance +
                '}';
    }

    public Point3D getClosestPoint(Point3D p) {
        double offset = -1 * distanceToPoint(p);
        return p.add(planeNormal.scale(offset).toPoint3D());
    }

    public Vector3D getNormalVector() {
        return planeNormal.copy();
    }

    public Point3D getPlaneCenter() {
        return planeNormal.scale(planeDistance).toPoint3D();
    }

    public Vector3D getVectorOnPlane() {
        if (planeNormal.equals(new Vector3D(1, 0, 0))) {
            return planeNormal.cross(new Vector3D(0, 1, 0));
        } else {
            return planeNormal.cross(new Vector3D(1, 0, 0));
        }
    }

    public Vector3D projectVectorOntoPlane(Vector3D v) {
        return v.subtract(planeNormal.scale(v.dot(planeNormal)));
    }

    public Point3D getRayIntersectionPoint(Ray ray) {
        if (Utils.isZero(ray.direction.dot(planeNormal))) {
            return null; // parallel to the plane
        }
        double t = -(ray.origin.dot(planeNormal) - planeDistance) / ray.direction.dot(planeNormal);
        return ray.getPointOnRay(t);
    }
}
