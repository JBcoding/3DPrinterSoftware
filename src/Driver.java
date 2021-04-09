import java.util.List;

public class Driver {
    public static void main(String[] args) {
        Triangle t = new Triangle(new Point3D(0, 0, 0), new Point3D(0, 0, 10), new Point3D(10, 10,0));
        Plane p = new Plane(new Vector3D(1, 2, 0), 0);
        System.out.println(t.getPlaneIntersection(p));

        UnitSphere s = new UnitSphere();
        System.out.println(s.getPlaneIntersection(p));

        UnitCylinder c = new UnitCylinder();
        List<PlaneIntersection> intersections = c.getPlaneIntersection(p);
        if (intersections != null) {
            for (PlaneIntersection pi : intersections) {
                System.out.println(pi.getGeoGebraString());
            }
        }
    }
}
