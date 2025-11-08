import simulation.Celestial;
import simulation.SolarSystem;
import simulation.Universe;
import util.CONST;
import util.LambertSolver;
import util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
 * Porkchop plot window with origin/destination drop-downs.
 */
public class TransferPlannerGUI extends JFrame {
    private Celestial origin;
    private Celestial destination;
    private final Universe universe;

    private JComboBox<Celestial> originBox;
    private JComboBox<Celestial> destinationBox;

    public TransferPlannerGUI(Universe universe) {
        super("Porkchop Plot");
        this.universe = universe;

        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        PlotPanel plotPanel = new PlotPanel();
        LegendPanel legendPanel = new LegendPanel(plotPanel);
        InfoPanel infoPanel = new InfoPanel(plotPanel);

        add(plotPanel, BorderLayout.CENTER);
        add(legendPanel, BorderLayout.EAST);
        add(infoPanel, BorderLayout.SOUTH);

        // --- top control panel with drop-downs and range fields ---
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT));

        originBox = new JComboBox<>();
        destinationBox = new JComboBox<>();

        // populate originBox with all celestials
        for (SolarSystem system : universe.getSolarSystems())
        {
            addCelestialsToComboBox(system.getRoot(), originBox);
        }

        originBox.addActionListener(e -> {
            updateDestinationBox();
            plotPanel.computeGridAsync();
        });

        destinationBox.addActionListener(e -> plotPanel.computeGridAsync());

        ctrl.add(new JLabel("Origin:"));
        ctrl.add(originBox);
        ctrl.add(new JLabel("Destination:"));
        ctrl.add(destinationBox);

        ctrl.add(new JLabel("Dep span (days):"));
        JTextField depSpanField = new JTextField("730", 6);
        ctrl.add(depSpanField);

        ctrl.add(new JLabel("TOF min (days):"));
        JTextField tofMinField = new JTextField("50", 4);
        ctrl.add(tofMinField);

        ctrl.add(new JLabel("TOF max (days):"));
        JTextField tofMaxField = new JTextField("500", 4);
        ctrl.add(tofMaxField);

        JButton updateButton = new JButton("Update Plot");
        updateButton.addActionListener(e -> {
            origin = (Celestial) originBox.getSelectedItem();
            destination = (Celestial) destinationBox.getSelectedItem();
            if (origin == null || destination == null) return;

            try
            {
                int depDays = Integer.parseInt(depSpanField.getText());
                int tofMin = Integer.parseInt(tofMinField.getText());
                int tofMax = Integer.parseInt(tofMaxField.getText());

                plotPanel.setRanges(depDays, tofMin, tofMax);
                plotPanel.computeGridAsync();
            }
            catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(this, "Invalid numeric input");
            }
        });
        ctrl.add(updateButton);

        add(ctrl, BorderLayout.NORTH);

        // initialize destination box based on first origin
        originBox.setSelectedIndex(0);
        updateDestinationBox();

        plotPanel.computeGridAsync();
        setVisible(true);
    }

    private void addCelestialsToComboBox(Celestial c, JComboBox<Celestial> box) {
        if (c.parent != null) box.addItem(c);  // only add if it has a parent
        for (Celestial child : c.children) addCelestialsToComboBox(child, box);
    }

    private void addCelestialsToComboBoxExcluding(Celestial c, JComboBox<Celestial> box, Celestial exclude) {
        if (c != exclude && c.parent != null) box.addItem(c); // only add if it has a parent
        for (Celestial child : c.children) addCelestialsToComboBoxExcluding(child, box, exclude);
    }
    private void updateDestinationBox()
    {
        origin = (Celestial) originBox.getSelectedItem();
        destinationBox.removeAllItems();
        if (origin == null || origin.parent == null) return;

        // Only show siblings: other children of the same parent
        for (Celestial sibling : origin.parent.children)
        {
            if (sibling != origin) destinationBox.addItem(sibling);
        }

        destination = (Celestial) destinationBox.getSelectedItem();
    }

    // ------------------- PlotPanel, LegendPanel, InfoPanel -------------------
    private class PlotPanel extends JPanel {
        private int nx = 140;
        private int ny = 100;
        private int depSpanDays = 730;
        private int tofMinDays = 50;
        private int tofMaxDays = 500;

        private double[][] dvGrid;
        private double maxDv = 1.0;

        private final DecimalFormat df = new DecimalFormat("0.0");

        private volatile boolean computing = false;

        PlotPanel() {
            setBackground(Color.BLACK);
            dvGrid = new double[nx][ny];
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = e.getX() * nx / getWidth();
                    int j = (getHeight() - e.getY()) * ny / getHeight();
                    i = Math.max(0, Math.min(nx-1, i));
                    j = Math.max(0, Math.min(ny-1, j));

                    if (i >= 0 && i < nx && j >= 0 && j < ny) {
                        double depDays = (double) i / (nx - 1) * depSpanDays;
                        double tofDays = tofMinDays + (double) j / (ny - 1) * (tofMaxDays - tofMinDays);
                        double dv = dvGrid[i][j];
                        JOptionPane.showMessageDialog(TransferPlannerGUI.this,
                                String.format("Dep: %.1f days\nTOF: %.1f days\nΔV: %s m/s",
                                        depDays, tofDays, Double.isNaN(dv) ? "N/A" : df.format(dv)));
                    }
                }
            });
        }

        public void setRanges(int depSpan, int tofMin, int tofMax) {
            this.depSpanDays = depSpan;
            this.tofMinDays = tofMin;
            this.tofMaxDays = tofMax;
        }

        public void computeGridAsync() {
            if (computing) return;
            computing = true;
            new Thread(() -> {
                computeGrid();
                repaint();
                computing = false;
            }).start();
        }

        private void computeGrid()
        {
            if(origin == null || destination == null) return;
            if(!origin.parent.equals(destination.parent)) return;

            double depSpanSec = depSpanDays * 86400.0;
            double tofMinSec = tofMinDays * 86400.0;
            double tofMaxSec = tofMaxDays * 86400.0;

            // central mass: parent or self if parentless
            double mu = CONST.G * origin.parent.mass;

            double localMax = 1.0;
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) dvGrid[i][j] = Double.NaN;
            }

            for (int i = 0; i < nx; i++) {
                double fracDep = (double) i / (nx - 1);
                double depSec = fracDep * depSpanSec;

                Vector2 r1 = origin.getPositionAtTime(depSec);
                Vector2 vPlanet1 = origin.getVelocityAtTime(depSec);

                for (int j = 0; j < ny; j++) {
                    double fracTof = (double) j / (ny - 1);
                    double tof = tofMinSec + fracTof * (tofMaxSec - tofMinSec);

                    Vector2 r2 = destination.getPositionAtTime(depSec + tof);
                    Vector2 vPlanet2 = destination.getVelocityAtTime(depSec + tof);

                    LambertSolver.Result res = LambertSolver.solve(r1, r2, tof, mu);
                    if (!res.success || res.vDepart == null || res.vArrive == null) {
                        dvGrid[i][j] = Double.NaN;
                        continue;
                    }

                    double dvDepart = res.vDepart.subtract(vPlanet1).magnitude();
                    double dvArrive = res.vArrive.subtract(vPlanet2).magnitude();
                    double totalDv = dvDepart + dvArrive;
                    dvGrid[i][j] = totalDv;

                    if (!Double.isNaN(totalDv) && totalDv > localMax) localMax = totalDv;
                }
            }
            this.maxDv = Math.max(1.0, localMax);
        }

        @Override
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            Graphics2D g = (Graphics2D) gg;
            int w = getWidth();
            int h = getHeight();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);

            if (dvGrid == null) return;

            for (int i = 0; i < nx; i++) {
                int x = i * w / nx;
                int nextX = (i + 1) * w / nx;
                int rectWidth = nextX - x;

                for (int j = 0; j < ny; j++) {
                    int y = h - ((j + 1) * h / ny);
                    int nextY = h - (j * h / ny);
                    int rectHeight = nextY - y;

                    double dv = dvGrid[i][j];
                    if (Double.isNaN(dv)) {
                        g.setColor(new Color(10, 10, 10));
                    } else {
                        double norm = Math.min(1.0, dv / maxDv);
                        g.setColor(mapColor(norm));
                    }
                    g.fillRect(x, y, rectWidth + 1, rectHeight + 1);
                }
            }
        }

        private Color mapColor(double t) {
            t = Math.max(0.0, Math.min(1.0, t));
            float h = (float) (0.66 - 0.66 * t);
            return Color.getHSBColor(h, 1.0f, 1.0f);
        }
    }

    private class LegendPanel extends JPanel {
        private final PlotPanel plot;
        private final DecimalFormat df = new DecimalFormat("0.0");
        LegendPanel(PlotPanel plot) { this.plot = plot; setPreferredSize(new Dimension(150,0)); setBackground(Color.GRAY); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int h = getHeight() - 40;
            for (int k=0;k<h;k++){
                double frac=1.0-(double)k/(h-1);
                g.setColor(plot.mapColor(frac));
                g.drawLine(20,20+k,40,20+k);
            }
            g.setColor(Color.WHITE);
            g.drawRect(20,20,20,h);
            g.drawString("0 m/s",50,20+h);
            g.drawString(df.format(plot.maxDv)+" m/s",50,30);
        }
    }

    private class InfoPanel extends JPanel {
        private final PlotPanel plot;
        InfoPanel(PlotPanel plot) { this.plot = plot; setBackground(Color.BLACK); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            g.drawString("Departure (days from epoch): 0 → "+plot.depSpanDays,10,10);
            g.drawString("TOF (days): "+plot.tofMinDays+" → "+plot.tofMaxDays,getWidth()/2-60,10);
        }
    }
}
