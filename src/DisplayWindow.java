import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DisplayWindow extends JPanel {
    static double planeHeight = 0.5;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo");

        DisplayWindow canvas = new DisplayWindow(); // TODO separate out the canvas to its own file
        canvas.setBounds(0, 0, 400, 400);
        frame.add(canvas);

        JSlider heightSlider = new JSlider(JSlider.VERTICAL, -1200, 1200, 500);
        heightSlider.setBounds(400, 0, 100, 400);
        heightSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                planeHeight = heightSlider.getValue() / 1000d;
                canvas.repaint();
            }
        });
        frame.add(heightSlider);


        frame.setSize(550, 400);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphic2d = (Graphics2D) g;
        graphic2d.setColor(Color.BLACK);

        BaseObject b = new UnitCylinder();
        Vector3D planeNormal = new Vector3D(0, 0, 1);
        Matrix4x4 m = Matrix4x4.getRotationMatrixAroundYAxis(Math.PI / 4);
        planeNormal = m.multiply(planeNormal);
        Plane p = new Plane(planeNormal, planeHeight);
        Optional<List<PlaneIntersection>> intersection = b.getPlaneIntersection(p);
        for (PlaneIntersection pi : intersection.orElse(new ArrayList<>())) {
            pi = m.inverse().multiply(pi);
            List<Point3D> points = pi.getPoints(100);
            int[] xPoints = points.stream().map(Point3D::getX).mapToInt(v -> (int) (v * 100) + 200).toArray();
            int[] yPoints = points.stream().map(Point3D::getY).mapToInt(v -> (int) (v * 100) + 200).toArray();
            graphic2d.drawPolyline(xPoints, yPoints, points.size());
        }
    }
}
