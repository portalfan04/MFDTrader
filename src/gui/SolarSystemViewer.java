import simulation.Celestial;
import simulation.Universe;
import util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SolarSystemViewer {

    private Universe universe;
    private double cameraX = 0, cameraY = 0;
    private double zoom = 1.0;

    private Celestial selected;
    private Celestial followed;

    private SimulationPanel panel;
    private double simulationTime = 0; // seconds

    public SolarSystemViewer(Universe universe) {
        this.universe = universe;
        panel = new SimulationPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void followBody(Celestial body) {
        followed = body;
        if (body != null) {
            cameraX = -body.getPositionAtTime(universe.time).x;
            cameraY = -body.getPositionAtTime(universe.time).y;
        }
    }

    public void setSimulationTime(double time) {
        this.simulationTime = time;
        panel.repaint();
    }

    private class SimulationPanel extends JPanel {
        private static final double BASE_SCALE = 1.0 / 1.0e12; // tweakable
        private Point lastMousePoint;

        public SimulationPanel() {
            setBackground(Color.BLACK);
            setFocusable(true);

            // Zooming with mouse wheel
            addMouseWheelListener(e -> {
                int rot = e.getWheelRotation();
                zoom *= Math.pow(1.25, -rot);
                repaint();
            });

            // Mouse click: single = select, double = follow
            // Mouse click: single = select, double = follow/unfollow
            addMouseListener(new MouseAdapter() {
                private Timer clickTimer;
                private final int DOUBLE_CLICK_DELAY = 200; // milliseconds

                @Override
                public void mousePressed(MouseEvent e) {
                    lastMousePoint = e.getPoint();
                    Celestial clicked = findBodyAt(e.getX(), e.getY());

                    if (clickTimer != null && clickTimer.isRunning()) {
                        // Second click within delay = double click
                        clickTimer.stop();

                        if (clicked != null)
                        {
                            // Double-clicked a body -> follow it
                            followed = clicked;
                            selected = clicked;
                            cameraX = -clicked.getPositionAtTime(universe.time).x;
                            cameraY = -clicked.getPositionAtTime(universe.time).y;
                        }
                        else
                        {
                            followed = null;
                        }
                        repaint();
                    } else {
                        // First click: wait a bit for possible second click
                        clickTimer = new Timer(DOUBLE_CLICK_DELAY, evt -> {
                            // Single click = select
                            selected = clicked;
                            repaint();
                        });
                        clickTimer.setRepeats(false);
                        clickTimer.start();
                    }
                }
            });


            // Drag to move camera
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (lastMousePoint != null && followed == null) {
                        int dx = e.getX() - lastMousePoint.x;
                        int dy = e.getY() - lastMousePoint.y;
                        cameraX -= dx / (BASE_SCALE * zoom);
                        cameraY += dy / (BASE_SCALE * zoom);
                        lastMousePoint = e.getPoint();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Keep camera centered on followed body
            if (followed != null) {
                cameraX = -followed.getPositionAtTime(universe.time).x;
                cameraY = -followed.getPositionAtTime(universe.time).y;
            }

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;


            // Draw orbits
            for (Celestial body : universe.getAllBodies()) {
                if (body.parent != null) drawOrbit(g2d, body, cx, cy);
            }

            // Draw bodies
            for (Celestial body : universe.getAllBodies()) {
                drawBody(g2d, body, cx, cy);
            }

            // Draw body labels after all bodies, avoiding overlaps
            drawLabels(g2d, cx, cy);
            // Draw periapsis/apoapsis for selected/followed
            if (selected != null && selected.parent != null) drawPeriApo(g2d, selected, cx, cy, Color.MAGENTA);
            if (followed != null && followed.parent != null) drawPeriApo(g2d, followed, cx, cy, Color.GREEN);

            // Draw simulation time in years
            g.setColor(Color.WHITE);
            simulationTime = universe.time;
            g.drawString(String.format("Time: %.2f years", simulationTime / (365.25*24*3600)), 10, 20);
        }

        private void drawOrbit(Graphics2D g, Celestial body, int cx, int cy) {
            if (body.orbitalElements == null) return;
            g.setColor(Color.DARK_GRAY);
            if (selected == body)
            {
                g.setColor(Color.MAGENTA);
            }
            if (followed == body)
            {
                g.setColor(Color.GREEN);
            }

            Vector2 parentPos = body.parent.getPositionAtTime(universe.time);
            double cosW = Math.cos(body.orbitalElements.argPeriapsis);
            double sinW = Math.sin(body.orbitalElements.argPeriapsis);

            int prevX = 0, prevY = 0;
            for (int i = 0; i <= 360; i++) {
                double θ = Math.toRadians(i);
                double r = body.orbitalElements.a * (1 - Math.pow(body.orbitalElements.e, 2)) /
                        (1 + body.orbitalElements.e * Math.cos(θ));

                double xOrb = r * Math.cos(θ);
                double yOrb = r * Math.sin(θ);

                double xRot = xOrb * cosW - yOrb * sinW;
                double yRot = xOrb * sinW + yOrb * cosW;

                double worldX = parentPos.x + xRot;
                double worldY = parentPos.y + yRot;

                int sx = cx + (int)((worldX + cameraX) * BASE_SCALE * zoom);
                int sy = cy - (int)((worldY + cameraY) * BASE_SCALE * zoom);

                if (i > 0) g.drawLine(prevX, prevY, sx, sy);
                prevX = sx; prevY = sy;
            }
        }

        private void drawBody(Graphics2D g, Celestial body, int cx, int cy) {
            // Screen position
            double sx = cx + (body.getPositionAtTime(universe.time).x + cameraX) * BASE_SCALE * zoom;
            double sy = cy - (body.getPositionAtTime(universe.time).y + cameraY) * BASE_SCALE * zoom;

            // Real radius in km
            double radiusKm = body.radius / 1e6;

            // Base visual scale factor for realistic size (tweakable)
            double bodyScale = 1e-6; // pixels per km

            // Minimum visible dot size
            int minDot = 3;

            // Calculate hybrid size
            double realSize = radiusKm * bodyScale * zoom;          // true size at current zoom
            int size = (int) Math.max(minDot, realSize);           // enforce min dot
            if (zoom < 0.05) size = minDot;                        // force dot mode if zoomed far out

            // Draw the body
            g.setColor(body.color);
            g.fillOval((int)(sx - size / 2), (int)(sy - size / 2), size, size);

            // Outline for selected/followed
            if (body == followed) {
                g.setColor(Color.GREEN);
                g.drawOval((int)(sx - size), (int)(sy - size), size * 2, size * 2);
            } else if (body == selected) {
                g.setColor(Color.MAGENTA);
                g.drawOval((int)(sx - size), (int)(sy - size), size * 2, size * 2);
            }
        }

        private void drawLabels(Graphics2D g, int cx, int cy) {
            // Sort so bigger/more important bodies go first
            List<Celestial> bodies = universe.getAllBodies().stream()
                    .sorted((a, b) -> {
                        // Stars first, then planets, then moons
                        int aLevel = (a.parent == null) ? 0 : (a.parent.parent == null ? 1 : 2);
                        int bLevel = (b.parent == null) ? 0 : (b.parent.parent == null ? 1 : 2);
                        if (aLevel != bLevel) return Integer.compare(aLevel, bLevel);
                        return Double.compare(b.radius, a.radius); // bigger first
                    })
                    .toList();

            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(Color.WHITE);

            java.util.List<Rectangle> usedLabels = new java.util.ArrayList<>();

            for (Celestial body : bodies) {
                // Skip if too small or zoomed out far, unless followed/selected
                boolean isStar = body.parent == null; // stars have no parent
                if (!isStar && zoom < 0.05 && body != followed && body != selected) continue;


                double sx = cx + (body.getPositionAtTime(universe.time).x + cameraX) * SimulationPanel.BASE_SCALE * zoom;
                double sy = cy - (body.getPositionAtTime(universe.time).y + cameraY) * SimulationPanel.BASE_SCALE * zoom;

                String label = body.name;
                FontMetrics fm = g.getFontMetrics();
                int labelW = fm.stringWidth(label);
                int labelH = fm.getHeight();

                int nameX = (int) sx + 8;
                int nameY = (int) sy - 8;

                Rectangle rect = new Rectangle(nameX, nameY - labelH, labelW, labelH);

                // Check overlap
                boolean overlaps = false;
                for (Rectangle used : usedLabels) {
                    if (used.intersects(rect)) {
                        overlaps = true;
                        break;
                    }
                }

                // Skip label if overlapping and not important
                if (overlaps && body != followed && body != selected) continue;

                // Draw outline and text
                g.setColor(Color.BLACK);
                g.drawString(label, nameX + 1, nameY + 1);
                g.setColor(Color.WHITE);
                g.drawString(label, nameX, nameY);

                usedLabels.add(rect);
            }
        }

        private void drawPeriApo(Graphics2D g, Celestial body, int cx, int cy, Color color) {
            g.setColor(color);
            double cosW = Math.cos(body.orbitalElements.argPeriapsis);
            double sinW = Math.sin(body.orbitalElements.argPeriapsis);
            Vector2 parentPos = body.parent.getPositionAtTime(universe.time);

            double rp = body.orbitalElements.a * (1 - body.orbitalElements.e);
            double ra = body.orbitalElements.a * (1 + body.orbitalElements.e);

            // Rotate helper
            java.util.function.BiFunction<Double, Double, Vector2> rotate = (x, y) -> {
                double xr = x * cosW - y * sinW;
                double yr = x * sinW + y * cosW;
                return new Vector2(xr, yr);
            };

            Vector2 periVec = rotate.apply(rp, 0.0).add(parentPos);
            Vector2 apoVec = rotate.apply(-ra, 0.0).add(parentPos);

            int screenXp = cx + (int)((periVec.x + cameraX) * BASE_SCALE * zoom);
            int screenYp = cy - (int)((periVec.y + cameraY) * BASE_SCALE * zoom);
            int screenXa = cx + (int)((apoVec.x + cameraX) * BASE_SCALE * zoom);
            int screenYa = cy - (int)((apoVec.y + cameraY) * BASE_SCALE * zoom);

            int markerSize = 6;
            g.fillOval(screenXp - markerSize/2, screenYp - markerSize/2, markerSize, markerSize);
            g.fillOval(screenXa - markerSize/2, screenYa - markerSize/2, markerSize, markerSize);

            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.drawString("Periapsis", screenXp + 5, screenYp - 5);
            g.drawString("Apoapsis", screenXa + 5, screenYa - 5);
        }

        private Celestial findBodyAt(int sx, int sy) {
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            for (Celestial b : universe.getAllBodies()) {
                double bx = cx + (b.getPositionAtTime(universe.time).x + cameraX) * BASE_SCALE * zoom;
                double by = cy - (b.getPositionAtTime(universe.time).y + cameraY) * BASE_SCALE * zoom;
                double dist = Math.hypot(sx - bx, sy - by);
                int size = (int)Math.max(3, 3 + Math.log10(b.radius / 1e6));
                if (dist <= size + 5) return b;
            }
            return null;
        }
    }
}
