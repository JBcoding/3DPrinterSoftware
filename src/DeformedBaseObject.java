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
    protected Optional<List<PlaneIntersectionCycle>> getPlaneIntersectionInternal(Plane p) {
        return PlaneIntersectionCycle.getPlaneIntersectionCyclesFromPlaneIntersections(baseObject.getPlaneIntersection(p));
    }

    @Override
    protected boolean isPointContainedInternal(Point3D p) {
        return baseObject.isPointContained(p);
    }

    @Override
    protected boolean isPointContainedOrOnSurfaceInternal(Point3D p) {
        return baseObject.isPointContainedOrOnSurface(p);
    }
}
