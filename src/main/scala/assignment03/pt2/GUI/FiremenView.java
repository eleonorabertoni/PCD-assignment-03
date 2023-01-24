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
    //private Integer n = 3;

    /**
     * Creates a view of the specified size (in pixels)
     *
     * @param w
     * @param h
     */
    public FiremenView(int w, int h) {
        frame = new VisualiserFrame(w, h);
    }

    public void display(int n) {
        if (bounds == null) {
            throw new IllegalStateException("Bounds are not set");
        } else {
            frame.display(n);
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
        private JPanel componentPanel;
        private VisualiserPanel canvasPanel;
        private JLabel name;
        private JLabel l;

        public ZonePanel(String s) {
            componentPanel = new JPanel();
            componentPanel.setLayout(new BoxLayout(componentPanel, BoxLayout.Y_AXIS));
            canvasPanel = new VisualiserPanel(getWidth(), getHeight() / 2);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            b = new JButton("Disable Alarm");
            name = new JLabel(s);
            l = new JLabel("BOH");
            l.setOpaque(true);
            l.setBackground(Color.red);
            name.setOpaque(true);
            name.setBackground(Color.yellow);
            componentPanel.add(name);
            componentPanel.add(b);
            componentPanel.add(l);


            setBorder(BorderFactory.createLineBorder(Color.black));

            add(componentPanel);
            add(canvasPanel);

        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        public void display(int n) {
            canvasPanel.display(n);
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

            for (int i = 0; i < 3; i++) {
                var temp = new ZonePanel("Zone " + i);
                getContentPane().add(temp);
                panels.add(temp);
            }

            getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));

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

        public static void setText(String s) {
            panels.get(0).setLabelText(s);
        }

        public static void setFocusOnSimulation() {
            // panel.requestFocusInWindow();
        }

        public static void setDisableButton(ActionListener al) {
            panels.get(0).addActionListener(al);
        }

        public void display(int n) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    panels.get(0).display(n);
                    repaint();
                });
            } catch (Exception ex) {
            }
        }

        ;

        public void updateScale(double k) {
            //panel.updateScale(k);
        }


    }

    public static class VisualiserPanel extends JPanel {

        private int n ;

        public VisualiserPanel(int w, int h) {
            setSize(w, h);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            requestFocusInWindow();
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.clearRect(0, 0, this.getWidth(), this.getHeight());

            for (int i = 0; i < n; i++) {
                g2.drawOval(25, 25 + 25 * i, 20, 20);
            }

        }

        public void display(int n) {
            this.n = n;
        }

    }
}


