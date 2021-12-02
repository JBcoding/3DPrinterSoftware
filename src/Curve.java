import java.util.ArrayList;
import java.util.List;

public class Curve implements PlaneIntersection {
    Function xt, yt, zt;
    double t0, t1;

    public Curve(Function xt, Function yt, Function zt, double t0, double t1) {
        this.xt = xt;
        this.yt = yt;
        this.zt = zt;
        this.t0 = t0;
        this.t1 = t1;
    }

    @Override
    public String toString() {
        return "Curve{" +
                "xt=" + xt +
                ", yt=" + yt +
                ", zt=" + zt +
                ", t0=" + t0 +
                ", t1=" + t1 +
                '}';
    }

    @Override
    public String getGeoGebraString() {
        return String.format("Curve(%s, %s, %s, t, %s, %s)", xt, yt, zt, t0, t1);
    }

    @Override
    public List<Point3D> getPoints(int maxPoints) {
        double tDelta = (t1 - t0) / (maxPoints - 1);
        List<Point3D> points = new ArrayList<>();
        for (int i = 0; i < maxPoints; i++) {
            double t = t0 + tDelta * i;
            points.add(new Point3D(xt.calculateValue(t), yt.calculateValue(t), zt.calculateValue(t)));
        }
        return points;
    }

    @Override
    public List<Point3D> getDeltaPoints(double delta) {
        double tDelta = 0.01;
        Point3D p1 = new Point3D(xt.calculateValue(t0), yt.calculateValue(t0), zt.calculateValue(t0));
        Point3D p2 = new Point3D(xt.calculateValue(t0 + tDelta), yt.calculateValue(t0 + tDelta), zt.calculateValue(t0 + tDelta));
        double p1p2Distance = p1.subtract(p2).distance0();
        double estimatedDistancePerUnitT = p1p2Distance / tDelta;
        double estimatedTotalDistance = estimatedDistancePerUnitT * (t1 - t0);
        return getPoints((int) (estimatedTotalDistance / delta + 1));
    }

    @Override
    public PlaneIntersection multiplyWithMatrix4x4(Matrix4x4 m) {
        Function newXt = new Function(Function.Operator.ADD, new Function(Function.Operator.MULTIPLY, this.xt, m.matrix[0][0]), new Function(Function.Operator.MULTIPLY, this.yt, m.matrix[0][1]), new Function(Function.Operator.MULTIPLY, this.zt, m.matrix[0][2]), m.matrix[0][3]);
        Function newYt = new Function(Function.Operator.ADD, new Function(Function.Operator.MULTIPLY, this.xt, m.matrix[1][0]), new Function(Function.Operator.MULTIPLY, this.yt, m.matrix[1][1]), new Function(Function.Operator.MULTIPLY, this.zt, m.matrix[1][2]), m.matrix[1][3]);
        Function newZt = new Function(Function.Operator.ADD, new Function(Function.Operator.MULTIPLY, this.xt, m.matrix[2][0]), new Function(Function.Operator.MULTIPLY, this.yt, m.matrix[2][1]), new Function(Function.Operator.MULTIPLY, this.zt, m.matrix[2][2]), m.matrix[2][3]);
        return new Curve(newXt, newYt, newZt, t0, t1);
    }

    @Override
    public Point3D getFirstPoint() {
        return new Point3D(xt.calculateValue(t0), yt.calculateValue(t0), zt.calculateValue(t0));
    }

    @Override
    public Point3D getLastPoint() {
        return new Point3D(xt.calculateValue(t1), yt.calculateValue(t1), zt.calculateValue(t1));
    }

    @Override
    public PlaneIntersection getSubIntersection(double startPercentage, double endPercentage) {
        double tDelta = t1 - t0;
        return new Curve(xt, yt, zt, t0 + tDelta * startPercentage, t0 + tDelta * endPercentage);
    }

    @Override
    public PlaneIntersection offsetXYPlane(double offset) {
        Function xdt = xt.differentiate();
        Function ydt = yt.differentiate();
        Function xdt2 = new Function(Function.Operator.MULTIPLY, xdt, xdt);
        Function ydt2 = new Function(Function.Operator.MULTIPLY, ydt, ydt);
        Function sqrt_xdt2_ydt2 = new Function(Function.Operator.SQRT, new Function(Function.Operator.ADD, xdt2, ydt2));
        Function offsetXt = new Function(Function.Operator.ADD, xt, new Function(Function.Operator.DIVIDE, new Function(Function.Operator.MULTIPLY, offset, ydt), sqrt_xdt2_ydt2));
        Function offsetYt = new Function(Function.Operator.ADD, yt, new Function(Function.Operator.MULTIPLY, -1, new Function(Function.Operator.DIVIDE, new Function(Function.Operator.MULTIPLY, offset, xdt), sqrt_xdt2_ydt2)));
        return new Curve(offsetXt, offsetYt, zt, t0, t1);
    }
}
