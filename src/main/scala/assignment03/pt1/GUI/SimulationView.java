package assignment03.pt1.GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import assignment03.pt1.Body.*;
import assignment03.pt1.Boundary;
import assignment03.pt1.P2d.*;

/**
 * Simulation view
 *
 * @author aricci
 *
 */
public class SimulationView {

    private final VisualiserFrame frame;
    private ArrayList<Body> bodies = null;
    private Boundary bounds = null;

    /**
     * Creates a view of the specified size (in pixels)
     *
     * @param w
     * @param h
     */
    public SimulationView(int w, int h){
        frame = new VisualiserFrame(w,h);
    }

    public void display(double vt, long iter){

        if(bounds == null || bodies == null){
            throw new IllegalStateException("Bounds or bodies are not set");
        }
        frame.display(bodies, vt, iter, bounds);


    }


    public void setBodies(ArrayList<Body> bodies){
        this.bodies = bodies;
    }

    public void setBounds(Boundary bounds){
        this.bounds = bounds;
    }



    public static class VisualiserFrame extends JFrame {

        private static VisualiserPanel panel;
        private static JButton start;
        private static JButton stop;

        public VisualiserFrame(int w, int h){
            setTitle("Bodies Simulation");
            setSize(w,h);
            setResizable(false);
            panel = new VisualiserPanel(w,h);
            getContentPane().setLayout(new BorderLayout(0,0));
            JPanel panel2 = new JPanel();
            start = new JButton("Start");
            stop = new JButton("Stop");
            panel2.setLayout(new FlowLayout());
            panel2.add(start);
            panel2.add(stop);
            getContentPane().add(panel2, BorderLayout.SOUTH);

            getContentPane().add(panel, BorderLayout.CENTER);
            addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent ev){
                    System.exit(0);
                }
                public void windowClosed(WindowEvent ev){
                    System.exit(0);
                }
            });
            this.setVisible(true);
        }

        public static void setFocusOnSimulation(){
            panel.requestFocusInWindow();
        }

        public static void setStartHandler(ActionListener al){
            start.addActionListener(al);
        }

        public static void setStopHandler(ActionListener al){
            stop.addActionListener(al);
        }

        public void display(ArrayList<Body> bodies, double vt, long iter, Boundary bounds){
            try {
                SwingUtilities.invokeAndWait(() -> {
                    panel.display(bodies, vt, iter, bounds);
                    repaint();
                });
            } catch (Exception ex) {}
        };

        public void updateScale(double k) {
            panel.updateScale(k);
        }


    }

    public static class VisualiserPanel extends JPanel implements KeyListener {

        private ArrayList<Body> bodies;
        private Boundary bounds;

        private long nIter;
        private double vt;
        private double scale = 1;

        private long dx;
        private long dy;

        public VisualiserPanel(int w, int h){
            setSize(w,h);
            dx = w/2 - 20;
            dy = h/2 - 20;
            this.addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            requestFocusInWindow();
        }

        public void paint(Graphics g){
            if (bodies != null) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2.clearRect(0,0,this.getWidth(),this.getHeight());


                int x0 = getXcoord(bounds.x0());
                int y0 = getYcoord(bounds.y0());

                int wd = getXcoord(bounds.x1()) - x0;
                int ht = y0 - getYcoord(bounds.y1());

                g2.drawRect(x0, y0 - ht, wd, ht);

                bodies.forEach( b -> {
                    P2d p = b.pos();
                    int radius = (int) (10*scale);
                    if (radius < 1) {
                        radius = 1;
                    }
                    g2.drawOval(getXcoord(p.x()),getYcoord(p.y()), radius, radius);
                });
                String time = String.format("%.2f", vt);
                g2.drawString("Bodies: " + bodies.size() + " - vt: " + time + " - nIter: " + nIter + " (UP for zoom in, DOWN for zoom out)", 2, 20);
            }
        }

        private int getXcoord(double x) {
            return (int)(dx + x*dx*scale);
        }

        private int getYcoord(double y) {
            return (int)(dy - y*dy*scale);
        }


        public void display(ArrayList<Body> bodies, double vt, long iter, Boundary bounds){
            this.bodies = bodies;
            this.bounds = bounds;
            this.vt = vt;
            this.nIter = iter;
        }

        public void updateScale(double k) {
            scale *= k;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 38){  		/* KEY UP */
                scale *= 1.1;
            } else if (e.getKeyCode() == 40){  	/* KEY DOWN */
                scale *= 0.9;
            }
        }

        public void keyReleased(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
    }
}

