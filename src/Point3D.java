import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Point3D {
    protected double x, y, z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static List<Point3D> removeDuplicatesFromList(List<Point3D> points) {
        List<Point3D> newPoints = new ArrayList<>();
        originalPointsLoop: for (Point3D point : points) {
            for (Point3D existingPoint : newPoints) {
                if (point.equals(existingPoint)) {
                    continue originalPointsLoop;
                }
            }
            newPoints.add(point);
        }
        return newPoints;
    }

    public double distance0() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Point3D scale(double s) {
        return new Point3D(x * s, y * s, z * s);
    }

    public Point3D add(Point3D p) {
        return new Point3D(x + p.x, y + p.y, z + p.z);
    }

    public Point3D subtract(Point3D p) {
        return new Point3D(x - p.x, y - p.y, z - p.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point3D point3D = (Point3D) o;
        return Utils.isZero(point3D.x - x) && Utils.isZero(point3D.y - y) && Utils.isZero(point3D.z - z);
    }

    @Override
    public String toString() {
        return "Point3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public Point3D copy() {
        return new Point3D(x, y, z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double dot(Vector3D v) {
        return v.dot(this);
    }

    public static List<Point3D> clusterAndAvgPoints(List<Point3D> allPoints) {
        List<List<Point3D>> pointClusters = new ArrayList<>();
        for (Point3D point : allPoints) {
            boolean foundMatch = false;
            for (List<Point3D> pointCluster : pointClusters) {
                if (Utils.isRoughZero(pointCluster.get(0).subtract(point).distance0())) {
                    pointCluster.add(point);
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                pointClusters.add(new ArrayList<>() {{add(point);}});
            }
        }
        List<Point3D> points = new ArrayList<>();
        for (List<Point3D> pointCluster : pointClusters) {
            points.add(new Point3D(
                    pointCluster.stream().mapToDouble(Point3D::getX).sum() / pointCluster.size(),
                    pointCluster.stream().mapToDouble(Point3D::getY).sum() / pointCluster.size(),
                    pointCluster.stream().mapToDouble(Point3D::getZ).sum() / pointCluster.size()
            ));
        }
        return points;
    }
}
