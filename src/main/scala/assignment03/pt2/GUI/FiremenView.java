package assignment03.pt2.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Simulation view
 *
 * @author aricci
 *
 */
public class FiremenView {

    private final VisualiserFrame frame;

    /**
     * Creates a view of the specified size (in pixels)
     *
     * @param w
     * @param h
     */
    public FiremenView(int w, int h, String zone, int maxSensor) {
        frame = new VisualiserFrame(w, h, zone, maxSensor);
    }

    public void display(int n) {
            frame.display(n);
    }

    public void setText(String text) {
        this.frame.setText("STATION: "+text);
    }
    
    public void setZoneLabel(String s){
        this.frame.setZoneLabel("ZONE: "+s);
    }

    public void setDisableButton(ActionListener al) {
        frame.setDisableButton(al);
    }

    public static class ZonePanel extends JPanel {
        private static JButton button;
        private final JLabel station;
        private final JLabel zone;
        private final VisualiserPanel canvasPanel;


        public ZonePanel(String s, int maxSensor) {
            JPanel componentPanel = new JPanel();
            componentPanel.setLayout(new BoxLayout(componentPanel, BoxLayout.Y_AXIS));
            canvasPanel = new VisualiserPanel(getWidth(), getHeight() / 2, maxSensor);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            button = new JButton("Disable Alarm");
            station = new JLabel("STATION: //");
            zone = new JLabel("ZONE: //");

            componentPanel.add(new JLabel(s));
            componentPanel.add(station);
            componentPanel.add(zone);
            componentPanel.add(button);

            this.setBackground(Color.pink);
            componentPanel.setBackground(Color.pink);

            setBorder(BorderFactory.createLineBorder(Color.black));
            add(componentPanel);
            add(canvasPanel);
            canvasPanel.setBackground(Color.pink);

        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        public void display(int n) {
            canvasPanel.display(n);
        }

        public void addActionListener(ActionListener al) {
            button.addActionListener(al);
        }

        public void setLabelText(String s) {
            station.setText(s);
        }
        
        public void setZoneLabel(String s){
            zone.setText(s);
        }
    }

    public static class VisualiserFrame extends JFrame {

        private static ZonePanel panel;

        public VisualiserFrame(int w, int h, String zone, int maxSensor) {

            setSize(w, h);
            setResizable(false);
            
            panel = new ZonePanel(zone, maxSensor);
            getContentPane().add(panel);

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
            panel.setLabelText(s);
        }
        
        public static void setZoneLabel(String s){
            panel.setZoneLabel(s);
        }
        
        public static void setDisableButton(ActionListener al) {
            panel.addActionListener(al);
        }

        public void display(int n) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    panel.display(n);
                    repaint();
                });
            } catch (Exception ex) {
            }
        }

    }

    public static class VisualiserPanel extends JPanel {

        private int n ;
        private List<Integer> posValues = new LinkedList<>();

        public VisualiserPanel(int w, int h, int maxSensor) {
            setSize(w, h);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            requestFocusInWindow();

            Random rand = new Random();
            for (int i = 0; i < maxSensor; i++){
                posValues.add(rand.nextInt(i*i + 50));
            }
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.clearRect(0, 0, this.getWidth(), this.getHeight());

            for (int i = 0; i < n; i++) {
                g2.setColor(Color.magenta);
                g2.fillOval(25 + posValues.get(i) * i, 25 + 25 * i, 20, 20);
            }

        }

        public void display(int n) {
            this.n = n;
        }

    }
}


