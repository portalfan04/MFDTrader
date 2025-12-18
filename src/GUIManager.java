import flight.Ship;
import gui.ShipInfoWindow;
import gui.SolarSystemViewer;
import gui.TransferPlannerGUI;
import simulation.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class GUIManager extends JFrame {

    private SolarSystemViewer solarSystemViewer;
    private JTree systemTree;
    private JPanel toolbarPanel;
    private Universe universe;
    private DefaultListModel<String> shipListModel;
    private JList<String> shipList;

    public GUIManager(Universe universe) {
        setTitle("Solar System Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Toolbar
        toolbarPanel = makeToolbar();
        add(toolbarPanel, BorderLayout.NORTH);

        // Sidebar with tabs for Celestials / Ships
        JTabbedPane sideTabs = new JTabbedPane();

        // Simulation.Celestial tree tab
        systemTree = makeTree(universe);
        JScrollPane treeScroll = new JScrollPane(systemTree);
        treeScroll.setPreferredSize(new Dimension(400, 0));
        sideTabs.addTab("Celestials", treeScroll);

        // Ships list tab
        JPanel shipsPanel = makeShipsPanel();
        sideTabs.addTab("Ships", shipsPanel);

        add(sideTabs, BorderLayout.WEST);

        sideTabs.addChangeListener(e -> {
            if (sideTabs.getSelectedComponent() == shipsPanel) {
                refreshShipList();
            }
        });

        // Solar System Viewer
        solarSystemViewer = new SolarSystemViewer(universe);
        add(solarSystemViewer.getPanel(), BorderLayout.CENTER);

        this.universe = universe;
        setVisible(true);

    }
    public JPanel makeToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton playButton = new JButton("Play");
        JButton stepButton = new JButton("Step");
        JComboBox<String> modeBox = new JComboBox<>(new String[]{"Real Time", "Turn Based"});

        // Editable combo box for days/sec
        String[] presetSpeeds = {"0.5", "1", "2", "5", "10"};
        JComboBox<String> speedBox = new JComboBox<>(presetSpeeds);
        speedBox.setEditable(true);
        speedBox.setSelectedItem("1"); // default speed

        JButton plannerGUI = new JButton("Planner");

        JButton flightPlanDesignerBtn = new JButton("Flight Plan Designer");
        flightPlanDesignerBtn.addActionListener(e -> {
            // Grab all ships and all celestials in the universe
            ArrayList<Ship> legacyShips = universe.getShips();
            ArrayList<Celestial> celestials = new ArrayList<>();
            for (SolarSystem ss : universe.getSolarSystems()) {
                celestials.addAll(ss.getAllBodies());
            }
            new gui.FlightPlanDesigner(universe, celestials);
        });

        JButton shipSpawnerBtn = new JButton("Ship Spawner");
        shipSpawnerBtn.addActionListener(e -> {
            new gui.ShipCreatorWindow(universe);
        });

        // Play / Pause
        playButton.addActionListener(e -> {
            Main.paused = !Main.paused;
            playButton.setText(Main.paused ? "Play" : "Pause");
        });

        // Step (for turn-based mode)
        stepButton.addActionListener(e -> {
            universe.advanceTurn();
            render();
        });

        // Planner button
        plannerGUI.addActionListener(e -> openTransferPlanner());

        // Mode selector
        modeBox.addActionListener(e -> {
            if (modeBox.getSelectedIndex() == 0) {
                universe.setMode(Universe.TimeMode.REAL_TIME);
                stepButton.setEnabled(false);
            } else {
                universe.setMode(Universe.TimeMode.TURN_BASED);
                stepButton.setEnabled(true);
            }
        });

        // Speed selector (editable)
        speedBox.addActionListener(e -> {
            String selected = (String) speedBox.getSelectedItem();
            try {
                double speed = Double.parseDouble(selected);
                if (speed <= 0) speed = 1; // fallback for invalid input
                universe.setSpeedMultiplier(speed);
            } catch (NumberFormatException ex) {
                // invalid input: fallback to 1 day/sec
                universe.setSpeedMultiplier(1);
            }
        });

        // Add all controls to toolbar
        toolbar.add(playButton);
        toolbar.add(stepButton);
        toolbar.add(modeBox);
        toolbar.add(speedBox);
        toolbar.add(plannerGUI);
        toolbar.add(flightPlanDesignerBtn);
        toolbar.add(shipSpawnerBtn);

        return toolbar;
    }
    private JTree makeTree(Universe universe)
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Universe");

        for (SolarSystem system : universe.getSolarSystems())
        {
            DefaultMutableTreeNode sysNode = new DefaultMutableTreeNode(system.getRoot().name);
            addChildrenToTree(system.getRoot(), sysNode);
            root.add(sysNode);
        }

        JTree tree = new JTree(root);

        // Double-click listener
        tree.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) return;

                    Rectangle bounds = tree.getRowBounds(tree.getRowForPath(path));
                    if (bounds != null)
                    {
                        int buttonWidth = 20;
                        boolean clickedOnButton = e.getX() < bounds.x + buttonWidth;
                        if (clickedOnButton) return; // ignore double-click on button
                    }

                    Object nodeObj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                    if (nodeObj == null) return;

                    String name = nodeObj.toString();
                    Celestial body = findCelestialByName(universe, name);

                    if (body != null)
                    {
                        new gui.PlanetInfoWindow(body);
                    }
                }
            }
        });

        tree.setCellRenderer(new ExpandButtonTreeRenderer());
        tree.addMouseListener(new ExpandButtonClickListener(tree));

        return tree;
    }
    private Celestial findCelestialByName(Universe universe, String name)
    {
        for (SolarSystem ss : universe.getSolarSystems())
        {
            for (Celestial c : ss.getAllBodies())
            {
                if (c.name.equals(name))
                {
                    return c;
                }
            }
        }

        return null;
    }

    private void addChildrenToTree(Celestial body, DefaultMutableTreeNode node) {
        for (Celestial child : body.children) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.name);
            node.add(childNode);
            addChildrenToTree(child, childNode);
        }
    }
    private JPanel makeShipsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        shipListModel = new DefaultListModel<>();  // <- field
        shipList = new JList<>(shipListModel);
        shipList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        refreshShipList(); // populate initially

        // Scroll + refresh button
        panel.add(new JScrollPane(shipList), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshShipList());
        panel.add(refresh, BorderLayout.SOUTH);


        shipList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = shipList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < universe.getShips().size()) {
                        Ship selectedLegacyShip = universe.getShips().get(index);
                        new ShipInfoWindow(universe, selectedLegacyShip);
                    }
                }
            }
        });


        return panel;
    }

    public void refreshShipList() {  // <- no parameters needed
        if (universe == null) return;
        shipListModel.clear();
        for (Ship legacyShip : universe.getShips()) {
            String status = legacyShip.getShortStatus();
            shipListModel.addElement(legacyShip.getName() + " — " + status);
        }
    }
    public SolarSystemViewer getSolarSystemViewer() {
        return solarSystemViewer;
    }
    private void openTransferPlanner()
    {
        TransferPlannerGUI planner = new TransferPlannerGUI(universe);
    }



    // This is the proper render call
    public void render() {
        solarSystemViewer.getPanel().repaint();
        refreshShipList();
    }
}
class ExpandButtonTreeRenderer extends JPanel implements TreeCellRenderer
{
    private JLabel nameLabel;
    private JButton expandButton;

    public ExpandButtonTreeRenderer()
    {
        setLayout(new BorderLayout(4, 0)); // small gap
        setOpaque(false);

        expandButton = new JButton("+");
        expandButton.setMargin(new Insets(0, 4, 0, 4));
        expandButton.setFocusable(false);
        expandButton.setOpaque(false);

        nameLabel = new JLabel();

        add(expandButton, BorderLayout.WEST);
        add(nameLabel, BorderLayout.CENTER);
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        nameLabel.setText(obj.toString());

        if (leaf)
        {
            expandButton.setVisible(false);
        }
        else
        {
            expandButton.setVisible(true);
            expandButton.setText(expanded ? "−" : "+");
        }

        // highlight background if selected
        if (selected)
        {
            setBackground(new Color(184, 207, 229));
            setOpaque(true);
        }
        else
        {
            setOpaque(false);
        }

        return this;
    }

    public JButton getExpandButton()
    {
        return expandButton;
    }
}

class ExpandButtonClickListener extends java.awt.event.MouseAdapter
{
    private final JTree tree;
    private final int buttonWidth = 20;

    public ExpandButtonClickListener(JTree tree)
    {
        this.tree = tree;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) return;

        Rectangle bounds = tree.getRowBounds(tree.getRowForPath(path));
        if (bounds == null) return;

        // Check if click was inside the expand button area
        boolean clickedOnButton = e.getX() >= bounds.x && e.getX() <= bounds.x + buttonWidth;

        if (clickedOnButton)
        {
            if (tree.isExpanded(path))
            {
                tree.collapsePath(path);
            }
            else
            {
                tree.expandPath(path);
            }
        }
    }
}
