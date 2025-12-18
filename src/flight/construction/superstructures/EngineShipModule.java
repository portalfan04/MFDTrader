package flight.construction.superstructures;

import flight.construction.parts.Part;
import flight.construction.parts.propulsion.Engine;
import flight.construction.parts.propulsion.EngineConfiguration;
import flight.construction.parts.propulsion.FuelMixture;

import java.util.ArrayList;
import java.util.List;

public class EngineShipModule extends ShipModule
{

    private List<Engine> allEngines = new ArrayList<>();
    private List<Engine> activeEngines = new ArrayList<>(); // engines that are able to run

    private EngineConfiguration activeConfig;

    public void addEngine(Engine e)
    {
        allEngines.add(e);
    }
    public void removeEngine(Engine e) { allEngines.remove(e); }
    public void addPart(Part e)
    {
        if (e instanceof Engine)
        {
            addEngine((Engine) e);
        }
    }
    public void enableEngine(Engine e)
    {
        activeEngines.add(e);
    }
    public void disableEngine(Engine e)
    {
        activeEngines.remove(e);
    }

    public List<Engine> getAllEngines()
    {
        return allEngines;
    }
    public List<Engine> getActiveEngines()
    {
        return activeEngines;
    }

    public void setConfiguration(EngineConfiguration config)
    {
        this.activeConfig = config;
    }

    public EngineConfiguration getActiveConfiguration()
    {
        return activeConfig;
    }

    // -----------------------------------------------------
    // Mass (structural + engines)
    // -----------------------------------------------------
    public double getMass()
    {
        double m = 0;
        for (Engine e : allEngines)
        {
            m += e.getMass();
        }
        return m;
    }

    // -----------------------------------------------------
    // Engine failure handling
    // -----------------------------------------------------
    public boolean isConfigurationValid()
    {
        if (activeConfig == null)
        {
            return false;
        }

        long working = allEngines.stream().filter(e -> !e.hasFailed()).count();

        if (activeConfig.requiresAllEngines())
        {
            return working >= activeConfig.getMountPoints().size();
        }

        return working >= 1;
    }

    public double getSpecificImpulse()
    {
        if (activeEngines.isEmpty())
        {
            return 0.0;
        }

        double totalThrust = 0.0;
        double thrustOverIsp = 0.0;

        for (Engine e : activeEngines)
        {
            if (e.hasFailed())
            {
                continue;
            }

            double t = e.getThrustVacuum();
            double isp = e.getIspVacuum();

            totalThrust += t;
            thrustOverIsp += (t / isp);
        }

        if (totalThrust == 0 || thrustOverIsp == 0)
        {
            return 0.0;
        }

        return totalThrust / thrustOverIsp;
    }
    public FuelMixture getFuelMixture()
    {
        FuelMixture mix = new FuelMixture();

        if (activeEngines.isEmpty())
        {
            return mix;
        }

        double totalThrust = 0.0;

        // First pass: total working thrust
        for (Engine e : activeEngines)
        {
            if (!e.hasFailed())
            {
                totalThrust += e.getThrustVacuum();
            }
        }

        if (totalThrust <= 0.0)
        {
            return mix;
        }

        // Second pass: weighted mixture accumulation
        for (Engine e : activeEngines)
        {
            if (e.hasFailed())
            {
                continue;
            }

            double weight = e.getThrustVacuum() / totalThrust;

            var engineMix = e.getMixture().getComponents();

            for (var entry : engineMix.entrySet())
            {
                String resource = entry.getKey();
                double ratio = entry.getValue();

                // Weighted contribution
                mix.add(resource, ratio * weight);
            }
        }

        // Make ratios add up to 1.0
        mix.normalize();

        return mix;
    }

}
