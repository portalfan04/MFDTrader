package flight.procedure;

import flight.Ship;
import flight.step.FlightStep;
import flight.step.PrintStep;
import simulation.Celestial;

import java.util.ArrayList;

public class PrintProcedure extends FlightProcedure
{
    private String msg;
    public PrintProcedure(String m)
    {
        msg = m;
    }
    @Override
    public ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime)
    {
        ArrayList<FlightStep> step = new ArrayList<FlightStep>();
        step.add(new PrintStep(msg));
        return step;
    }

    @Override
    public String getTimeLineDescription()
    {
        return "Print: " + msg + "";
    }

    @Override
    public String getTypeName()
    {
        return "Print";
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

    public void setMessage(String input)
    {
        this.msg = input;
    }
    public String getMessage()
    {
        return msg;
    }

}