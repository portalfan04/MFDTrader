package flight.procedure;

import flight.*;
import flight.step.*;
import simulation.Celestial;


import java.util.ArrayList;

public abstract class FlightProcedure
{
    public abstract ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime);
    public abstract String getTimeLineDescription();
    public abstract String getTypeName();
    public abstract Celestial getProcedureOrigin();
    public abstract Celestial getProcedureDestination();
}
