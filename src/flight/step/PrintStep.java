package flight.step;

import flight.Ship;

public class PrintStep extends FlightStep
{

    private String message;

    public PrintStep(String message)
    {
        super("Print: " + message);
        this.message = message;
    }

    public double getWait()
    {
        return 0;
    }

    public void execute(Ship s)
    {
        System.out.println(s.getName() + ": " + message);
    }
}
