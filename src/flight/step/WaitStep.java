package flight.step;

import flight.Ship;

public class WaitStep extends FlightStep
{

    private double waitTime;

    public WaitStep(double waitTime)
    {
        super("Wait " + waitTime + "s");
        this.waitTime = waitTime;
    }

    public double getWait()
    {
        return waitTime;
    }

    public void execute(Ship s)
    { /* nothing, ship waits */ }
}
