import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UnitSphere implements BaseObject {
    // center 0,0,0 and radius 1, just use transform to make it into any ellipsoid


    public Optional<List<PlaneIntersection>> getPlaneIntersection(Plane p) {
        double distance = p.distanceToPoint(new Point3D(0, 0, 0));
        if (Math.abs(distance) >= 1) { // no intersection or point intersection
            return Optional.empty();
        }

        double radiusOfIntersectionCircle = Math.abs(Math.sin(Math.PI/2 - Math.asin(Math.abs(distance))));
        Point3D centerOfIntersectionCircle = p.getClosestPoint(new Point3D(0, 0, 0));

        // Based on ({{1,0,0},{0,1,0},{0,0,1}} + {{0,-z,y},{z,0,-x},{-y,x,0}}*sin(theta) + {{0,-z,y},{z,0,-x},{-y,x,0}}.{{0,-z,y},{z,0,-x},{-y,x,0}}*(1-cos(theta)) ) .{a, b, c}
        // Given the normal vector we want to rotate around (x, y, z) // the normal vector of the plane in this case
        // And the vector we want to rotate (a, b, c) // A vector from the circle center to the circle perimeter
        // And the circle center point (e, f, g) // The point of the center of the circle
        // We have the circle perimeter given by these functions:

        // e + cos(θ) (a y^2 + a z^2 - b x y - c x z) + sin(θ) (c y - b z) + b x y + c x z - a y^2 - a z^2 + a
        // f + cos(θ) (-a x y + b x^2 + b z^2 - c y z) + sin(θ) (a z - c x) + a x y - b x^2 - b z^2 + b + c y z
        // g + cos(θ) (-a x z - b y z + c x^2 + c y^2) + sin(θ) (b x - a y) + a x z + b y z - c x^2 - c y^2 + c

        // θ runs from 0 to 2 pi
        double x = p.getNormalVector().getX();
        double y = p.getNormalVector().getY();
        double z = p.getNormalVector().getZ();
        Vector3D vectorToCirclePerimeter = p.getVectorOnPlane().normalise().scale(radiusOfIntersectionCircle);
        double a = vectorToCirclePerimeter.getX();
        double b = vectorToCirclePerimeter.getY();
        double c = vectorToCirclePerimeter.getZ();
        double e = centerOfIntersectionCircle.x;
        double f = centerOfIntersectionCircle.y;
        double g = centerOfIntersectionCircle.z;

        Function xt = new Function(Function.Operator.ADD, e, new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.COS), a * y * y + a * z * z - b * x * y - c * x * z), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.SIN), c * y - b * z), b * x * y + c * x * z - a * y * y - a * z * z + a);
        Function yt = new Function(Function.Operator.ADD, f, new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.COS), -a * x * y + b * x * x + b * z * z - c * y * z), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.SIN), a * z - c * x), a * x * y - b * x * x - b * z * z + b + c * y * z);
        Function zt = new Function(Function.Operator.ADD, g, new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.COS), -a * x * z - b * y * z + c * x * x + c * y * y), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.SIN), b * x - a * y), a * x * z + b * y * z - c * x * x - c * y * y + c);

        return Optional.of(Collections.singletonList(new Curve(xt, yt, zt, 0, Math.PI * 2)));
    }
}
