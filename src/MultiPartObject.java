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

    public Matrix4x4 getDeformationMatrix() {
        return deformationMatrix;
    }

    public void setDeformationMatrix(Matrix4x4 deformationMatrix) {
        this.deformationMatrix = deformationMatrix;
    }
}
