import java.util.*;
import java.util.stream.Collectors;

public abstract class MultiPartObject {
    protected Matrix4x4 deformationMatrix = Matrix4x4.IDENTITY();

    protected abstract Optional<List<PlaneIntersectionCycle>> getPlaneIntersectionInternal(Plane p);

    public Optional<List<PlaneIntersectionCycle>> getPlaneIntersection(Plane p) {
        Matrix4x4 inverseDeformationMatrix = deformationMatrix.inverse();
        Plane deformedPlane = inverseDeformationMatrix.multiply(p);
        Optional<List<PlaneIntersectionCycle>> planeIntersections = getPlaneIntersectionInternal(deformedPlane);
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

    public Optional<List<PlaneIntersectionCycle>> getPlaneIntersectionWithOffset(PlaneIntersectionCycle pic, double offset) {
        // TODO(mbjorn) move to better place, since this can only be done in the outer most layer, since we need to be flat in the xy plane
        List<PlaneIntersection> expandedCorners = new ArrayList<>();
        List<PlaneIntersection> expandedIntersections = new ArrayList<>();
        for (PlaneIntersection pi : pic.getPlaneIntersections()) {

            Function xt1 = new Function(Function.Operator.ADD, pi.getFirstPoint().getX(), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.SIN), new Function(Function.Operator.CONSTANT, offset)));
            Function yt1 = new Function(Function.Operator.ADD, pi.getFirstPoint().getY(), new Function(Function.Operator.MULTIPLY, new Function(Function.Operator.COS), new Function(Function.Operator.CONSTANT, offset)));
            Function zt1 = new Function(Function.Operator.ADD, pi.getFirstPoint().getZ(), new Function(Function.Operator.CONSTANT, 0));
            PlaneIntersection cycle = new Curve(xt1, yt1, zt1, 0, Math.PI * 2);
            expandedCorners.add(cycle);

            PlaneIntersection offsetPi = pi.offsetXYPlane(offset);
            expandedIntersections.add(offsetPi);
        }

        List<PlaneIntersection> extendedCutIntersections = new ArrayList<>();
        for (int i = 0; i < expandedIntersections.size(); i ++) {
            PlaneIntersection offsetPlaneIntersectionBefore = expandedIntersections.get(i);
            PlaneIntersection offsetPlaneIntersectionAfter = expandedIntersections.get((i + 1) % expandedIntersections.size());
            PlaneIntersection corner = expandedCorners.get((i + 1) % expandedCorners.size());

            PythonFunctionData f1 = corner.getPythonFunctionData("g");
            PythonFunctionData f2 = offsetPlaneIntersectionBefore.getPythonFunctionData("f");
            PythonFunctionData f3 = offsetPlaneIntersectionAfter.getPythonFunctionData("f");
            double startCut = PythonFunctionData.solveFor(f2, f1)[3];
            double endCut = PythonFunctionData.solveFor(f3, f1)[1];
            if (offset > 0) {
                if (startCut < endCut) {
                    endCut -= 1;
                }
            } else {
                if (startCut > endCut) {
                    endCut += 1;
                }
            }
            if (!Utils.isRoughZero(Math.abs(startCut - endCut)) && !Utils.isRoughZero(Math.abs(startCut - endCut) - 1)) {
                extendedCutIntersections.add(corner.getSubIntersection(startCut, endCut));
            }
            extendedCutIntersections.add(offsetPlaneIntersectionAfter);
        }

        List<PlaneIntersectionCycle> expandedPlaneIntersectionCycles = PlaneIntersectionCycle.getPlaneIntersectionCyclesFromPlaneIntersections(extendedCutIntersections);
        return Optional.of(expandedPlaneIntersectionCycles);


        // OLD DEDUB CODE BELOW
       /* double deltaPrecision = offset / 4;
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
        return Optional.of(finalPlaneIntersections);*/
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
        if (containedCheck.check(midPoint)) {
            return exactExitPoint(pi, containedCheck, tMid, t2);
        } else {
            return exactExitPoint(pi, containedCheck, t1, tMid);
        }
    }

    protected List<PlaneIntersectionCycle> combinePlaneIntersections(List<PlaneIntersectionCycle> planeIntersectionCycles, PointContainedCheck containedCheck, Plane plane) {
        double deltaPrecision = 0.001; // TODO(mbjorn) put into utils
        List<PlaneIntersection> finalPlaneIntersections = new ArrayList<>();
        List<PlaneIntersection> planeIntersections = planeIntersectionCycles.stream().flatMap(cycle -> cycle.getPlaneIntersections().stream()).collect(Collectors.toList());
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
                    inOutPoints.add(exactExitPoint(pi, containedCheck, (i - 2) / (double) (points.size() - 1), (i + 1) / (double) (points.size() - 1)));
                } else if (!currentlyIn && isPointContained) {
                    startPointIndex = i;
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
        }

        List<PlaneIntersectionCycle> combinedPlaneIntersectionCycles = PlaneIntersectionCycle.getPlaneIntersectionCyclesFromPlaneIntersections(finalPlaneIntersections);
        List<PlaneIntersectionCycle> rotatedPlaneIntersectionCycles = new ArrayList<>();
        for (PlaneIntersectionCycle cycle : combinedPlaneIntersectionCycles) {
            int inCount = 0;
            for (PlaneIntersection pi: cycle.getPlaneIntersections()) {
                Point3D point = pi.getPoint(.5);
                Vector3D direction = pi.getDirection(.5);
                Matrix4x4 rotationMatrix = Matrix4x4.getRotationMatrixAroundVector(plane.getNormalVector(), Math.PI / 2);
                Vector3D sideDirection = rotationMatrix.multiply(direction);
                sideDirection = sideDirection.normalise().scale(Utils.eps * 5);
                if (containedCheck.check(point.add(sideDirection.toPoint3D()))) {
                    inCount ++;
                } else {
                    inCount --;
                }
            }
            if (inCount < 0) {
                rotatedPlaneIntersectionCycles.add(cycle.reverse());
            } else {
                rotatedPlaneIntersectionCycles.add(cycle);
            }
        }
        return rotatedPlaneIntersectionCycles;
    }

}
