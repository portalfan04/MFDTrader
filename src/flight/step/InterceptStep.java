package flight.step;

import flight.Ship;
import simulation.Celestial;

public class InterceptStep extends FlightStep
{

    private Celestial target;

    public InterceptStep(Celestial target)
    {
        super("Intercept " + target.name);
        this.target = target;
    }

    public double getWait()
    {
        return 0;
    }

    public void execute(Ship s)
    {
        s.location = target;
    }
}
