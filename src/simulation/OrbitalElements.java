package simulation;

import util.CONST;
import util.Vector2;

import java.io.Serializable;

public class OrbitalElements implements Serializable
{
    private static final long serialVersionUID = 1L;

    public double a;           // semi-major axis (m)
    public double e;           // eccentricity
    public double argPeriapsis; // radians
    public double meanAnomalyAtEpoch; // radians at t = 0
    public double meanMotion;  // radians per second

    public double centralMass; // kg

    public OrbitalElements(double a, double e, double argPeriapsisDeg, double meanAnomalyDeg, double centralMass)
    {
        this.a = a;
        this.e = e;
        this.argPeriapsis = Math.toRadians(argPeriapsisDeg);
        this.meanAnomalyAtEpoch = Math.toRadians(meanAnomalyDeg);
        this.centralMass = centralMass;
        double mu = CONST.G * centralMass;
        this.meanMotion = Math.sqrt(mu / Math.pow(a, 3));
    }

    public double getPeriod()
    {
        return 2 * Math.PI / meanMotion;
    }

    /**
     * Compute position in orbital plane at time t since epoch (seconds).
     * Returns vector in parent-centered frame.
     */
    public Vector2 computePosition2D(double t)
    {
        double M = meanAnomalyAtEpoch + meanMotion * t;
        M %= 2 * Math.PI;

        // Solve Kepler’s equation
        double E = solveEccentricAnomaly(M, e);

        // True anomaly
        double ν = 2 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2),
                Math.sqrt(1 - e) * Math.cos(E / 2));

        // Distance from focus
        double r = a * (1 - e * Math.cos(E));

        // Cartesian coordinates in orbital plane
        double x = r * Math.cos(ν + argPeriapsis);
        double y = r * Math.sin(ν + argPeriapsis);

        return new Vector2(x, y);
    }

    /**
     * Compute orbital velocity in 2D orbital plane.
     */
    public Vector2 computeVelocity2D(double t, double centralMass)
    {
        double M = meanAnomalyAtEpoch + meanMotion * t;
        M %= 2 * Math.PI;

        double E = solveEccentricAnomaly(M, e);
        double ν = 2 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2),
                Math.sqrt(1 - e) * Math.cos(E / 2));

        double mu = CONST.G * centralMass;
        double r = a * (1 - e * Math.cos(E));

        double h = Math.sqrt(mu * a * (1 - e * e));

        double vx = -mu / h * Math.sin(ν + argPeriapsis);
        double vy = mu / h * (e + Math.cos(ν + argPeriapsis));
        return new Vector2(vx, vy);
    }

    private double solveEccentricAnomaly(double M, double e)
    {
        double E = M;
        for (int i = 0; i < 15; i++)
        {
            E = E - (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
        }
        return E;
    }

    public double getPeriapsis()
    {
        return a * (1.0 - e);
    }

    public double getApoapsis()
    {
        return a * (1.0 + e);
    }

}
