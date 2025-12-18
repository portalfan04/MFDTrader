package simulation;

import flight.Ship;

import java.util.ArrayList;

public class Organisation
{
    private String name;
    private ArrayList<Ship> allLegacyShips = new ArrayList<>();
    private Universe universe;
    private Celestial homeworld;
    public Organisation(String n, Universe u, Celestial hw)
    {
        name = n;
        universe = u;
        homeworld = hw;
    }
    public void addShip(Ship s)
    {
        allLegacyShips.add(s);
    }
    public void update(double dt)
    {
        for (Ship legacyShip : allLegacyShips)
        {
            legacyShip.update(dt);
        }
    }

    public ArrayList<Ship> getShips()
    {
        return allLegacyShips;
    }

    public String getName()
    {
        return name;
    }
}
