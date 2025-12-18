package flight.procedure;

import flight.Ship;
import flight.step.FlightStep;
import flight.step.WaitStep;
import simulation.Celestial;

import java.util.ArrayList;

public class WaitProcedure extends FlightProcedure
{
    private double waitTime;
    public WaitProcedure(double wt)
    {
        waitTime = wt;
    }
    @Override
    public ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime)
    {
        ArrayList<FlightStep> step = new ArrayList<FlightStep>();
        step.add(new WaitStep(waitTime));
        return step;
    }

    @Override
    public String getTimeLineDescription()
    {
        return "Wait for " + waitTime + "s";
    }

    @Override
    public String getTypeName()
    {
        return "Wait";
    }

    @Override
    public Celestial getProcedureOrigin()
    {
        return null;
    }

    @Override
    public Celestial getProcedureDestination()
    {
        return null;
    }

    public void setDuration(double v)
    {
        waitTime = v;
    }
    public double getDuration()
    {
        return waitTime;
    }
}