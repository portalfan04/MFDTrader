import Util.CONST;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Celestial {
    public String name;
    public double mass;
    public double radius;
    public Color color;
    public Vector2 mapPosition;
    public Celestial parent;           // null for root (Sun)
    public List<Celestial> children = new ArrayList<>();
    public OrbitalElements orbitalElements; // null for stationary/star

    public Celestial(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public double getMu() {
        return mass * CONST.G;
    }

    /**
     * Compute the absolute position at time t (seconds since epoch).
     * For root bodies (like stars), just return origin.
     */
    public Vector2 getPositionAtTime(double t) {
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
    public Vector2 getVelocityAtTime(double t) {
        if (parent == null) return new Vector2(0, 0);
        if (orbitalElements == null) return parent.getVelocityAtTime(t);

        Vector2 parentVel = parent.getVelocityAtTime(t);
        Vector2 relVel = orbitalElements.computeVelocity2D(t, parent.mass);
        return parentVel.add(relVel);
    }
}
