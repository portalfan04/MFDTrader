package flight;

import simulation.Celestial;

abstract class FlightStep {
    private String description;
    public FlightStep(String description) { this.description = description; }
    public String getDescription() { return description; }
    public abstract void execute(Ship s);
    public abstract double getWait(); // in seconds
}

class WaitStep extends FlightStep {
    private double waitTime;
    public WaitStep(double waitTime) { super("Wait " + waitTime + "s"); this.waitTime = waitTime; }
    public double getWait() { return waitTime; }
    public void execute(Ship s) { /* nothing, ship waits */ }
}

class BurnStep extends FlightStep {
    private double deltaV;
    public BurnStep(double deltaV) { super("Burn " + deltaV + " m/s"); this.deltaV = deltaV; }
    public double getWait() { return 0; }
    public void execute(Ship s) { s.burn(deltaV); }
}

class InterceptStep extends FlightStep {
    private Celestial target;
    public InterceptStep(Celestial target) { super("Intercept " + target.name); this.target = target; }
    public double getWait() { return 0; }
    public void execute(Ship s) { s.location = target; }
}

class PrintStep extends FlightStep {
    private String message;
    public PrintStep(String message) { super("Print: " + message); this.message = message; }
    public double getWait() { return 0; }
    public void execute(Ship s) { System.out.println(s.getName() + ": " + message); }
}
class StateStep extends FlightStep
{
    private String state;
    public StateStep(String stater)
    {
        super("Modify ship state to: " + stater);
        this.state = stater;
    }
    public double getWait() { return 0; }
    public void execute(Ship s)
    {
        s.setStateByString(state);
    }
}
