package flight.construction.parts.propulsion;

import economy.resource.ResourceContainer;
import flight.construction.parts.Part;

public class Engine extends Part
{
    private String name;

    private FuelMixture mixture;

    private double thrustVacuum;   // kN
    private double ispVacuum;      // seconds

    private boolean enabled = true;
    private boolean failed = false;

    // engines may throttle 0–1
    private double throttle = 1.0;

    public Engine(String name, double mass, FuelMixture mixture, double thrustVacuum, double ispVacuum)
    {
        super(name, mass, "engine_generic");
        this.name = name;
        this.mixture = mixture;
        this.thrustVacuum = thrustVacuum;
        this.ispVacuum = ispVacuum;
    }

    public String getName()
    {
        return name;
    }

    public FuelMixture getMixture()
    {
        return mixture;
    }

    public double getThrustVacuum()
    {
        return thrustVacuum;
    }

    public double getIspVacuum()
    {
        return ispVacuum;
    }

    public void setThrottle(double t)
    {
        this.throttle = Math.max(0.0, Math.min(1.0, t));
    }

    public double getThrottle()
    {
        return throttle;
    }

    public boolean isEnabled()
    {
        return enabled && !failed;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean hasFailed()
    {
        return failed;
    }

    public void setFailed(boolean failed)
    {
        this.failed = failed;
    }

    // ----------------------------------------------------------
    // Core engine logic
    // ----------------------------------------------------------

    // thrust in kN scaled by throttle
    public double computeThrust()
    {
        if (!isEnabled())
        {
            return 0.0;
        }
        return thrustVacuum * throttle;
    }
}
