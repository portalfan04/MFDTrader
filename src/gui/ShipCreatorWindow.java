package gui;

import flight.Ship;
import simulation.Universe;
import simulation.Celestial;
import simulation.Organisation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ShipCreatorWindow extends JFrame
{
    private Universe universe;

    private JTextField nameField;
    private JComboBox<String> celestialBox;
    private JComboBox<String> organisationBox;
    private JSpinner deltaVSpinner;
    private JButton createButton;
    private JButton cancelButton;

    public ShipCreatorWindow(Universe universe)
    {
        this.universe = universe;

        setTitle("Ship Creator");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameField = new JTextField("New Ship");

        celestialBox = new JComboBox<>();
        for (Celestial c : universe.getAllBodies())
        {
            celestialBox.addItem(c.name);
        }

        organisationBox = new JComboBox<>();
        for (Organisation o : universe.getOrganisations())
        {
            organisationBox.addItem(o.getName());
        }

        deltaVSpinner = new JSpinner(new SpinnerNumberModel(5000.0, 0.0, 1e6, 100.0));

        formPanel.add(new JLabel("Ship Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Organisation:"));
        formPanel.add(organisationBox);
        formPanel.add(new JLabel("Starting Body:"));
        formPanel.add(celestialBox);
        formPanel.add(new JLabel("Delta-V (m/s):"));
        formPanel.add(deltaVSpinner);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createButton = new JButton("Create Ship");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        createButton.addActionListener(this::onCreateShip);
        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void onCreateShip(ActionEvent e)
    {
        String name = nameField.getText().trim();
        if (name.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Ship name cannot be empty.");
            return;
        }

        Celestial body = universe.findCelestialByName((String) celestialBox.getSelectedItem());
        if (body == null)
        {
            JOptionPane.showMessageDialog(this, "Invalid starting body.");
            return;
        }

        Organisation org = universe.findOrganisationByName((String) organisationBox.getSelectedItem());
        if (org == null)
        {
            JOptionPane.showMessageDialog(this, "Invalid organisation.");
            return;
        }

        double deltaV = (double) deltaVSpinner.getValue();

        Ship newShip = new Ship(name, body, org);
        newShip.setName(name);

        org.addShip(newShip);

        JOptionPane.showMessageDialog(this, "Ship '" + name + "' created under " + org.getName() + " around " + body.name + "!");
        dispose();
    }
}
