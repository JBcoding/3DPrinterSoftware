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
    protected Optional<List<PlaneIntersection>> getPlaneIntersectionInternal(Plane p) {
        return baseObject.getPlaneIntersection(p);
    }

    @Override
    protected boolean isPointContainedInternal(Point3D p) {
        return baseObject.isPointContained(p);
    }
}
