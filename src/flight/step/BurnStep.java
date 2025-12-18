package flight.step;

import flight.Ship;

public class BurnStep extends FlightStep
{

    private double deltaV;

    public BurnStep(double deltaV)
    {
        super("Burn " + deltaV + " m/s");
        this.deltaV = deltaV;
    }

    public double getWait()
    {
        return 0;
    }

    public void execute(Ship s)
    {
        s.burn(deltaV);
    }
}
