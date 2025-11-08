package Ship;

import Util.ConversionHelper;

import java.util.ArrayDeque;

public class Ship {

    private String name;
    private double deltaV; // total delta-v available
    private FlightPlan plan;
    private ArrayDeque<FlightStep> flightQueue = new ArrayDeque<>();
    public ShipState stepState;
    public FlightState flightState;
    public ShipTaskState taskState;
    public double timeToWait;
    public Celestial location;
    public Celestial target;
    public Organisation owner;
    private double currentSimTime = 0; // keeps track of total sim time for step scheduling

    public Ship(String name, double deltaV, Celestial location, Organisation org) {
        this.name = name;
        this.deltaV = deltaV;
        this.timeToWait = 0;
        this.location = location;
        this.owner = org;

        this.stepState = ShipState.WAITING;
        this.flightState = FlightState.ORBITING;
        this.taskState = ShipTaskState.OFF;
    }

    public String getName() { return name; }
    public double getDeltaV() { return deltaV; }

    public void setFlightPlan(FlightPlan fp) {
        this.plan = fp;
        refillQueueIfNeeded(); // pull in first procedure right away
    }

    // ship tick
    public void update(double dt) {
        if (stepState != ShipState.WAITING) return;

        currentSimTime += dt;
        timeToWait -= dt;

        while (timeToWait <= 0)
        {
            // if queue empty, pull next procedure
            refillQueueIfNeeded();
            if (flightQueue.isEmpty()) {
                stepState = ShipState.INACTIVE;
                return;
            }

            FlightStep step = flightQueue.poll();
            if (step == null) break;

            step.execute(this);
            timeToWait = step.getWait();
        }
    }

    private void refillQueueIfNeeded() {
        if (plan == null) return;
        if (flightQueue.isEmpty()) {
            FlightProcedure proc = plan.getNextProcedure();
            if (proc == null) return; // done
            flightQueue.addAll(proc.generateSteps(this, currentSimTime));
        }
    }

    public void burn(double dv) {
        if (dv <= deltaV) {
            deltaV -= dv;
            System.out.println(name + " burned " + dv + " m/s. Remaining Δv: " + deltaV);
        } else {
            System.out.println("PlanFault on ship " + name + ": insufficient deltaV!");
            stepState = ShipState.PLAN_FAULT;
        }
    }
    public boolean isInTransfer()
    {
        return (flightState.equals(FlightState.XFER));
    }

    public void setStateByString(String state)
    {
        switch (state)
        {
            case "orb":
            {
                this.flightState = FlightState.ORBITING;
                break;
            }
            case "dock":
            {
                this.flightState = FlightState.DOCKED;
                break;
            }
            case "wnd":
            {
                this.flightState = FlightState.WAIT_WINDOW;
                break;
            }
            case "xfer":
            {
                this.flightState = FlightState.XFER;
                break;
            }
        }
    }
    public String getStatus()
    {
        switch (flightState)
        {
            case FlightState.DOCKED:
            {
                return "Docked at "; // + dockedBerth
            }
            case FlightState.ORBITING:
            {
                return "Orbiting " + location;
            }
            case FlightState.XFER:
            {
                return "En route to " + target + ", arrival in " + Math.round(ConversionHelper.secondToDay(timeToWait)) + " days";
            }
            case FlightState.WAIT_WINDOW:
            {
                return "Orbiting " + location + ", burning in " + Math.round(ConversionHelper.secondToDay(timeToWait)) + " days";
            }
            default:
            {
                return "???";
            }
        }
    }
}
