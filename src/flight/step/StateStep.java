package flight.step;

import flight.Ship;

public class StateStep extends FlightStep
{

    private String state;

    public StateStep(String stater)
    {
        super("Modify ship state to: " + stater);
        this.state = stater;
    }

    public double getWait()
    {
        return 0;
    }

    public void execute(Ship s)
    {
        s.setStateByString(state);
    }
}
