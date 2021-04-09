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
}
