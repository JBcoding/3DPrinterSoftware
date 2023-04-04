import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaneIntersectionCycle {
    private final List<PlaneIntersection> cycle;

    public PlaneIntersectionCycle(List<PlaneIntersection> cycle) {
        this.cycle = cycle;
    }

    public static List<Point3D> findSelfIntersectionPoints(List<PlaneIntersectionCycle> planeCycles) {
        List<Point3D> allPoints = new ArrayList<>();
        List<PlaneIntersection> planeIntersections = planeCycles.stream().flatMap(cycle -> cycle.cycle.stream()).collect(Collectors.toList());
        for (PlaneIntersection pi1 : planeIntersections) {
            for (PlaneIntersection pi2 : planeIntersections) {
                // TODO (mbjorn): Figure out how to compare intersections with itself, it can have self intersections (maybe bounds that never overlaps)
                if (pi1 != pi2) { // Yes object level comparison
                    PythonFunctionData f1 = pi1.getPythonFunctionData("g");
                    PythonFunctionData f2 = pi2.getPythonFunctionData("f");
                    double[] data = PythonFunctionData.findAllIntersections(f1, f2);
                    List<Point3D> points = new ArrayList<>();
                    for (int i = 0; i < data.length; i+=2) {
                        if ((data[i] < 0.05 || data[i] > 0.95) && (data[i + 1] < 0.05 || data[i + 1] > 0.95)) {
                            // TODO(mbjorn) check if these have endpoints meeting from the cycle
                            // For now we just assume they do
                        } else {
                            Point3D p1 = pi1.getPoint(data[i]);
                            Point3D p2 = pi2.getPoint(data[i + 1]);

                            //if (Utils.isRoughZero(p1.subtract(p2).distance0())) {
                                points.add(pi1.getPoint(data[i]));
                                points.add(pi2.getPoint(data[i + 1]));
                                System.out.println(pi1.getPoint(data[i]));
                                System.out.println(pi2.getPoint(data[i + 1]));
                                System.out.println(p1.subtract(p2).distance0());
                                System.out.println("");
                            //}
                        }
                    }
                    allPoints.addAll(points);
                }
            }
        }
        return allPoints;
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