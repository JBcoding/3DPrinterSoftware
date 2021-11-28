import java.util.List;
import java.util.Optional;

public class Driver {
    public static void main(String[] args) {
        Triangle t = new Triangle(new Point3D(0, 0, 0), new Point3D(0, 0, 10), new Point3D(10, 10,0));
        Plane p = new Plane(new Vector3D(1, 2, -.3), 0);
        System.out.println(t.getPlaneIntersection(p));

        UnitSphere s = new UnitSphere();
        System.out.println(s.getPlaneIntersection(p));

        UnitCylinder c = new UnitCylinder();
        Vector3D planeNormal = new Vector3D(0, 0, 1);
        planeNormal = Matrix4x4.getRotationMatrixAroundYAxis(Math.PI / 4).multiply(planeNormal);
        Plane plane45 = new Plane(planeNormal, .5);
        Optional<List<PlaneIntersection>> intersections = c.getPlaneIntersection(plane45);
        if (intersections.isPresent()) {
            System.out.println("MOB");
            for (PlaneIntersection pi : intersections.get()) {
                System.out.println(pi.getGeoGebraString());
            }
            System.out.println("MOB");
        }

        Matrix4x4 m1 = new Matrix4x4(new double[][]{{1,2,3,4}, {5,6,7,8}, {9,-10,11,12}, {13,14,15,16}});
        System.out.println(m1);
        System.out.println(m1.multiply(m1));

        System.out.println(m1.multiply(new Point3D(2, 3, 4)));

        System.out.println(Matrix4x4.getMovementMatrix(new Point3D(0, 1, 2)).multiply(Matrix4x4.getRotationMatrixAroundYAxis(Math.PI / 4)).multiply(Matrix4x4.getStretchingMatrixInTheXAxis(2)).multiply(s.getPlaneIntersection(p).get().get(0)).getGeoGebraString());
    }
}
