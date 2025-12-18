package gui;

import flight.*;
import simulation.*;

import javax.swing.*;
import java.awt.*;

public class ShipInfoWindow extends JFrame
{
    private Universe universe;
    private Ship currentLegacyShip;

    private JLabel nameLabel;
    private JLabel statusLabel;
    private JLabel locationLabel;
    private JLabel procedureLabel;
    private JLabel deltavLabel;
    private JComboBox<String> shipSelector;
    private JRadioButton offButton, manualButton, autoButton;
    private JComboBox<String> flightPlanSelector;
    private JButton applyPlanButton;

    public ShipInfoWindow(Universe universe, Ship initialLegacyShip)
    {
        this.universe = universe;
        this.currentLegacyShip = initialLegacyShip;

        setTitle("Ship Info - " + initialLegacyShip.getName());
        setSize(400, 260);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Ship selector
        shipSelector = new JComboBox<>();
        for (Ship legacyShip : universe.getShips())
        {
            shipSelector.addItem(legacyShip.getName());
        }
        shipSelector.setSelectedItem(initialLegacyShip.getName());
        shipSelector.addActionListener(e -> switchShip((String) shipSelector.getSelectedItem()));
        add(shipSelector, BorderLayout.NORTH);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        nameLabel = new JLabel();
        statusLabel = new JLabel();
        locationLabel = new JLabel();
        procedureLabel = new JLabel();
        deltavLabel = new JLabel();
        infoPanel.add(nameLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(procedureLabel);
        infoPanel.add(deltavLabel);
        add(infoPanel, BorderLayout.CENTER);

        // Mode controls
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

        offButton.addActionListener(e -> currentLegacyShip.setTaskState(ShipTaskState.OFF));
        manualButton.addActionListener(e -> currentLegacyShip.setTaskState(ShipTaskState.MANUAL));
        autoButton.addActionListener(e -> currentLegacyShip.setTaskState(ShipTaskState.AUTO));

        add(modePanel, BorderLayout.SOUTH);

        // Flight plan controls
        JPanel planPanel = new JPanel(new FlowLayout());
        planPanel.add(new JLabel("Flight Plan:"));
        flightPlanSelector = new JComboBox<>();
        refreshFlightPlanList();
        planPanel.add(flightPlanSelector);

        applyPlanButton = new JButton("Apply Plan");
        applyPlanButton.addActionListener(e -> applySelectedPlan());
        planPanel.add(applyPlanButton);

        add(planPanel, BorderLayout.NORTH);

        // Refresh info every second
        Timer t = new Timer(1000, e -> refreshInfo());
        t.start();

        setVisible(true);
    }

    private void refreshFlightPlanList()
    {
        flightPlanSelector.removeAllItems();
        flightPlanSelector.addItem("None");
        for (var plan : FlightPlanRepository.allPlans)
        {
            flightPlanSelector.addItem(plan.getName());
        }
    }

    private void applySelectedPlan()
    {
        if (currentLegacyShip == null)
            return;

        String selectedName = (String) flightPlanSelector.getSelectedItem();
        if (selectedName == null)
            return;

        if (selectedName.equals("None"))
        {
            currentLegacyShip.setFlightPlan(null);
            JOptionPane.showMessageDialog(this, "Cleared flight plan for " + currentLegacyShip.getName());
            return;
        }

        FlightPlan plan = FlightPlanRepository.allPlans.stream()
                .filter(p -> p.getName().equals(selectedName))
                .findFirst()
                .orElse(null);

        if (plan != null)
        {
            currentLegacyShip.setFlightPlan(plan);
            JOptionPane.showMessageDialog(this, "Applied plan '" + plan.getName() + "' to ship " + currentLegacyShip.getName());
        }
    }

    private void switchShip(String shipName)
    {
        for (Ship legacyShip : universe.getShips())
        {
            if (legacyShip.getName().equals(shipName))
            {
                currentLegacyShip = legacyShip;
                setTitle("Ship Info - " + currentLegacyShip.getName());
                refreshInfo();
                updateModeButtons();
                updateFlightPlanSelector();
                break;
            }
        }
    }

    private void updateModeButtons()
    {
        if (currentLegacyShip == null)
            return;

        switch (currentLegacyShip.taskState)
        {
            case OFF -> offButton.setSelected(true);
            case MANUAL -> manualButton.setSelected(true);
            case AUTO -> autoButton.setSelected(true);
        }
    }

    private void updateFlightPlanSelector()
    {
        if (currentLegacyShip == null)
            return;

        String currentPlanName = currentLegacyShip.getFlightPlan() != null
                ? currentLegacyShip.getFlightPlan().getName()
                : "None";

        flightPlanSelector.setSelectedItem(currentPlanName);
    }

    private void refreshInfo()
    {
        if (currentLegacyShip == null)
            return;

        nameLabel.setText("Name: " + currentLegacyShip.getName());
        statusLabel.setText("Status: " + currentLegacyShip.getShortStatus());
        locationLabel.setText("Location: " + currentLegacyShip.location.name);
        procedureLabel.setText("Procedure: " + currentLegacyShip.getProcedureStatus());
        deltavLabel.setText("Delta-V: " + currentLegacyShip.getDeltaV());

        updateModeButtons();
    }
}
