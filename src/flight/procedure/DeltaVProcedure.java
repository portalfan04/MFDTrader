package flight.procedure;

import simulation.Celestial;

public abstract class DeltaVProcedure extends FlightProcedure
{
    protected double maxDeltaV;
    public double getMaxDeltaV()
    {
        return maxDeltaV;
    }
    public void setMaxDeltaV(double v)
    {
        this.maxDeltaV = v;
    }

}
