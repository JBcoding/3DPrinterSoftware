import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class MultiPartObject {
    protected Matrix4x4 deformationMatrix = Matrix4x4.IDENTITY();

    protected abstract Optional<List<PlaneIntersection>> getPlaneIntersectionInternal(Plane p);

    public Optional<List<PlaneIntersection>> getPlaneIntersection(Plane p) {
        Matrix4x4 inverseDeformationMatrix = deformationMatrix.inverse();
        Plane deformedPlane = inverseDeformationMatrix.multiply(p);
        Optional<List<PlaneIntersection>> planeIntersections = getPlaneIntersectionInternal(deformedPlane);
        if (!planeIntersections.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(planeIntersections.get().stream().map(deformationMatrix::multiply).collect(Collectors.toList()));
    }

    protected abstract boolean isPointContainedInternal(Point3D p);

    public boolean isPointContained(Point3D p) {
        Matrix4x4 inverseDeformationMatrix = deformationMatrix.inverse();
        Point3D deformedPoint = inverseDeformationMatrix.multiply(p);
        return isPointContainedInternal(deformedPoint);
    }

    protected abstract boolean isPointContainedOrOnSurfaceInternal(Point3D p);

    public boolean isPointContainedOrOnSurface(Point3D p) {
        Matrix4x4 inverseDeformationMatrix = deformationMatrix.inverse();
        Point3D deformedPoint = inverseDeformationMatrix.multiply(p);
        return isPointContainedOrOnSurfaceInternal(deformedPoint);
    }

    public Matrix4x4 getDeformationMatrix() {
        return deformationMatrix;
    }

    public void setDeformationMatrix(Matrix4x4 deformationMatrix) {
        this.deformationMatrix = deformationMatrix;
    }

    public Optional<List<PlaneIntersection>> getPlaneIntersectionWithOffset(Plane p, double offset) {
        Optional<List<PlaneIntersection>> planeIntersections = getPlaneIntersection(p);
        if (!planeIntersections.isPresent()) {
            return Optional.empty();
        }
        List<PlaneIntersection> expandedPlaneIntersections = new ArrayList<>();
        for (PlaneIntersection pi : planeIntersections.get()) {
            expandedPlaneIntersections.add(pi);

            Function xt1 = new Function(Function.Operator.ADD, pi.getFirstPoint().getX(), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.SIN), new Function(Function.Operator.CONSTANT, offset)));
            Function yt1 = new Function(Function.Operator.ADD, pi.getFirstPoint().getY(), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.COS), new Function(Function.Operator.CONSTANT, offset)));
            Function zt1 = new Function(Function.Operator.ADD, pi.getFirstPoint().getZ(), new Function(Function.Operator.CONSTANT, 0));
            expandedPlaneIntersections.add(new Curve(xt1, yt1, zt1, 0, Math.PI * 2));

            Function xt2 = new Function(Function.Operator.ADD, pi.getLastPoint().getX(), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.SIN), new Function(Function.Operator.CONSTANT, offset)));
            Function yt2 = new Function(Function.Operator.ADD, pi.getLastPoint().getY(), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.COS), new Function(Function.Operator.CONSTANT, offset)));
            Function zt2 = new Function(Function.Operator.ADD, pi.getLastPoint().getZ(), new Function(Function.Operator.CONSTANT, 0));
            expandedPlaneIntersections.add(new Curve(xt2, yt2, zt2, 0, Math.PI * 2));

            PlaneIntersection offset0 = pi.offsetXYPlane(offset);
            PlaneIntersection offset1 = pi.offsetXYPlane(-offset);

            expandedPlaneIntersections.add(offset0);
            expandedPlaneIntersections.add(offset1);
        }
        return Optional.of(expandedPlaneIntersections);
    }
}
