package simulation;

import util.Vector2;

import java.util.ArrayList;
import java.util.List;

public class SolarSystem
{
    private Celestial root;
    private List<Celestial> celestials;

    public SolarSystem(Celestial root)
    {
        this.root = root;
        this.celestials = new ArrayList<>();
        collectBodies(root); // gather all children automatically
    }

    /**
     * In our new model, updating just means advancing simulation time if needed.
     * Positions are computed on demand using getPositionAtTime().
     */
    public void update(double dt)
    {
        // Nothing to integrate here — orbits are analytic now.
        // Could be used for non-orbital updates (e.g. ships or anomalies)
    }

    public Celestial getRoot() {
        return root;
    }

    public List<Celestial> getAllBodies() {
        return celestials;
    }

    /**
     * Helper to recursively collect all bodies.
     */
    private void collectBodies(Celestial body)
    {
        celestials.add(body);
        for (Celestial child : body.children)
        {
            collectBodies(child);
        }
    }

    /**
     * Print the analytic position of every body at the given time (seconds since epoch)
     */
    public void printPositions(double timeSeconds)
    {
        printRecursive(root, 0, timeSeconds);
    }

    private void printRecursive(Celestial body, int level, double t)
    {
        Vector2 pos = body.getPositionAtTime(t);
        System.out.println("  ".repeat(level) + body.name + " at " + pos);
        for (Celestial child : body.children) {
            printRecursive(child, level + 1, t);
        }
    }
}
