import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaneIntersectionCycle {
    private final List<PlaneIntersection> cycle;

    public PlaneIntersectionCycle(List<PlaneIntersection> cycle) {
        this.cycle = cycle;
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