package flight;

import java.util.ArrayList;

public class FlightPlanRepository
{
    public static final ArrayList<FlightPlan> allPlans = new ArrayList<>();

    public static void addPlan(FlightPlan fp)
    {
        allPlans.add(fp);
    }

    public static ArrayList<FlightPlan> getAllPlans()
    {
        return allPlans;
    }

    public static FlightPlan findByName(String name)
    {
        for (FlightPlan fp : allPlans)
        {
            if (fp.getName().equalsIgnoreCase(name))
                return fp;
        }
        return null;
    }
}
