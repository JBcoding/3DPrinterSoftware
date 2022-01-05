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

        double deltaPrecision = offset / 4;
        long t = System.nanoTime();
        List<List<Point3D>> planeIntersectionPoints = planeIntersections.get().stream().map(pi -> pi.getDeltaPoints(deltaPrecision)).collect(Collectors.toList());
        List<PlaneIntersection> finalPlaneIntersections = new ArrayList<>();
        for (PlaneIntersection epi : expandedPlaneIntersections) {
            boolean currentlyAtDistance = getShortestDistanceBetweenPointsAndPoint(planeIntersectionPoints, epi.getFirstPoint(), deltaPrecision) > offset - deltaPrecision;
            List<Point3D> points = epi.getDeltaPoints(deltaPrecision);
            int startPointIndex = 0;
            for (int i = 0; i < points.size(); i++) {
                boolean atDistance = getShortestDistanceBetweenPointsAndPoint(planeIntersectionPoints, points.get(i), deltaPrecision) > offset - deltaPrecision;
                if (atDistance && !currentlyAtDistance) {
                    // start here
                    startPointIndex = i;
                } else if (!atDistance && currentlyAtDistance) {
                    // end here
                    finalPlaneIntersections.add(epi.getSubIntersection(
                            startPointIndex / (double) points.size(),
                            i / (double) points.size()
                    ));
                }
                currentlyAtDistance = atDistance;
            }
            if (currentlyAtDistance) {
                // end here
                finalPlaneIntersections.add(epi.getSubIntersection(
                        startPointIndex / (double) points.size(),
                        1
                ));
            }
        }
        System.out.println((System.nanoTime() - t) / 1e9 + " s");
        return Optional.of(finalPlaneIntersections);
    }

    private double getShortestDistanceBetweenPointsAndPoint(List<List<Point3D>> points, Point3D point, double deltaPrecision) {
        double shortestDistance = Double.MAX_VALUE;
        for (List<Point3D> listOfPoints : points) {
            for (int i = 0; i < listOfPoints.size(); i++) {
                double distance = listOfPoints.get(i).subtract(point).distance0();
                shortestDistance = Math.min(shortestDistance, distance);
            }
        }
        return shortestDistance;
    }

    public interface PointContainedCheck {
        boolean check(Point3D p);
    }

    // expects t1 to be contained and t2 to not be contained
    protected Point3D exactExitPoint(PlaneIntersection pi, PointContainedCheck containedCheck, double t1, double t2) {
        if (Math.abs(t2 - t1) < Utils.eps) {
            return pi.getPoint((t1 + t2) / 2);
        }
        double delta = t2 - t1;
        double tMid = t1 + delta / 2;
        Point3D midPoint = pi.getPoint(tMid);
        System.out.println(pi.hashCode() + " " + t1 + " " + t2 + " " + containedCheck.check(midPoint) + " " + pi.getPoint((t1 + t2) / 2));
        if (containedCheck.check(midPoint)) {
            return exactExitPoint(pi, containedCheck, tMid, t2);
        } else {
            return exactExitPoint(pi, containedCheck, t1, tMid);
        }
    }

    protected List<PlaneIntersection> combinePlaneIntersections(List<PlaneIntersection> planeIntersections, PointContainedCheck containedCheck) {
        double deltaPrecision = 0.001; // TODO(mbjorn) put into utils
        List<PlaneIntersection> finalPlaneIntersections = new ArrayList<>();
        for (PlaneIntersection pi : planeIntersections) {
            boolean currentlyIn = containedCheck.check(pi.getFirstPoint());
            List<Point3D> points = pi.getDeltaPoints(deltaPrecision);
            int startPointIndex = 0;
            List<Point3D> inOutPoints = new ArrayList<>();
            for (int i = 0; i < points.size(); i ++) {
                Point3D p = points.get(i);
                boolean isPointContained = containedCheck.check(p);
                if (currentlyIn && !isPointContained) {
                    finalPlaneIntersections.add(pi.getSubIntersection(
                            startPointIndex / (double) (points.size() - 1),
                            i / (double) (points.size() - 1)
                    ));
                    System.out.println("a");
                    inOutPoints.add(exactExitPoint(pi, containedCheck, (i - 2) / (double) (points.size() - 1), (i + 1) / (double) (points.size() - 1)));
                } else if (!currentlyIn && isPointContained) {
                    startPointIndex = i;
                    System.out.println("b");
                    inOutPoints.add(exactExitPoint(pi, containedCheck, (i + 1) / (double) (points.size() - 1), (i - 2) / (double) (points.size() - 1)));
                }
                currentlyIn = isPointContained;
            }
            if (currentlyIn && startPointIndex != points.size() - 1) {
                finalPlaneIntersections.add(pi.getSubIntersection(
                        startPointIndex / (double) (points.size() - 1),
                        1
                ));
            }
            System.out.println(inOutPoints);
        }
        System.out.println();
        return finalPlaneIntersections;
    }

}
