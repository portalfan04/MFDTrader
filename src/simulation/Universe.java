import Ship.Ship;

import java.util.ArrayList;
import java.util.List;

public class Universe {
    private List<SolarSystem> solarSystems = new ArrayList<>();
    private List<Organisation> organisations = new ArrayList<>();
    public int time;
    public enum TimeMode { REAL_TIME, TURN_BASED }
    private TimeMode mode = TimeMode.REAL_TIME;
    private double timeStep = 60 * 60 * 24; // default: 1 day in seconds
    private double speedMultiplier = 1.0;   // 1x speed


    public Universe()
    {
        time = 0;
    }
    public void tick()
    {
        if (mode == TimeMode.REAL_TIME)
        {
            update(timeStep * speedMultiplier / 30.0);
            // if Timer is ~33ms, 30 frames ≈ 1 second
        }
    }
    public void update(double dt)
    {
        time += dt;
        for (SolarSystem system : solarSystems)
        {
            system.update(dt);
        }
        for (Organisation org : organisations)
        {
            org.update(dt);
        }
    }
    public void advanceTurn()
    {
        if (mode == TimeMode.TURN_BASED)
        {
            update(timeStep);
        }
    }

    public void setMode(TimeMode mode) {
        this.mode = mode;
    }

    public TimeMode getMode()
    {
        return mode;
    }

    public void setTimeStep(double seconds)
    {
        this.timeStep = seconds;
    }

    public void setSpeedMultiplier(double multiplier)
    {
        this.speedMultiplier = multiplier;
    }
    public void addSolarSystem(SolarSystem system)
    {
        solarSystems.add(system);
    }

    public List<SolarSystem> getSolarSystems()
    {
        return solarSystems;
    }

    // Flatten all bodies for rendering
    public List<Celestial> getAllBodies() {
        List<Celestial> bodies = new ArrayList<>();
        for (SolarSystem system : solarSystems) {
            collectBodies(system.getRoot(), bodies);
        }
        return bodies;
    }

    private void collectBodies(Celestial current, List<Celestial> bodies) {
        bodies.add(current);
        for (Celestial child : current.children) {
            collectBodies(child, bodies);
        }
    }
    public void addOrganisation(Organisation o)
    {
        organisations.add(o);
    }
    public ArrayList<Ship> getShips()
    {
        ArrayList<Ship> ships = new ArrayList<>();
        for (Organisation org : organisations)
        {
            ships.addAll(org.getShips());
        }
        return ships;
    }
}
