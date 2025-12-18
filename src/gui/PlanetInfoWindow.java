package gui;

import simulation.Celestial;
import util.ConversionHelper;

import javax.swing.*;
import java.awt.*;

public class PlanetInfoWindow extends JFrame
{
    private Celestial body;

    public PlanetInfoWindow(Celestial body)
    {
        this.body = body;

        setTitle("Celestial Info – " + body.name);
        setSize(700, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Overview", makeOverview());
        tabs.addTab("Environment", makeEnvironment());
        tabs.addTab("Economy", makeEconomy());
        tabs.addTab("Population", makePopulation());
        tabs.addTab("Sites", makeSites());
        tabs.addTab("Orbit", makeOrbit());

        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel makeOverview()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Radius: " + body.radius));
        panel.add(new JLabel("Mass: " + body.mass));
        panel.add(new JLabel("Object type: " + body.type.name()));
        return panel;
    }

    private JPanel makeEnvironment()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Environment tab placeholder"));
        return panel;
    }

    private JPanel makeEconomy()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Economy tab placeholder"));
        return panel;
    }

    private JPanel makePopulation()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Population tab placeholder"));
        return panel;
    }

    private JPanel makeSites()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Colonies / sites placeholder"));
        return panel;
    }

    private JPanel makeOrbit()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (body.parent == null)
        {
            panel.add(new JLabel("Orbital elements are not available for parent object"));
        }
        else
        {
            double aAU = ConversionHelper.meterToAU(body.orbitalElements.a);
            double apoAU = ConversionHelper.meterToAU(body.orbitalElements.getApoapsis());
            double periAU = ConversionHelper.meterToAU(body.orbitalElements.getPeriapsis());

            aAU = ConversionHelper.roundToSigFigs(aAU, 4);
            apoAU = ConversionHelper.roundToSigFigs(apoAU, 4);
            periAU = ConversionHelper.roundToSigFigs(periAU, 4);

            panel.add(new JLabel("Semi-Major Axis (AU): " + aAU));
            panel.add(new JLabel("Apoapsis (AU): " + apoAU));
            panel.add(new JLabel("Periapsis (AU): " + periAU));
            panel.add(new JLabel("Eccentricity: " + body.orbitalElements.e));
            panel.add(new JLabel("Mean anomaly (rad): " + body.orbitalElements.meanAnomalyAtEpoch));
            panel.add(new JLabel("Argument of periapsis (deg): " +
                    body.orbitalElements.argPeriapsis * 180.0 / Math.PI));
            panel.add(new JLabel("Period (days): " + body.orbitalElements.getPeriod()));
        }

        return panel;
    }

}
