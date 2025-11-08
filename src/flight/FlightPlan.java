package Ship;

import java.util.ArrayList;

class FlightPlan {
    private ArrayList<FlightProcedure> procedures = new ArrayList<>();
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
}