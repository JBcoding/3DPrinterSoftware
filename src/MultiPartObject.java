import java.util.List;
import java.util.Optional;

public abstract class MultiPartObject {
    protected Matrix4x4 deformationMatrix = Matrix4x4.IDENTITY();

    public abstract Optional<List<PlaneIntersection>> getPlaneIntersection(Plane p);

    public Matrix4x4 getDeformationMatrix() {
        return deformationMatrix;
    }

    public void setDeformationMatrix(Matrix4x4 deformationMatrix) {
        this.deformationMatrix = deformationMatrix;
    }
}
