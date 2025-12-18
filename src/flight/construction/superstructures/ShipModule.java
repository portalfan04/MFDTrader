package flight.construction.superstructures;

import flight.construction.IMass;
import flight.construction.IResourceMass;
import flight.construction.parts.ContainerPart;
import flight.construction.parts.Part;
import flight.construction.sections.Section;

import java.util.ArrayList;

public abstract class ShipModule implements IMass, IResourceMass
{
    ArrayList<Section> sections = new ArrayList<>();
    protected String name;

    public ArrayList<Section> getSections()
    {
        return sections;
    }

    public ArrayList<Part> getParts()
    {
        ArrayList<Part> parts = new ArrayList<>();
        for (Section s : sections)
        {
            parts.addAll(s.getParts());
        }
        return parts;
    }
    public double getMass()
    {
        double mass = 0;
        for (Section s : sections)
        {
            mass += s.getMass();
        }
        return mass;
    }

    public double getResourceMass(String resourceID)
    {
        double mass = 0.0;

        for (Part part : this.getParts())
        {
            if (part instanceof ContainerPart tankPart)
            {
                ContainerPart tank = (ContainerPart) part;
                mass += tank.getResourceMass(resourceID); // pass resourceID here
            }
        }

        return mass;
    }

    public void addPartToSection(Part p, Section s)
    {
        if(sections.contains(s))
        {
            s.addPart(p);
        }
        else
        {
            System.out.println("Warning: attempted to add part " + p.getName() + " to section " + s.getName() + " in module " + this.name);
        }
    }

    public void addSection(Section section)
    {
        sections.add(section);
    }
}
