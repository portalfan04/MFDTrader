package flight.construction.parts;

import economy.resource.ResourceContainer;
import economy.resource.ResourceDefinition;
import economy.resource.ResourceDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContainerPart extends Part
{
    private ArrayList<ResourceContainer> containers = new ArrayList<>();
    private HashMap<String, ResourceContainer> resourceMap = new HashMap<String, ResourceContainer>();

    public ContainerPart(double dryMass)
    {
        super("tank_", dryMass, "tank"); // change this later!
    }

    // -----------------------------------------------------
    // Total mass of a *specific* resource
    // -----------------------------------------------------
    public double getResourceMass(String resourceId)
    {
        if (resourceMap.containsKey(resourceId))
        {
            return resourceMap.get(resourceId).getCurrentMass();
        }

        return 0.0;
    }

    // -----------------------------------------------------
    // Total mass of ALL stored resources
    // -----------------------------------------------------
    public double getStoredMass()
    {
        double total = 0.0;

        for (ResourceContainer c : containers)
        {
            total += c.getCurrentMass();
        }

        return total;
    }

    // -----------------------------------------------------
    // Full part mass = dry mass + stored mass
    // -----------------------------------------------------
    @Override
    public double getMass()
    {
        return super.getMass() + getStoredMass();
    }

    public ArrayList<ResourceContainer> getContainers()
    {
        return containers;
    }
    public void addContainer(ResourceContainer rc)
    {
        containers.add(rc);
        resourceMap.put(rc.getResourceID(), rc);
    }

    public boolean hasResource(String resourceId)
    {
        return resourceMap.containsKey(resourceId);
    }
    public void fillAll()
    {
        for (ResourceContainer c : containers)
        {
            c.add(c.getMaxMass());
        }
    }
}
