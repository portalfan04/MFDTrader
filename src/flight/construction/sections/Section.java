package flight.construction.sections;

import flight.construction.IMass;
import flight.construction.parts.ContainerPart;
import flight.construction.parts.Part;
import flight.construction.superstructures.PayloadShipModule;
import flight.construction.superstructures.ShipModule;

import java.util.ArrayList;

public class Section implements IMass
{

    protected String name;
    protected double mass;
    protected String socketName;
    private ShipModule module;
    private ArrayList<Part> parts = new ArrayList<>();

    public Section(String name, double mass, String socketName)
    {
        this.name = name;
        this.mass = mass;
        this.socketName = socketName;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public double getMass()
    {
        double sum = 0;
        for (Part p : parts)
        {
            sum += p.getMass();
            if (p instanceof ContainerPart)
            {
                ContainerPart tank = (ContainerPart) p;
                mass += tank.getStoredMass();
            }
        }
        return sum;
    }
    public void addPart(Part p)
    {
        if (p.getSocketName().equals(this.socketName))
        {
            parts.add(p);
        }
        else
        {
            System.out.println("Warning: attempted to add part " + p.getName() + " to section " + this.name + " with socket " + this.socketName);
        }
    }
    public ArrayList<Part> getParts()
    {
        return parts;
    }
    public void removePart(Part p)
    {
        parts.remove(p);
    }

    public ShipModule getModule()
    {
        return module;
    }
    public Section cloneSection()
    {
        return new Section(this.name, this.mass, this.socketName);
    }

    public void setModule(ShipModule payloadModule)
    {
        this.module = payloadModule;
    }
}
