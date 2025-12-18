package flight.step;

import flight.Ship;

public abstract class FlightStep {
    private String description;
    public FlightStep(String description) { this.description = description; }
    public String getDescription() { return description; }
    public abstract void execute(Ship s);
    public abstract double getWait(); // in seconds
}

