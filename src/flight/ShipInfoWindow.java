package Ship;

import javax.swing.*;
import java.awt.*;

public class ShipInfoWindow extends JFrame {

    private Universe universe;
    private Ship currentShip;

    private JLabel nameLabel;
    private JLabel statusLabel;
    private JLabel locationLabel;
    private JComboBox<String> shipSelector;
    private JRadioButton offButton, manualButton, autoButton;

    public ShipInfoWindow(Universe universe, Ship initialShip) {
        this.universe = universe;
        this.currentShip = initialShip;

        setTitle("Ship.Ship Info - " + initialShip.getName());
        setSize(350, 250);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Top dropdown to select another ship
        shipSelector = new JComboBox<>();
        for (Ship ship : universe.getShips()) {
            shipSelector.addItem(ship.getName());
        }
        shipSelector.setSelectedItem(initialShip.getName());
        shipSelector.addActionListener(e -> switchShip((String) shipSelector.getSelectedItem()));
        add(shipSelector, BorderLayout.NORTH);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        nameLabel = new JLabel();
        statusLabel = new JLabel();
        locationLabel = new JLabel();
        infoPanel.add(nameLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(locationLabel);
        add(infoPanel, BorderLayout.CENTER);

        // Task mode controls
        JPanel modePanel = new JPanel(new FlowLayout());
        offButton = new JRadioButton("OFF");
        manualButton = new JRadioButton("MANUAL");
        autoButton = new JRadioButton("AUTO");

        ButtonGroup group = new ButtonGroup();
        group.add(offButton);
        group.add(manualButton);
        group.add(autoButton);

        modePanel.add(new JLabel("Task Mode:"));
        modePanel.add(offButton);
        modePanel.add(manualButton);
        modePanel.add(autoButton);

        // Set initial selection
        updateModeButtons();

        // Action listeners
        offButton.addActionListener(e -> currentShip.taskState = ShipTaskState.OFF);
        manualButton.addActionListener(e -> currentShip.taskState = ShipTaskState.MANUAL);
        autoButton.addActionListener(e -> currentShip.taskState = ShipTaskState.AUTO);

        add(modePanel, BorderLayout.SOUTH);

        // Timer to refresh info every second
        Timer t = new Timer(1000, e -> refreshInfo());
        t.start();

        setVisible(true);
    }

    private void switchShip(String shipName) {
        for (Ship ship : universe.getShips()) {
            if (ship.getName().equals(shipName)) {
                currentShip = ship;
                setTitle("Ship.Ship Info - " + currentShip.getName());
                refreshInfo();
                updateModeButtons();
                break;
            }
        }
    }

    private void updateModeButtons() {
        if (currentShip == null) return;

        switch (currentShip.taskState   ) {
            case OFF -> offButton.setSelected(true);
            case MANUAL -> manualButton.setSelected(true);
            case AUTO -> autoButton.setSelected(true);
        }
    }

    private void refreshInfo() {
        if (currentShip == null) return;

        nameLabel.setText("Name: " + currentShip.getName());
        statusLabel.setText("Status: " + currentShip.getStatus());
        locationLabel.setText("Location: " + currentShip.location.name);

        updateModeButtons();
    }
}
