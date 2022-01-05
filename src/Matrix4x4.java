import java.util.Arrays;
import java.util.stream.Collectors;

public class Matrix4x4 {
    protected double[][] matrix;

    public Matrix4x4(double[][] matrix) {
        this.matrix = deepCopy2dDoubleArray(matrix);
    }

    public static Matrix4x4 IDENTITY() {
        return new Matrix4x4(new double[][]{{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}});
    }

    public static Matrix4x4 getRotationMatrixAroundXAxis(double theta) {
        return new Matrix4x4(new double[][]{
                {1, 0, 0, 0},
                {0, Math.cos(theta), -Math.sin(theta), 0},
                {0, Math.sin(theta), Math.cos(theta), 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 getRotationMatrixAroundYAxis(double theta) {
        return new Matrix4x4(new double[][]{
                {Math.cos(theta), 0, Math.sin(theta), 0},
                {0, 1, 0, 0},
                {-Math.sin(theta), 0, Math.cos(theta), 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 getRotationMatrixAroundZAxis(double theta) {
        return new Matrix4x4(new double[][]{
                {Math.cos(theta), -Math.sin(theta), 0, 0},
                {Math.sin(theta), Math.cos(theta), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 getMovementMatrix(Vector3D v) {
        return new Matrix4x4(new double[][]{
                {1, 0, 0, v.getX()},
                {0, 1, 0, v.getY()},
                {0, 0, 1, v.getZ()},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 getMovementMatrix(Point3D p) {
        return getMovementMatrix(new Vector3D(p));
    }

    public static Matrix4x4 getStretchingMatrixInTheXAxis(double factor) {
        return new Matrix4x4(new double[][]{
                {factor, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 getStretchingMatrixInTheYAxis(double factor) {
        return new Matrix4x4(new double[][]{
                {1, 0, 0, 0},
                {0, factor, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4x4 getStretchingMatrixInTheZAxis(double factor) {
        return new Matrix4x4(new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, factor, 0},
                {0, 0, 0, 1}
        });
    }

    public Matrix4x4 multiply(Matrix4x4 matrix4x4) {
        double newMatrix[][] = new double[4][4];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                newMatrix[x][y] = 0;
                for (int i = 0; i < 4; i++) {
                    newMatrix[x][y] += this.matrix[x][i] * matrix4x4.matrix[i][y];
                }
            }
        }
        return new Matrix4x4(newMatrix);
    }

    public Vector3D multiply(Vector3D v) {
        return multiply(v, 1);
    }

    public Vector3D multiply(Vector3D v, double w) {
        double x = matrix[0][0] * v.getX() + matrix[0][1] * v.getY() + matrix[0][2] * v.getZ() + matrix[0][3] * w;
        double y = matrix[1][0] * v.getX() + matrix[1][1] * v.getY() + matrix[1][2] * v.getZ() + matrix[1][3] * w;
        double z = matrix[2][0] * v.getX() + matrix[2][1] * v.getY() + matrix[2][2] * v.getZ() + matrix[2][3] * w;
        return new Vector3D(x, y, z);
    }

    public Point3D multiply(Point3D p) {
        return multiply(new Vector3D(p)).toPoint3D();
    }

    public Point3D multiply(Point3D p, double w) {
        return multiply(new Vector3D(p), w).toPoint3D();
    }

    public PlaneIntersection multiply(PlaneIntersection pi) {
        return pi.multiplyWithMatrix4x4(this);
    }

    public PlaneIntersectionCycle multiply(PlaneIntersectionCycle pic) {
        return pic.multiplyWithMatrix4x4(this);
    }

    public Plane multiply(Plane p) {
        // https://stackoverflow.com/a/7706849
        Vector3D n = p.getNormalVector();
        Point3D o = n.toPoint3D().scale(p.planeDistance);

        Point3D newO = multiply(o);
        Vector3D newN = this.inverse().transpose().multiply(n, 0);
        newN = newN.normalise();

        double d = newO.dot(newN);

        return new Plane(newN, d);
    }

    public Matrix4x4 transpose() {
        double[][] tran = new double[4][4];

        tran[0][0] = matrix[0][0];
        tran[0][1] = matrix[1][0];
        tran[0][2] = matrix[2][0];
        tran[0][3] = matrix[3][0];

        tran[1][0] = matrix[0][1];
        tran[1][1] = matrix[1][1];
        tran[1][2] = matrix[2][1];
        tran[1][3] = matrix[3][1];

        tran[2][0] = matrix[0][2];
        tran[2][1] = matrix[1][2];
        tran[2][2] = matrix[2][2];
        tran[2][3] = matrix[3][2];

        tran[3][0] = matrix[0][3];
        tran[3][1] = matrix[1][3];
        tran[3][2] = matrix[2][3];
        tran[3][3] = matrix[3][3];

        return new Matrix4x4(tran);
    }

    public Matrix4x4 inverse() {
        double[][] inv = new double[4][4];

        inv[0][0] = this.matrix[1][1]  * this.matrix[2][2] * this.matrix[3][3] -
                this.matrix[1][1]  * this.matrix[2][3] * this.matrix[3][2] -
                this.matrix[2][1]  * this.matrix[1][2]  * this.matrix[3][3] +
                this.matrix[2][1]  * this.matrix[1][3]  * this.matrix[3][2] +
                this.matrix[3][1] * this.matrix[1][2]  * this.matrix[2][3] -
                this.matrix[3][1] * this.matrix[1][3]  * this.matrix[2][2];

        inv[1][0] = -this.matrix[1][0]  * this.matrix[2][2] * this.matrix[3][3] +
                this.matrix[1][0]  * this.matrix[2][3] * this.matrix[3][2] +
                this.matrix[2][0]  * this.matrix[1][2]  * this.matrix[3][3] -
                this.matrix[2][0]  * this.matrix[1][3]  * this.matrix[3][2] -
                this.matrix[3][0] * this.matrix[1][2]  * this.matrix[2][3] +
                this.matrix[3][0] * this.matrix[1][3]  * this.matrix[2][2];

        inv[2][0] = this.matrix[1][0]  * this.matrix[2][1] * this.matrix[3][3] -
                this.matrix[1][0]  * this.matrix[2][3] * this.matrix[3][1] -
                this.matrix[2][0]  * this.matrix[1][1] * this.matrix[3][3] +
                this.matrix[2][0]  * this.matrix[1][3] * this.matrix[3][1] +
                this.matrix[3][0] * this.matrix[1][1] * this.matrix[2][3] -
                this.matrix[3][0] * this.matrix[1][3] * this.matrix[2][1];

        inv[3][0] = -this.matrix[1][0]  * this.matrix[2][1] * this.matrix[3][2] +
                this.matrix[1][0]  * this.matrix[2][2] * this.matrix[3][1] +
                this.matrix[2][0]  * this.matrix[1][1] * this.matrix[3][2] -
                this.matrix[2][0]  * this.matrix[1][2] * this.matrix[3][1] -
                this.matrix[3][0] * this.matrix[1][1] * this.matrix[2][2] +
                this.matrix[3][0] * this.matrix[1][2] * this.matrix[2][1];

        inv[0][1] = -this.matrix[0][1]  * this.matrix[2][2] * this.matrix[3][3] +
                this.matrix[0][1]  * this.matrix[2][3] * this.matrix[3][2] +
                this.matrix[2][1]  * this.matrix[0][2] * this.matrix[3][3] -
                this.matrix[2][1]  * this.matrix[0][3] * this.matrix[3][2] -
                this.matrix[3][1] * this.matrix[0][2] * this.matrix[2][3] +
                this.matrix[3][1] * this.matrix[0][3] * this.matrix[2][2];

        inv[1][1] = this.matrix[0][0]  * this.matrix[2][2] * this.matrix[3][3] -
                this.matrix[0][0]  * this.matrix[2][3] * this.matrix[3][2] -
                this.matrix[2][0]  * this.matrix[0][2] * this.matrix[3][3] +
                this.matrix[2][0]  * this.matrix[0][3] * this.matrix[3][2] +
                this.matrix[3][0] * this.matrix[0][2] * this.matrix[2][3] -
                this.matrix[3][0] * this.matrix[0][3] * this.matrix[2][2];

        inv[2][1] = -this.matrix[0][0]  * this.matrix[2][1] * this.matrix[3][3] +
                this.matrix[0][0]  * this.matrix[2][3] * this.matrix[3][1] +
                this.matrix[2][0]  * this.matrix[0][1] * this.matrix[3][3] -
                this.matrix[2][0]  * this.matrix[0][3] * this.matrix[3][1] -
                this.matrix[3][0] * this.matrix[0][1] * this.matrix[2][3] +
                this.matrix[3][0] * this.matrix[0][3] * this.matrix[2][1];

        inv[3][1] = this.matrix[0][0]  * this.matrix[2][1] * this.matrix[3][2] -
                this.matrix[0][0]  * this.matrix[2][2] * this.matrix[3][1] -
                this.matrix[2][0]  * this.matrix[0][1] * this.matrix[3][2] +
                this.matrix[2][0]  * this.matrix[0][2] * this.matrix[3][1] +
                this.matrix[3][0] * this.matrix[0][1] * this.matrix[2][2] -
                this.matrix[3][0] * this.matrix[0][2] * this.matrix[2][1];

        inv[0][2] = this.matrix[0][1]  * this.matrix[1][2] * this.matrix[3][3] -
                this.matrix[0][1]  * this.matrix[1][3] * this.matrix[3][2] -
                this.matrix[1][1]  * this.matrix[0][2] * this.matrix[3][3] +
                this.matrix[1][1]  * this.matrix[0][3] * this.matrix[3][2] +
                this.matrix[3][1] * this.matrix[0][2] * this.matrix[1][3] -
                this.matrix[3][1] * this.matrix[0][3] * this.matrix[1][2];

        inv[1][2] = -this.matrix[0][0]  * this.matrix[1][2] * this.matrix[3][3] +
                this.matrix[0][0]  * this.matrix[1][3] * this.matrix[3][2] +
                this.matrix[1][0]  * this.matrix[0][2] * this.matrix[3][3] -
                this.matrix[1][0]  * this.matrix[0][3] * this.matrix[3][2] -
                this.matrix[3][0] * this.matrix[0][2] * this.matrix[1][3] +
                this.matrix[3][0] * this.matrix[0][3] * this.matrix[1][2];

        inv[2][2] = this.matrix[0][0]  * this.matrix[1][1] * this.matrix[3][3] -
                this.matrix[0][0]  * this.matrix[1][3] * this.matrix[3][1] -
                this.matrix[1][0]  * this.matrix[0][1] * this.matrix[3][3] +
                this.matrix[1][0]  * this.matrix[0][3] * this.matrix[3][1] +
                this.matrix[3][0] * this.matrix[0][1] * this.matrix[1][3] -
                this.matrix[3][0] * this.matrix[0][3] * this.matrix[1][1];

        inv[3][2] = -this.matrix[0][0]  * this.matrix[1][1] * this.matrix[3][2] +
                this.matrix[0][0]  * this.matrix[1][2] * this.matrix[3][1] +
                this.matrix[1][0]  * this.matrix[0][1] * this.matrix[3][2] -
                this.matrix[1][0]  * this.matrix[0][2] * this.matrix[3][1] -
                this.matrix[3][0] * this.matrix[0][1] * this.matrix[1][2] +
                this.matrix[3][0] * this.matrix[0][2] * this.matrix[1][1];

        inv[0][3] = -this.matrix[0][1] * this.matrix[1][2] * this.matrix[2][3] +
                this.matrix[0][1] * this.matrix[1][3] * this.matrix[2][2] +
                this.matrix[1][1] * this.matrix[0][2] * this.matrix[2][3] -
                this.matrix[1][1] * this.matrix[0][3] * this.matrix[2][2] -
                this.matrix[2][1] * this.matrix[0][2] * this.matrix[1][3] +
                this.matrix[2][1] * this.matrix[0][3] * this.matrix[1][2];

        inv[1][3] = this.matrix[0][0] * this.matrix[1][2] * this.matrix[2][3] -
                this.matrix[0][0] * this.matrix[1][3] * this.matrix[2][2] -
                this.matrix[1][0] * this.matrix[0][2] * this.matrix[2][3] +
                this.matrix[1][0] * this.matrix[0][3] * this.matrix[2][2] +
                this.matrix[2][0] * this.matrix[0][2] * this.matrix[1][3] -
                this.matrix[2][0] * this.matrix[0][3] * this.matrix[1][2];

        inv[2][3] = -this.matrix[0][0] * this.matrix[1][1] * this.matrix[2][3] +
                this.matrix[0][0] * this.matrix[1][3] * this.matrix[2][1] +
                this.matrix[1][0] * this.matrix[0][1] * this.matrix[2][3] -
                this.matrix[1][0] * this.matrix[0][3] * this.matrix[2][1] -
                this.matrix[2][0] * this.matrix[0][1] * this.matrix[1][3] +
                this.matrix[2][0] * this.matrix[0][3] * this.matrix[1][1];

        inv[3][3] = this.matrix[0][0] * this.matrix[1][1] * this.matrix[2][2] -
                this.matrix[0][0] * this.matrix[1][2] * this.matrix[2][1] -
                this.matrix[1][0] * this.matrix[0][1] * this.matrix[2][2] +
                this.matrix[1][0] * this.matrix[0][2] * this.matrix[2][1] +
                this.matrix[2][0] * this.matrix[0][1] * this.matrix[1][2] -
                this.matrix[2][0] * this.matrix[0][2] * this.matrix[1][1];

        double det = this.matrix[0][0] * inv[0][0] + this.matrix[0][1] * inv[1][0] + this.matrix[0][2] * inv[2][0] + this.matrix[0][3] * inv[3][0];

        if (det == 0) {
            return null;
        }

        det = 1.0 / det;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                inv[i][j] *= det;
            }
        }

        Matrix4x4 inverse = new Matrix4x4(inv);

        return inverse;
    }

    private double[][] deepCopy2dDoubleArray(double[][] arr) {
        double[][] newArr = new double[arr.length][];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = new double[arr[i].length];
            for (int j = 0; j < arr[i].length; j++) {
                newArr[i][j] = arr[i][j];
            }
        }
        return newArr;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Matrix4x4{");
        for (int x = 0; x < 4; x++) {
            s.append("\n\t[");
            s.append(Arrays.stream(matrix[x]).mapToObj(Double::toString).collect(Collectors.joining(", ")));
            s.append("]");
        }
        s.append("\n}");
        return s.toString();
    }
}
