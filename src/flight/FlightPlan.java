package flight;

import flight.procedure.FlightProcedure;
import simulation.Celestial;

import java.util.ArrayList;

public class FlightPlan {
    private ArrayList<FlightProcedure> procedures = new ArrayList<>();
    private String name;
    private String description;
    private boolean repeat = true;
    private int currentIndex = 0;

    public FlightPlan(boolean repeat)
    {
        this.repeat = repeat;
    }

    public void addProcedure(FlightProcedure proc)
    {
        procedures.add(proc);
    }

    public FlightProcedure getNextProcedure()
    {
        if(procedures.isEmpty()) return null;
        FlightProcedure proc = procedures.get(currentIndex);
        currentIndex++;
        if(currentIndex >= procedures.size())
        {
            if(repeat) currentIndex = 0;
            else currentIndex = procedures.size();
        }
        return proc;
    }

    public boolean isComplete()
    {
        return !repeat && currentIndex >= procedures.size();
    }

    public void setName(String text)
    {
        this.name = text;
    }
    public String getName()
    {
        return name;
    }

    public boolean isRepeat()
    {
        return repeat;
    }

    public ArrayList<FlightProcedure> getProcedures()
    {
        return procedures;
    }
    public Celestial getOrigin()
    {
        return procedures.get(0).getProcedureOrigin();
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
}