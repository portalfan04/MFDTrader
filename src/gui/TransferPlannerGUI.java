package gui;

import simulation.Celestial;
import simulation.SolarSystem;
import simulation.Universe;
import util.CONST;
import util.ConversionHelper;
import util.LambertSolver;
import util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Porkchop plot window with origin/destination drop-downs.
 */
public class TransferPlannerGUI extends JFrame {
    private Celestial origin;
    private Celestial destination;
    private final Universe universe;

    private JComboBox<Celestial> originBox;
    private JComboBox<Celestial> destinationBox;

    // controls that we need access to from multiple places
    private JTextField depSpanField;
    private JTextField tofMinField;
    private JTextField tofMaxField;
    private JCheckBox autoTofCheck;

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
        for (SolarSystem system : universe.getSolarSystems()) {
            addCelestialsToComboBox(system.getRoot(), originBox);
        }

        originBox.addActionListener(e -> {
            updateDestinationBox();
            if (autoTofCheck.isSelected()) computeAutoTof();
            plotPanel.computeGridAsync();
        });

        destinationBox.addActionListener(e -> {
            if (autoTofCheck.isSelected()) computeAutoTof();
            plotPanel.computeGridAsync();
        });

        ctrl.add(new JLabel("Origin:"));
        ctrl.add(originBox);
        ctrl.add(new JLabel("Destination:"));
        ctrl.add(destinationBox);

        ctrl.add(new JLabel("Dep span (days):"));
        depSpanField = new JTextField("730", 6);
        ctrl.add(depSpanField);

        ctrl.add(new JLabel("TOF min (days):"));
        tofMinField = new JTextField("50", 5);
        ctrl.add(tofMinField);

        ctrl.add(new JLabel("TOF max (days):"));
        tofMaxField = new JTextField("500", 5);
        ctrl.add(tofMaxField);

        autoTofCheck = new JCheckBox("Auto TOF range");
        autoTofCheck.setToolTipText("Automatically pick TOF min/max based on orbital periods (disables manual TOF fields). \n Can be glitchy with moon to moon transfers!");
        autoTofCheck.addActionListener(e -> {
            boolean auto = autoTofCheck.isSelected();
            tofMinField.setEnabled(!auto);
            tofMaxField.setEnabled(!auto);
            if (auto) {
                computeAutoTof();
                plotPanel.computeGridAsync();
            }
        });
        ctrl.add(autoTofCheck);

        JButton updateButton = new JButton("Update Plot");
        updateButton.addActionListener(e -> {
            origin = (Celestial) originBox.getSelectedItem();
            destination = (Celestial) destinationBox.getSelectedItem();
            if (origin == null || destination == null) return;

            try {
                double depDays = Double.parseDouble(depSpanField.getText());
                double tofMin = Double.parseDouble(tofMinField.getText());
                double tofMax = Double.parseDouble(tofMaxField.getText());

                plotPanel.setRanges(depDays, tofMin, tofMax);
                plotPanel.computeGridAsync();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid numeric input");
            }
        });
        ctrl.add(updateButton);

        add(ctrl, BorderLayout.NORTH);

        // initialize destination box based on first origin
        if (originBox.getItemCount() > 0) originBox.setSelectedIndex(0);
        updateDestinationBox();

        // If auto is enabled by default, compute initial TOF
        if (autoTofCheck.isSelected()) computeAutoTof();

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

    private void updateDestinationBox() {
        origin = (Celestial) originBox.getSelectedItem();
        destinationBox.removeAllItems();
        if (origin == null || origin.parent == null) return;

        // Only show siblings: other children of the same parent
        for (Celestial sibling : origin.parent.children) {
            if (sibling != origin) destinationBox.addItem(sibling);
        }

        destination = (Celestial) destinationBox.getSelectedItem();
    }

    /**
     * Compute automatic TOF min/max (in days) and set the text fields.
     * Picks multipliers adaptively: moons (short period) get smaller TOF ranges.
     */
    private void computeAutoTof()
    {
        origin = (Celestial) originBox.getSelectedItem();
        destination = (Celestial) destinationBox.getSelectedItem();
        if (origin == null || destination == null) return;
        if (origin.parent == null || destination.parent == null) return;
        if (!origin.parent.equals(destination.parent)) return; // only siblings

        // --- Step 1: Compute mean orbital period ---
        double p1 = (origin.orbitalElements != null) ? origin.orbitalElements.getPeriod() : Double.POSITIVE_INFINITY;
        double p2 = (destination.orbitalElements != null) ? destination.orbitalElements.getPeriod() : Double.POSITIVE_INFINITY;
        double Tmean = (p1 == Double.POSITIVE_INFINITY || p2 == Double.POSITIVE_INFINITY) ? Math.max(p1, p2) : 0.5 * (p1 + p2);

        if (Tmean <= 0 || Double.isInfinite(Tmean))
        {
            tofMinField.setText("1.0");
            tofMaxField.setText("500.0");
            return;
        }

        // --- Step 2: Adaptive coarse grid based on orbital period ---
        int samplesPerOrbit = 40; // default for slow bodies
        if (Tmean < 3600) samplesPerOrbit = 120;       // <1 hr orbit
        else if (Tmean < 86400) samplesPerOrbit = 80;  // <1 day orbit

        int coarseNx = Math.min(200, Math.max(20, samplesPerOrbit));
        int coarseNy = Math.min(300, Math.max(20, samplesPerOrbit));

        double depStep = Tmean / samplesPerOrbit; // in seconds
        double tofStep = depStep;                 // same fraction for TOF

        double depSpanSec = 2.0 * Tmean;          // departure span ~2 orbital periods
        double tofMinSecGuess = Math.max(1.0, 0.01 * Tmean);
        double tofMaxSecGuess = Math.max(50.0, 0.6 * Tmean);

        double[][] dvGrid = new double[coarseNx][coarseNy];
        double maxDv = 1.0;
        double minDv = Double.POSITIVE_INFINITY;
        double mu = CONST.G * origin.parent.mass;

        // --- Step 3: First-pass Lambert evaluation ---
        for (int i = 0; i < coarseNx; i++)
        {
            double depSec = i * depStep;

            Vector2 parentPosDep = origin.parent.getPositionAtTime(depSec);
            Vector2 parentVelDep = origin.parent.getVelocityAtTime(depSec);
            Vector2 r1 = origin.getPositionAtTime(depSec).subtract(parentPosDep);
            Vector2 v1 = origin.getVelocityAtTime(depSec).subtract(parentVelDep);

            for (int j = 0; j < coarseNy; j++)
            {
                double tofSec = tofMinSecGuess + j * tofStep;
                double arrSec = depSec + tofSec;

                Vector2 parentPosArr = origin.parent.getPositionAtTime(arrSec);
                Vector2 parentVelArr = origin.parent.getVelocityAtTime(arrSec);
                Vector2 r2 = destination.getPositionAtTime(arrSec).subtract(parentPosArr);
                Vector2 v2 = destination.getVelocityAtTime(arrSec).subtract(parentVelArr);

                if (r1.magnitude() < 1e-6 || r2.magnitude() < 1e-6)
                {
                    dvGrid[i][j] = Double.NaN;
                    continue;
                }

                LambertSolver.Result res = LambertSolver.solve(r1, r2, tofSec, mu);
                if (!res.success || res.vDepart == null || res.vArrive == null)
                {
                    dvGrid[i][j] = Double.NaN;
                    continue;
                }

                double dv = res.vDepart.subtract(v1).magnitude() + res.vArrive.subtract(v2).magnitude();
                dvGrid[i][j] = dv;
                if (!Double.isNaN(dv))
                {
                    maxDv = Math.max(maxDv, dv);
                    minDv = Math.min(minDv, dv);
                }
            }
        }

        // --- Step 4: Filter TOF range based on ΔV ---
        double dvThreshold = minDv * 1.5;
        double tofMinFinal = Double.POSITIVE_INFINITY;
        double tofMaxFinal = 0.0;

        for (int j = 0; j < coarseNy; j++)
        {
            boolean validRow = false;
            for (int i = 0; i < coarseNx; i++)
            {
                double dv = dvGrid[i][j];
                if (!Double.isNaN(dv) && dv <= dvThreshold)
                {
                    validRow = true;
                    break;
                }
            }

            if (validRow)
            {
                double tofCandidate = tofMinSecGuess + j * tofStep;
                tofMinFinal = Math.min(tofMinFinal, tofCandidate);
                tofMaxFinal = Math.max(tofMaxFinal, tofCandidate);
            }
        }

        // Fallbacks
        if (Double.isInfinite(tofMinFinal)) tofMinFinal = tofMinSecGuess;
        if (tofMaxFinal <= tofMinFinal) tofMaxFinal = tofMinFinal * 5.0;

        // --- Step 5: Clamp and convert to days ---
        tofMinFinal = Math.max(tofMinFinal, 1e-6);
        tofMaxFinal = Math.max(tofMaxFinal, tofMinFinal + 0.1);

        tofMinField.setText(String.valueOf(ConversionHelper.roundToSigFigs(ConversionHelper.secondToDay(tofMinFinal), 6)));
        tofMaxField.setText(String.valueOf(ConversionHelper.roundToSigFigs(ConversionHelper.secondToDay(tofMaxFinal), 6)));
    }
    // ------------------- PlotPanel, LegendPanel, InfoPanel -------------------
    private class PlotPanel extends JPanel {
        // start with defaults; these will be adjusted dynamically by computeGrid()
        private int nx = 140;
        private int ny = 100;
        private double depSpanDays = 730;
        private double tofMinDays = 50;
        private double tofMaxDays = 500;

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
                    i = Math.max(0, Math.min(nx - 1, i));
                    j = Math.max(0, Math.min(ny - 1, j));

                    if (i >= 0 && i < nx && j >= 0 && j < ny) {
                        double depDays = (double) i / (nx - 1) * depSpanDays;
                        double tofDays = tofMinDays + (double) j / (ny - 1) * (tofMaxDays - tofMinDays);
                        double dv = dvGrid[i][j];
                        JOptionPane.showMessageDialog(TransferPlannerGUI.this,
                                String.format("Dep: %.6f days\nTOF: %.6f days\nΔV: %s m/s",
                                        depDays, tofDays, Double.isNaN(dv) ? "N/A" : df.format(dv)));
                    }
                }
            });
        }

        public void setRanges(double depSpan, double tofMin, double tofMax) {
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

        /**
         * Adaptive sampling:
         * - choose time steps based on orbital periods (if available)
         * - adjust nx/ny accordingly and reallocate dvGrid
         */
        private void computeGrid() {
            if (origin == null || destination == null) return;
            if (!origin.parent.equals(destination.parent)) return;

            // read ranges from UI (they are doubles in days)
            try {
                depSpanDays = Double.parseDouble(depSpanField.getText());
                tofMinDays = Double.parseDouble(tofMinField.getText());
                tofMaxDays = Double.parseDouble(tofMaxField.getText());
            } catch (NumberFormatException nfe) {
                // fallback to current values
            }

            double depSpanSec = ConversionHelper.dayToSecond(depSpanDays);
            double tofMinSec = ConversionHelper.dayToSecond(tofMinDays);
            double tofMaxSec = ConversionHelper.dayToSecond(tofMaxDays);

            // central mass: the shared parent (planet)
            double mu = CONST.G * origin.parent.mass;

            // Estimate a "characteristic period" for sampling from orbital elements, if available
            double p1 = (origin.orbitalElements != null) ? origin.orbitalElements.getPeriod() : Double.POSITIVE_INFINITY;
            double p2 = (destination.orbitalElements != null) ? destination.orbitalElements.getPeriod() : Double.POSITIVE_INFINITY;
            double minPeriod = Math.min(p1, p2);
            if (Double.isInfinite(minPeriod) || minPeriod <= 0) minPeriod = 86400.0; // default 1 day

            // Decide desired time step: sample at least N samples per orbit (adaptive)
            int samplesPerOrbit = 40; // aim for this many samples per orbit
            // For very short orbits, sample more finely
            if (minPeriod < 3600) samplesPerOrbit = 120; // <1hr orbit -> many samples
            else if (minPeriod < 86400) samplesPerOrbit = 80; // <1 day -> more samples

            double depStep = Math.max(0.001, minPeriod / (double) samplesPerOrbit); // seconds
            double tofStep = Math.max(0.001, minPeriod / (double) samplesPerOrbit);

            // derive nx, ny from step sizes but clamp to reasonable sizes
            int newNx = (int) Math.max(40, Math.min(800, Math.ceil(depSpanSec / depStep)));
            int newNy = (int) Math.max(20, Math.min(600, Math.ceil((tofMaxSec - tofMinSec) / tofStep)));

            // Avoid zero-sized arrays
            if (newNx <= 0) newNx = 40;
            if (newNy <= 0) newNy = 20;

            // if sizes changed, reallocate dvGrid
            if (newNx != nx || newNy != ny || dvGrid == null) {
                nx = newNx;
                ny = newNy;
                dvGrid = new double[nx][ny];
            }

            double localMax = 1.0;
            for (int i = 0; i < nx; i++) Arrays.fill(dvGrid[i], Double.NaN);

            // Loop over adaptive grid
            for (int i = 0; i < nx; i++) {
                double fracDep = (double) i / (nx - 1);
                double depSec = fracDep * depSpanSec;

                // parent (central) state at departure time
                Vector2 parentPosAtDep = origin.parent.getPositionAtTime(depSec);
                Vector2 parentVelAtDep = origin.parent.getVelocityAtTime(depSec);

                // origin position/velocity relative to parent (parent-centered frame)
                Vector2 r1_rel = origin.getPositionAtTime(depSec).subtract(parentPosAtDep);
                Vector2 vPlanet1_rel = origin.getVelocityAtTime(depSec).subtract(parentVelAtDep);

                for (int j = 0; j < ny; j++) {
                    double fracTof = (double) j / (ny - 1);
                    double tof = tofMinSec + fracTof * (tofMaxSec - tofMinSec);

                    double arrivalTime = depSec + tof;
                    Vector2 parentPosAtArr = origin.parent.getPositionAtTime(arrivalTime);
                    Vector2 parentVelAtArr = origin.parent.getVelocityAtTime(arrivalTime);

                    // destination position/velocity relative to same parent at arrival time
                    Vector2 r2_rel = destination.getPositionAtTime(arrivalTime).subtract(parentPosAtArr);
                    Vector2 vPlanet2_rel = destination.getVelocityAtTime(arrivalTime).subtract(parentVelAtArr);

                    // guard: avoid degenerate zero vectors
                    if (r1_rel.magnitude() < 1e-6 || r2_rel.magnitude() < 1e-6) {
                        dvGrid[i][j] = Double.NaN;
                        continue;
                    }

                    // Solve Lambert in parent-centered frame
                    LambertSolver.Result res = LambertSolver.solve(r1_rel, r2_rel, tof, mu);
                    if (!res.success || res.vDepart == null || res.vArrive == null) {
                        dvGrid[i][j] = Double.NaN;
                        continue;
                    }

                    // ΔV calculations use the difference between transfer velocity and body inertial velocity
                    double dvDepart = res.vDepart.subtract(vPlanet1_rel).magnitude();
                    double dvArrive = res.vArrive.subtract(vPlanet2_rel).magnitude();
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
