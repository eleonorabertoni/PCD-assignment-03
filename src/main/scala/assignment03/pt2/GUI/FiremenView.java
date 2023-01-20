package assignment03.pt2.GUI;

import assignment03.pt1.main.Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simulation view
 *
 * @author aricci
 *
 */
public class FiremenView {

    private final VisualiserFrame frame;
    private Boundary bounds = null;

    /**
     * Creates a view of the specified size (in pixels)
     *
     * @param w
     * @param h
     */
    public FiremenView(int w, int h) {
        frame = new VisualiserFrame(w, h);
    }

    public void display(double vt, long iter) {
        if(bounds == null){
            System.out.println("CIAO");
            throw new IllegalStateException("Bounds are not set");
        }else {
            System.out.println("WUT");
            frame.display(vt, iter, bounds);
        }





    }

    public void setBounds(Boundary bounds) {
        this.bounds = bounds;
    }


    public static class VisualiserFrame extends JFrame {

        private static VisualiserPanel panel;
        private static JButton disableButton;

        public VisualiserFrame(int w, int h) {
            setSize(w, h);
            setResizable(false);
            panel = new VisualiserPanel(w, h);
            getContentPane().setLayout(new BorderLayout(0, 0));
            JPanel panel2 = new JPanel();
            disableButton = new JButton("Disable Alarm");
            panel2.setLayout(new FlowLayout());
            panel2.add(disableButton);
            getContentPane().add(panel2, BorderLayout.SOUTH);

            getContentPane().add(panel, BorderLayout.CENTER);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent ev) {
                    System.exit(0);
                }

                public void windowClosed(WindowEvent ev) {
                    System.exit(0);
                }
            });
            this.setVisible(true);
        }

        public static void setFocusOnSimulation() {
            panel.requestFocusInWindow();
        }

        public static void setDisableButton(ActionListener al) {
            disableButton.addActionListener(al);
        }

        public void display(double vt, long iter, Boundary bounds) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    panel.display(vt, iter, bounds);
                    repaint();
                });
            } catch (Exception ex) {}
        };

        public void updateScale(double k) {
            panel.updateScale(k);
        }


    }

    public static class VisualiserPanel extends JPanel implements KeyListener {

        private Boundary bounds;

        private long nIter;
        private double vt;
        private double scale = 0.3;

        private long dx;
        private long dy;

        public VisualiserPanel(int w, int h) {
            setSize(w, h);
            dx = w / 2; //-20 anche qui prima
            dy = h / 2 - 20;
            this.addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            requestFocusInWindow();
        }

        public void paint(Graphics g) {
            if (bounds != null) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2.clearRect(0, 0, this.getWidth(), this.getHeight());

                int x0 = getXcoord(bounds.x0());
                int y0 = getYcoord(bounds.y0());

                int wd = getXcoord(bounds.x1()) - x0;
                int ht = y0 - getYcoord(bounds.y1());

                g2.drawRect(x0, y0 - ht, wd, ht);

                String time = String.format("%.2f", vt);
                g2.drawString(" - vt: " + time + " - nIter: " + nIter + " (UP for zoom in, DOWN for zoom out)", 2, 20);
            }
        }

        private int getXcoord(double x) {
            return (int) (dx + x * dx * scale);
        }

        private int getYcoord(double y) {
            return (int) (dy - y * dy * scale);
        }


        public void display(double vt, long iter, Boundary bounds) {
            this.bounds = bounds;
            this.vt = vt;
            this.nIter = iter;
        }

        public void updateScale(double k) {
            scale *= k;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 38) {        /* KEY UP */
                scale *= 1.1;
            } else if (e.getKeyCode() == 40) {    /* KEY DOWN */
                scale *= 0.9;
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }
    }
}


