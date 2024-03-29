import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import javafx.scene.input.KeyCode;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class DisplayWindow extends JPanel implements GLEventListener {
    static GLU glu;
    static GLCanvas canvas;
    static FPSAnimator animator;

    static double RENDER_DISTANCE = 100;


    private double fovX, fovY, width, height;
    private int[] viewport;
    private double[] mvmatrix;
    private double[] projmatrix;

    private int frame_count = 0;

    private double xAngle = 315;
    private double yAngle = 22.5;

    boolean goingUp, goingDown, goingRight, goingLeft, goingForward, goingBackwards;
    boolean draggingMouse;
    double lastMouseCoordX, lastMouseCoordY;

    private List<Pair<Vector3D, Vector3D>> objectLines = new ArrayList<>();

    private Vector3D position = new Vector3D(5, 3, 5);

    public DisplayWindow() {
        super();

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateObjectLines();
            }
        }).start();

        JFrame frame = new JFrame("Demo");

        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        glu = new GLU();

        final GLCanvas glcanvas = new GLCanvas(capabilities);
        canvas = glcanvas;

        animator = new FPSAnimator(canvas, 60);
        animator.setUpdateFPSFrames(20, null);
        animator.start();

        glcanvas.addGLEventListener(this);
        glcanvas.setPreferredSize(new Dimension(1200, 900));

        glcanvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 81) {
                    goingUp = true;
                } else if (e.getKeyCode() == 69) {
                    goingDown = true;
                } else if (e.getKeyCode() == 87) {
                    goingForward = true;
                } else if (e.getKeyCode() == 83) {
                    goingBackwards = true;
                } else if (e.getKeyCode() == 65) {
                    goingLeft = true;
                } else if (e.getKeyCode() == 68) {
                    goingRight = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 81) {
                    goingUp = false;
                } else if (e.getKeyCode() == 69) {
                    goingDown = false;
                } else if (e.getKeyCode() == 87) {
                    goingForward = false;
                } else if (e.getKeyCode() == 83) {
                    goingBackwards = false;
                } else if (e.getKeyCode() == 65) {
                    goingLeft = false;
                } else if (e.getKeyCode() == 68) {
                    goingRight = false;
                }
            }
        });

        glcanvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                draggingMouse = true;
                lastMouseCoordX = e.getX();
                lastMouseCoordY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingMouse = false;
                // update
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        glcanvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double draggedPixelToAngleRatio = 0.05;
                xAngle += (lastMouseCoordX - e.getX()) * draggedPixelToAngleRatio;
                yAngle += (lastMouseCoordY - e.getY()) * draggedPixelToAngleRatio;
                lastMouseCoordX = e.getX();
                lastMouseCoordY = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        frame.add(glcanvas);

        frame.setSize(1200, 900);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void updateObjectLines() {
        objectLines = new ArrayList<>();

        int linesPerUnit = 1;
        int startingHeight = -3;
        int endingHeight = 5;

        MultiPartObject b1 = new DeformedBaseObject(new UnitCylinder());
        Matrix4x4 m = Matrix4x4.getMovementMatrix(new Vector3D(0, 1.9, 0))
                .multiply(Matrix4x4.getMovementMatrix(new Vector3D(0, 0, 1)))
                .multiply(Matrix4x4.getRotationMatrixAroundXAxis(Math.PI / 2.5))
                .multiply(Matrix4x4.getStretchingMatrixInTheZAxis(.9));
        b1.setDeformationMatrix(m);

        MultiPartObject b2 = new DeformedBaseObject(new UnitSphere());
        b2.setDeformationMatrix(
                Matrix4x4.getMovementMatrix(new Vector3D(0, 1, 1))
                .multiply(Matrix4x4.getStretchingMatrixInTheXAxis(1.2))
                .multiply(Matrix4x4.getStretchingMatrixInTheYAxis(1.2))
                .multiply(Matrix4x4.getStretchingMatrixInTheZAxis(1.2))
        );

        MultiPartObject b = new SubtractionObject(b2, b1);

        Vector3D planeNormal = new Vector3D(0, 0, 1);
        for (int i = startingHeight * linesPerUnit; i < endingHeight * linesPerUnit; i++) {
            Plane p = new Plane(planeNormal, (double) i / linesPerUnit);

            Optional<List<PlaneIntersectionCycle>> intersection = b.getPlaneIntersection(p);
            List<PlaneIntersectionCycle> intersectionToDraw = new ArrayList<>();
            if (intersection.isPresent()) {
                intersectionToDraw.addAll(intersection.get());
                intersectionToDraw.addAll(b.getPlaneIntersectionWithOffset(intersection.get(), .25).get());
            }
            for (PlaneIntersectionCycle pic : intersectionToDraw) {
                for (PlaneIntersection pi : pic.getPlaneIntersections()) {
                    List<Point3D> points = pi.getPoints(100);
                    double[] xPoints = points.stream().mapToDouble(Point3D::getX).toArray();
                    double[] yPoints = points.stream().mapToDouble(Point3D::getY).toArray();
                    double[] zPoints = points.stream().mapToDouble(Point3D::getZ).toArray();
                    for (int j = 0; j < points.size() - 1; j++) {
                        objectLines.add(new Pair<>(new Vector3D(xPoints[j], zPoints[j], yPoints[j]), new Vector3D(xPoints[j + 1], zPoints[j + 1], yPoints[j + 1])));
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        DisplayWindow dw = new DisplayWindow();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        frame_count += 1;

        double movementSpeed = 0.05;
        if (goingUp) {
            position = new Vector3D(position.getX(), position.getY() + movementSpeed, position.getZ());
        }
        if (goingDown) {
            position = new Vector3D(position.getX(), position.getY() - movementSpeed, position.getZ());
        }
        if (goingForward) {
            position = new Vector3D(position.getX() + Math.sin(xAngle / 180 * Math.PI) * movementSpeed, position.getY(), position.getZ() - Math.cos(xAngle / 180 * Math.PI) * movementSpeed);
        }
        if (goingBackwards) {
            position = new Vector3D(position.getX() - Math.sin(xAngle / 180 * Math.PI) * movementSpeed, position.getY(), position.getZ() + Math.cos(xAngle / 180 * Math.PI) * movementSpeed);
        }
        if (goingLeft) {
            position = new Vector3D(position.getX() - Math.cos(xAngle / 180 * Math.PI) * movementSpeed, position.getY(), position.getZ() - Math.sin(xAngle / 180 * Math.PI) * movementSpeed);
        }
        if (goingRight) {
            position = new Vector3D(position.getX() + Math.cos(xAngle / 180 * Math.PI) * movementSpeed, position.getY(), position.getZ() + Math.sin(xAngle / 180 * Math.PI) * movementSpeed);
        }


        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        gl.glEnable(gl.GL_DEPTH_TEST);

        gl.glEnable(gl.GL_BLEND);
        gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLoadIdentity();


        float[] lightPos = { 0, 0,0,1 };        // light position
        float[] noAmbient = { 0, 0, 0, 1f };     // low ambient light
        float[] diffuse = { .4f, .4f, .4f, 1f };        // full diffuse colour
        float[] specular = { 1f, 1f, 1f, 1f };        // full diffuse colour

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION);
        gl.glMaterialiv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new int[]{1, 1, 1, 1}, 0);
        gl.glMaterialiv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, new int[]{0, 0, 0, 1}, 0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, noAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION,lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR,specular, 0);


        gl.glRotated(yAngle, 1.0f, 0.0f, 0.0f);
        gl.glRotated(xAngle, 0.0f, 1.0f, 0.0f);
        gl.glTranslated(-position.getX(), -position.getY(), -position.getZ());


        gl.glColor3d(0.8, 0.8, 0.8);
        gl.glLineWidth(2);

        for (int i = 0; i < objectLines.size(); i++) {
            Pair<Vector3D, Vector3D> line = objectLines.get(i);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(line.getKey().getX(), line.getKey().getY(), line.getKey().getZ());
            gl.glVertex3d(line.getValue().getX(), line.getValue().getY(), line.getValue().getZ());
            gl.glEnd();

        }

        gl.glColor3d(0.4, 0.4, 0.4);
        double lrd = 10;
        for (int i = (int) -lrd; i <= lrd; i+= 1) {
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(lrd, 0, i);
            gl.glVertex3d(-lrd, 0, i);
            gl.glEnd();

            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(i, 0, lrd);
            gl.glVertex3d(i, 0, -lrd);
            gl.glEnd();
        }

        gl.glColor3d(0.2, 0.2, 0.2);
        double factor = 4;
        for (int i = (int) ((int) -lrd  * factor); i <= lrd * factor; i+= 1) {
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(lrd, 0, i / factor);
            gl.glVertex3d(-lrd, 0, i / factor);
            gl.glEnd();

            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(i / factor, 0, lrd);
            gl.glVertex3d(i / factor, 0, -lrd);
            gl.glEnd();
        }
        gl.glPopMatrix();

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = (GL2) drawable.getGL();

        height = (height == 0) ? 1 : height; // prevent divide by zero
        float aspect = (float)width / height;

        // Set the current view port to cover full screen
        gl.glViewport(0, 0, width, height);

        // Set up the projection matrix - choose perspective view
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity(); // reset
        // Angle of view (fovy) is 45 degrees (in the up y-direction). Based on this
        // canvas's aspect ratio. Clipping z-near is 0.1f and z-near is 100.0f.
        fovY = 50.0;
        fovX = fovY * aspect;
        this.width = width / 2;
        this.height = height / 2;

        if (canvas != null) {
            this.width = canvas.getWidth();
            this.height = canvas.getHeight();
        }
        glu.gluPerspective(fovY, aspect, .1f, RENDER_DISTANCE); // fovy, aspect, zNear, zFar

        // Enable the model-view transform
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity(); // reset

        this.viewport = new int[4];
        this.mvmatrix = new double[16];
        this.projmatrix = new double[16];

        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);

        this.viewport[2] = canvas.getWidth();
        this.viewport[3] = canvas.getHeight();

    }
}
