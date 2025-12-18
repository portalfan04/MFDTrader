package economy.resource;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ResourceDefinition
{
    private String id;
    private String name;
    private double density;
    private ArrayList<String> tags;
    private String description;

    public ResourceDefinition(String id, String name, double density, ArrayList<String> tags, String description)
    {
        this.id = id;
        this.name = name;
        this.density = density;
        this.tags = tags;
        this.description = description;
        ResourceDatabase.register(this);
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public double getDensity()
    {
        return density;
    }

    public ArrayList<String> getTags()
    {
        return tags;
    }

    public String getDescription()
    {
        return description;
    }
}
