package assignment03.pt2.GUI;

import assignment03.pt1.main.Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.Border;

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
            throw new IllegalStateException("Bounds are not set");
        }else {
            frame.display(vt, iter, bounds);
        }

    }

    public void setText(String text) {
        this.frame.setText(text);
    }

    public void setBounds(Boundary bounds) {
        this.bounds = bounds;
    }

    public void setDisableButton(ActionListener al) {
        frame.setDisableButton(al);
    }

    public static class ZonePanel extends JPanel {
        private static JButton b;
        private JLabel name;
        private JLabel l;

        public ZonePanel(String s){
            b = new JButton("Disable Alarm");
            name = new JLabel(s);
            l = new JLabel("");
            setBorder(BorderFactory.createLineBorder(Color.black));
            add(name);
            add(b);
            add(l);
        }

        public void addActionListener(ActionListener al) {
            b.addActionListener(al);
        }
        public void setLabelText(String s) {
            l.setText(s);
        }
    }

    public static class VisualiserFrame extends JFrame {

        private static java.util.List<ZonePanel> panels = new java.util.LinkedList<ZonePanel>();

        public VisualiserFrame(int w, int h) {

            setSize(w, h);
            setResizable(false);

            for (int i = 2; i >= 0; i--) {
                panels.add(new ZonePanel("Zone " + i));
            }

            getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));

            for (int i = 2; i >= 0; i--) {
                getContentPane().add(panels.get(i), BoxLayout.X_AXIS);
            }

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

        public static void setText(String s){
            panels.get(0).setLabelText(s);
        }

        public static void setFocusOnSimulation() {
           // panel.requestFocusInWindow();
        }

        public static void setDisableButton(ActionListener al) {
            panels.get(0).addActionListener(al);
        }

        public void display(double vt, long iter, Boundary bounds) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    //panel.display(vt, iter, bounds);
                    repaint();
                });
            } catch (Exception ex) {}
        };

        public void updateScale(double k) {
            //panel.updateScale(k);
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
                //g2.drawString(""+text.getText(), 5, 50);
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


