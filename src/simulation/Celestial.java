package simulation;

import economy.Economy;
import util.CONST;
import util.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Celestial implements Serializable
{
    private static final long serialVersionUID = 1L;
    // meta attributes
    public String name;
    public Color color;

    // physical attributes
    public double mass;
    public double radius;
    public CelestialType type = CelestialType.TERRESTRIAL;
    public Environment environment;

    // shipside attributes
    private double parkingAltitude;

    // orbital attributes
    public boolean isBarycenter;
    public Vector2 mapPosition;
    public Celestial parent;
    public List<Celestial> children = new ArrayList<>();
    public OrbitalElements orbitalElements;

    // economic attributes
    public Economy economy;

    public Celestial(String name)
    {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    public double getMu() {
        return mass * CONST.G;
    }
    public double getParkingAltitude()
    {
        if (parkingAltitude == 0)
        {
            double mult = 1;
            if(isBarycenter)
            {
                mult *= 1.5; // multiply by two if barycenter;
            }
            return switch (type)
            {
                case STAR -> 150_000_000_000d * mult; // 1 AU
                case GASEOUS -> 500_000_000 * mult; // 1,000,000 km, roughly Ganymede's orbit. Avoids most of the radiation.
                case DWARF -> 15_000_000_000d * mult; // 0.1 AU.
                case ASTEROID -> 10_000 * mult; // 10 km. Barely an orbit
                default -> 100_000;
            };
        }
        return parkingAltitude;
    }
    /**
     * Compute the absolute position at time t (seconds since epoch).
     * For root bodies (like stars), just return origin.
     */
    public Vector2 getPositionAtTime(double t)
    {
        if (parent == null) return mapPosition;
        if (orbitalElements == null) return parent.getPositionAtTime(t);

        Vector2 parentPos = parent.getPositionAtTime(t);
        Vector2 relPos = orbitalElements.computePosition2D(t);
        return parentPos.add(relPos);
    }
    /**
     * Compute orbital velocity in 2D inertial frame.
     * Derived analytically from orbital elements.
     */
    public Vector2 getVelocityAtTime(double t)
    {
        if (parent == null) return new Vector2(0, 0);
        if (orbitalElements == null) return parent.getVelocityAtTime(t);

        Vector2 parentVel = parent.getVelocityAtTime(t);
        Vector2 relVel = orbitalElements.computeVelocity2D(t, parent.mass);
        return parentVel.add(relVel);
    }
}
