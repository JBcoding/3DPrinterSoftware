import java.util.*;
import java.util.stream.Collectors;

public class PlaneIntersectionCycle {
    private final List<PlaneIntersection> cycle;

    public PlaneIntersectionCycle(List<PlaneIntersection> cycle) {
        this.cycle = cycle;
    }

    private static boolean isFalsePositiveSelfIntersectionPoint(PlaneIntersection pi1, PlaneIntersection pi2, List<PlaneIntersectionCycle> planeCycles, double pi1_percentage, double pi2_percentage) {
        // It is a false positive if pi1 and pi2 are the same segment and the points are the same percentage
        if (pi1 == pi2 && Utils.isRoughZero(pi1_percentage - pi2_percentage)) {
            return true;
        }

        // It is also a false positive if pi1 and pi2 are after each other in a cycle and this is just the intersection point
        for (PlaneIntersectionCycle cycle : planeCycles) {
            for (int i = 0; i < cycle.cycle.size(); i++) {
                PlaneIntersection cpi1 = cycle.cycle.get(i);
                PlaneIntersection cpi2 = cycle.cycle.get((i + 1) % cycle.cycle.size());
                if (pi1 == cpi1 && pi2 == cpi2 && pi1_percentage > 0.95 && pi2_percentage < 0.05) {
                    return true;
                }
                if (pi1 == cpi2 && pi2 == cpi1 && pi1_percentage < 0.05 && pi2_percentage > 0.95) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<PlaneIntersectionCycle> cutAtSelfIntersectionPoints(List<PlaneIntersectionCycle> planeCycles) {
        List<Point3D> allPoints = new ArrayList<>();
        List<PlaneIntersection> planeIntersections = planeCycles.stream().flatMap(cycle -> cycle.cycle.stream()).collect(Collectors.toList());
        for (int i = 0; i < planeIntersections.size(); i++) {
            for (int j = 0; j < i + 1; j++) {
                PlaneIntersection pi1 = planeIntersections.get(i);
                PlaneIntersection pi2 = planeIntersections.get(j);
                PythonFunctionData f1 = pi1.getPythonFunctionData("g");
                PythonFunctionData f2 = pi2.getPythonFunctionData("f");
                double[] data = PythonFunctionData.findAllIntersections(f1, f2);
                List<Point3D> points = new ArrayList<>();
                for (int k = 0; k < data.length; k+=2) {
                    if (!isFalsePositiveSelfIntersectionPoint(pi1, pi2, planeCycles, data[k], data[k + 1])) {
                        Point3D p1 = pi1.getPoint(data[k]);
                        Point3D p2 = pi2.getPoint(data[k + 1]);

                        points.add(p1);
                        points.add(p2);
                    }
                }
                allPoints.addAll(Point3D.clusterAndAvgPoints(points));
                System.out.println(Point3D.clusterAndAvgPoints(points));
            }
        }
        return planeCycles;
    }

    public List<PlaneIntersection> getPlaneIntersections() {
        return cycle;
    }

    public PlaneIntersectionCycle multiplyWithMatrix4x4(Matrix4x4 m) {
        return new PlaneIntersectionCycle(cycle.stream().map(pi -> pi.multiplyWithMatrix4x4(m)).collect(Collectors.toList()));
    }

    public static List<PlaneIntersectionCycle> getPlaneIntersectionCyclesFromPlaneIntersections(List<PlaneIntersection> planeIntersections) {
        List<PlaneIntersectionCycle> cycles = new ArrayList<>();
        Set<PlaneIntersection> allPlaneIntersections = new HashSet<>(planeIntersections);
        while (allPlaneIntersections.size() > 0) {
            List<PlaneIntersection> currentCycle = new ArrayList<>();
            PlaneIntersection currentPlaneIntersection = allPlaneIntersections.iterator().next();
            Point3D pointToMatch = currentPlaneIntersection.getLastPoint();
            allPlaneIntersections.remove(currentPlaneIntersection);
            currentCycle.add(currentPlaneIntersection);
            do {
                // Effectively final for lambda below
                Point3D finalPointToMatch = pointToMatch;

                if (allPlaneIntersections.isEmpty()) {
                    break;
                }
                PlaneIntersection closestPlaneIntersection = allPlaneIntersections.stream()
                        .min(
                                Comparator.comparingDouble(
                                        pi -> Math.min(
                                                pi.getFirstPoint().subtract(finalPointToMatch).distance0(),
                                                pi.getLastPoint().subtract(finalPointToMatch).distance0()
                                        )
                                )
                        ).get();

                double distanceToClosestSegment = Math.min(
                        closestPlaneIntersection.getFirstPoint().subtract(finalPointToMatch).distance0(),
                        closestPlaneIntersection.getLastPoint().subtract(finalPointToMatch).distance0()
                );
                if (!Utils.isRoughZero(distanceToClosestSegment)) {
                    break;
                }
                double closestPlaneIntersectionFirstPoint = closestPlaneIntersection.getFirstPoint().subtract(pointToMatch).distance0();
                double closestPlaneIntersectionLastPoint = closestPlaneIntersection.getLastPoint().subtract(pointToMatch).distance0();
                allPlaneIntersections.remove(closestPlaneIntersection);
                if (closestPlaneIntersectionLastPoint < closestPlaneIntersectionFirstPoint) {
                    closestPlaneIntersection = closestPlaneIntersection.reverse();
                }
                pointToMatch = closestPlaneIntersection.getLastPoint();
                currentCycle.add(closestPlaneIntersection);
                currentPlaneIntersection = closestPlaneIntersection;
            } while (true);
            cycles.add(new PlaneIntersectionCycle(currentCycle));
        }
        return cycles;
    }

    public static Optional<List<PlaneIntersectionCycle>> getPlaneIntersectionCyclesFromPlaneIntersections(Optional<List<PlaneIntersection>> planeIntersections) {
        return planeIntersections.map(PlaneIntersectionCycle::getPlaneIntersectionCyclesFromPlaneIntersections);
    }

    public PlaneIntersectionCycle reverse() {
        List<PlaneIntersection> newCycle = new ArrayList<>();
        for (PlaneIntersection pi : getPlaneIntersections()) {
            newCycle.add(pi.reverse());
        }
        Collections.reverse(newCycle);
        return new PlaneIntersectionCycle(newCycle);
    }
}