import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeformedBaseObject extends MultiPartObject {
    private BaseObject baseObject;

    public DeformedBaseObject(BaseObject baseObject) {
        this.baseObject = baseObject;
    }

    public DeformedBaseObject(Matrix4x4 deformationMatrix, BaseObject baseObject) {
        this.deformationMatrix = deformationMatrix;
        this.baseObject = baseObject;
    }

    @Override
    public Optional<List<PlaneIntersection>> getPlaneIntersection(Plane p) {
        Matrix4x4 inverseDeformationMatrix = deformationMatrix.inverse();
        Plane deformedPlane = inverseDeformationMatrix.multiply(p);
        Optional<List<PlaneIntersection>> planeIntersections = baseObject.getPlaneIntersection(deformedPlane);
        if (!planeIntersections.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(planeIntersections.get().stream().map(deformationMatrix::multiply).collect(Collectors.toList()));
    }
}
