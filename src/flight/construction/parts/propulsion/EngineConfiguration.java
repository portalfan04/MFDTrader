package flight.construction.parts.propulsion;

import util.Vector3;

import java.util.List;

public class EngineConfiguration
{
    private String name;

    // normalized thrust direction (global)
    private Vector3 thrustDirection;

    // mount positions relative to ship origin (meters)
    private List<Vector3> mountPoints;

    // requires engine count >= mountPoints.size()
    private boolean requiresAllEngines;

    public EngineConfiguration(String name, Vector3 thrustDirection, List<Vector3> mountPoints, boolean requiresAllEngines)
    {
        this.name = name;
        this.thrustDirection = thrustDirection.normalized();
        this.mountPoints = mountPoints;
        this.requiresAllEngines = requiresAllEngines;
    }

    public String getName()
    {
        return name;
    }

    public Vector3 getThrustDirection()
    {
        return thrustDirection;
    }

    public List<Vector3> getMountPoints()
    {
        return mountPoints;
    }

    public boolean requiresAllEngines()
    {
        return requiresAllEngines;
    }
}
