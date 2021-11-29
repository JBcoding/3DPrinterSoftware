import java.util.*;

public class UnitCylinder implements BaseObject {
    // A cylinder with height 1, standing on the xy plane with center bottom at (0,0,0) and radius 1


    public Optional<List<PlaneIntersection>> getPlaneIntersection(Plane p) {
        if (Utils.isZero(p.getNormalVector().getZ())) {
            // The plane is vertical
            if (p.planeDistance >= 1) {
                return Optional.empty(); // No intersection
            } else {
                // Intersection can be made with 4 straight lines
                double planeNormalAngle = Math.atan2(p.getNormalVector().getY(), p.getNormalVector().getX());
                double planeDistance = p.planeDistance;

                double cutAngle1 = planeNormalAngle + Math.acos(planeDistance);
                double cutAngle2 = planeNormalAngle - Math.acos(planeDistance);

                Point3D p1 = new Point3D(Math.cos(cutAngle1), Math.sin(cutAngle1), 0);
                Point3D p2 = new Point3D(Math.cos(cutAngle1), Math.sin(cutAngle1), 1);
                Point3D p3 = new Point3D(Math.cos(cutAngle2), Math.sin(cutAngle2), 0);
                Point3D p4 = new Point3D(Math.cos(cutAngle2), Math.sin(cutAngle2), 1);

                Segment topSegment = new Segment(p2, p4);
                Segment bottomSegment = new Segment(p1, p3);
                Segment sideSegment1 = new Segment(p1, p2);
                Segment sideSegment2 = new Segment(p3, p4);

                return Optional.of(Arrays.asList(topSegment, bottomSegment, sideSegment1, sideSegment2));
            }
        }

        Vector3D up = new Vector3D(0, 0, 1);
        Vector3D steepestAscent = p.projectVectorOntoPlane(up);
        double xyPlaneLength = Math.sqrt(Math.pow(steepestAscent.getX(), 2) + Math.pow(steepestAscent.getY(), 2));
        double highestHeight, lowestHeight;
        if (Utils.isZero(xyPlaneLength)) {
            // Plane is horizontal
            highestHeight = lowestHeight = p.planeDistance;
        } else {
            // Plan is sloping
            Vector3D steepestAscentUnit = steepestAscent.scale(1d / xyPlaneLength);
            highestHeight = p.getRayIntersectionPoint(new Ray(new Point3D(steepestAscentUnit.getX(), steepestAscentUnit.getY(), 0), up)).getZ();
            lowestHeight = p.getRayIntersectionPoint(new Ray(new Point3D(-steepestAscentUnit.getX(), -steepestAscentUnit.getY(), 0), up)).getZ();
        }
        double heightAtCenter = (highestHeight + lowestHeight) / 2d; // Since the center is in the middle of these to extremes

        if (highestHeight <= 0 || lowestHeight >= 1) {
            return Optional.empty(); // the cylinder is above or below the plane
        }

        double heightPerUnitXAxis = p.getRayIntersectionPoint(new Ray(new Point3D(1, 0, 0), up)).getZ() - p.getRayIntersectionPoint(new Ray(new Point3D(0, 0, 0), up)).getZ();
        double heightPerUnitYAxis = p.getRayIntersectionPoint(new Ray(new Point3D(0, 1, 0), up)).getZ() - p.getRayIntersectionPoint(new Ray(new Point3D(0, 0, 0), up)).getZ();

        Function xt = new Function(Function.Operator.COS);
        Function yt = new Function(Function.Operator.SIN);
        Function zt = new Function(Function.Operator.ADD, heightAtCenter, new Function(Function.Operator.MULTIPLY, xt, heightPerUnitXAxis), new Function(Function.Operator.MULTIPLY, yt, heightPerUnitYAxis));

        double angleToSteepestAscent = Math.atan2(steepestAscent.getY(), steepestAscent.getX());
        double heightSpan = (highestHeight - lowestHeight) / 2;

        double deltaAngleToBottomCut = Math.acos((0 - lowestHeight) / heightSpan - 1);
        double deltaAngleToTopCut = Math.acos((1 - lowestHeight) / heightSpan - 1);

        double bottomCutStartAngle = angleToSteepestAscent + deltaAngleToBottomCut;
        double bottomCutEndAngle = bottomCutStartAngle + (Math.PI - deltaAngleToBottomCut) * 2;

        double topCutStartAngle = angleToSteepestAscent + deltaAngleToTopCut;
        double topCutEndAngle = topCutStartAngle + (Math.PI - deltaAngleToTopCut) * 2;

        if (highestHeight > 1 && lowestHeight < 0) {
            // we cut both the top and bottom of the cylinder
            Curve curve1 = new Curve(xt, yt, zt, topCutStartAngle, bottomCutStartAngle);
            Curve curve2 = new Curve(xt, yt, zt, bottomCutEndAngle, topCutEndAngle);
            Segment topSegment = new Segment(new Point3D(Math.cos(topCutStartAngle), Math.sin(topCutStartAngle), 1), new Point3D(Math.cos(topCutEndAngle), Math.sin(topCutEndAngle), 1));
            Segment bottomSegment = new Segment(new Point3D(Math.cos(bottomCutStartAngle), Math.sin(bottomCutStartAngle), 0), new Point3D(Math.cos(bottomCutEndAngle), Math.sin(bottomCutEndAngle), 0));
            return Optional.of(Arrays.asList(curve1, curve2, topSegment, bottomSegment));
        } else if (highestHeight > 1) {
            // we cut only the top of the cylinder
            Curve curve = new Curve(xt, yt, zt, topCutStartAngle, topCutEndAngle);
            Segment topSegment = new Segment(new Point3D(Math.cos(topCutStartAngle), Math.sin(topCutStartAngle), 1), new Point3D(Math.cos(topCutEndAngle), Math.sin(topCutEndAngle), 1));
            return Optional.of(Arrays.asList(curve, topSegment));
        } else if (lowestHeight < 0) {
            // we cut only the bottom of the cylinder
            Curve curve = new Curve(xt, yt, zt, bottomCutEndAngle, bottomCutStartAngle + Math.PI * 2);
            Segment bottomSegment = new Segment(new Point3D(Math.cos(bottomCutStartAngle), Math.sin(bottomCutStartAngle), 0), new Point3D(Math.cos(bottomCutEndAngle), Math.sin(bottomCutEndAngle), 0));
            return Optional.of(Arrays.asList(curve, bottomSegment));
        } else {
            // we cut neither the bottom or top of the cylinder
            Curve curve = new Curve(xt, yt, zt, 0, Math.PI * 2);
            return Optional.of(Collections.singletonList(curve));
        }
    }

    @Override
    public boolean isPointContained(Point3D p) {
        return p.getZ() > Utils.eps
                && p.getZ() < 1.0 - Utils.eps
                && Math.sqrt(Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2)) < 1.0 - Utils.eps;
    }
}
