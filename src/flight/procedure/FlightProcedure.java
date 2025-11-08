package flight;

import simulation.Celestial;
import util.CONST;
import util.ConversionHelper;
import util.LambertSolver;
import util.Vector2;

import java.util.ArrayList;

abstract class FlightProcedure
{
    public abstract ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime);
}
public class HohmannTransferProcedure extends FlightProcedure
{
    private Celestial origin, destination;
    private double maxDeltaV;
    private double roughTravelTime;
    private double searchIncrement = 86400; // 1 day in seconds

    public HohmannTransferProcedure(Celestial origin, Celestial destination, double maxDV, double roughTime) {
        this.origin = origin;
        this.destination = destination;
        this.maxDeltaV = maxDV;
        this.roughTravelTime = roughTime;
    }

    @Override
    public ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime) {
        if (s.location != origin)
            throw new RuntimeException("Ship.Ship not at origin!");

        double candidateDep = cumulativeTime;
        double travelTime, totalDV;

        while(true) {
            travelTime = estimateTransferTime(origin, destination, candidateDep);
            totalDV = estimateDV(origin, destination, candidateDep, travelTime);
            if(!Double.isNaN(totalDV) && totalDV <= maxDeltaV) break;
            candidateDep += searchIncrement;
            if(candidateDep > cumulativeTime + roughTravelTime * 10)
                throw new RuntimeException("Cannot find valid Hohmann transfer!");
        }

        ArrayList<FlightStep> steps = new ArrayList<>();
        steps.add(new StateStep("wnd"));
        steps.add(new WaitStep(candidateDep - cumulativeTime));
        steps.add(new BurnStep(totalDV / 2));
        steps.add(new InterceptStep(origin.parent));
        steps.add(new StateStep("xfer"));
        steps.add(new WaitStep(travelTime));
        steps.add(new BurnStep(totalDV / 2));
        steps.add(new InterceptStep(destination));
        steps.add(new StateStep("orb"));

        s.target = destination;

        System.out.println("Hohmann transfer window found");
        System.out.println("For " + origin + " to " + destination);
        System.out.println("Burning at " + ConversionHelper.secondToDay(candidateDep - cumulativeTime) + " days");
        System.out.println("With a travel time of " + ConversionHelper.secondToDay(travelTime) + " days");

        return steps;
    }

    private double estimateTransferTime(Celestial o, Celestial d, double dep)
    {
        double mu = CONST.G * o.parent.mass;
        double r1 = o.getPositionAtTime(dep).magnitude();
        double r2 = d.getPositionAtTime(dep + roughTravelTime).magnitude();
        double a = (r1 + r2) / 2.0;
        return Math.PI * Math.sqrt(a * a * a / mu);
    }

    private double estimateDV(Celestial o, Celestial d, double dep, double tof)
    {
        Vector2 r1 = o.getPositionAtTime(dep);
        Vector2 r2 = d.getPositionAtTime(dep + tof);
        Vector2 v1 = o.getVelocityAtTime(dep);
        Vector2 v2 = d.getVelocityAtTime(dep + tof);

        LambertSolver.Result res = LambertSolver.solve(r1, r2, tof, CONST.G * o.parent.mass);
        if(!res.success) return Double.NaN;

        double dvDepart = res.vDepart.subtract(v1).magnitude();
        double dvArrive = res.vArrive.subtract(v2).magnitude();
        return dvDepart + dvArrive;
    }
}
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
}
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
}