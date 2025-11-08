import Ship.Ship;
import Simulation.Celestial;
import Simulation.Universe;

import java.util.ArrayList;

public class Organisation
{
    private String name;
    private ArrayList<Ship> allShips = new ArrayList<>();
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
        allShips.add(s);
    }
    public void update(double dt)
    {
        for (Ship ship : allShips)
        {
            ship.update(dt);
        }
    }

    public ArrayList<Ship> getShips()
    {
        return allShips;
    }
}
