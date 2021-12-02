import java.util.ArrayList;
import java.util.List;

public class OffsetCurve {
    public static List<PlaneIntersection> offsetPlaneIntersections(List<PlaneIntersection> planeIntersections, double offset) {
        List<PlaneIntersection> expandedPlaneIntersections = new ArrayList<>();
        for (PlaneIntersection pi : planeIntersections) {
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
        return expandedPlaneIntersections;
    }
}
