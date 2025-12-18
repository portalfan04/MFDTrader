package economy.resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceContainer
{
    private double currentMass;
    private double maxMass;
    private String resourceID;
    public ResourceContainer(String resourceID, double maxMass)
    {
        this.maxMass = maxMass;
        this.resourceID = resourceID;
    }
    public ResourceContainer(String resourceID, double maxMass, double currentMass)
    {
        this.currentMass = currentMass;
        this.maxMass = maxMass;
        this.resourceID = resourceID;
    }
    public double getCurrentMass()
    {
        return currentMass;
    }
    public double getMaxMass()
    {
        return maxMass;
    }
    public String getResourceID() { return resourceID; }

    public boolean canAdd(double massKg)
    {
        return currentMass + massKg <= maxMass;
    }
    public void add(double massKg)
    {
        if(canAdd(massKg))
        {
            currentMass += massKg;
        }
        else
        {
            System.out.println("Could not add fuel!");
        }
    }
    public double addToMax(double massKg)
    {
        double remainer = getEmptyPortion() - massKg;
        fill();
        return remainer;
    }
    public double getEmptyPortion()
    {
        return maxMass - currentMass;
    }
    public void fill()
    {
        currentMass = maxMass;
    }

}
