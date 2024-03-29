public class Utils {
    public static final double eps = 0.0000001d;

    public static boolean isZero(double d) {
        return Math.abs(d) < eps;
    }

    public static boolean isRoughZero(double d) {
        return Math.abs(d) < 0.01;
    }

    public static boolean isPositive(double d) {
        return d > 0 && !isZero(d);
    }
}
