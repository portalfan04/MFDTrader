package flight.construction.parts;

import flight.construction.IMass;

public class Part implements IMass
{

    private String name;
    private double mass;
    private String socketName;

    /**
     * Creates a part
     *
     * @param name       Name of the part
     * @param dryMass    Dry mass of the part (kg)
     * @param socketName Name of the socket the part is connected to
     */
    public Part(String name, double dryMass, String socketName)
    {
        this.name = name;
        this.mass = dryMass;
        this.socketName = socketName;
    }

    public String getName()
    {
        return name;
    }
    public String getSocketName()
    {
        return socketName;
    }
    @Override
    public double getMass()
    {
        return this.mass;
    }

    public Part clonePart()
    {
        return new Part(this.name, this.mass, this.socketName);
    }

}
