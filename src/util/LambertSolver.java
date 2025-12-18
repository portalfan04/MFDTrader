package util;

import util.Vector2;

/**
 * Lambert solver using the universal variable formulation.
 * Returns departure+arrival velocity vectors in the same inertial frame as r1/r2.
 *
 * Note: This implementation assumes non-coplanar issues aren't special-cased,
 * and uses the short-way (dtheta <= PI) transfer by construction.
 */
public class LambertSolver {

    public static class Result {
        public boolean success;
        public Vector2 vDepart; // velocity at r1 (in same frame)
        public Vector2 vArrive; // velocity at r2 (in same frame)
    }

    /**
     * Solve Lambert for transfer from r1 -> r2 in time dt (seconds).
     * mu is the gravitational parameter of the central body (m^3/s^2).
     */
    public static Result solve(Vector2 r1, Vector2 r2, double dt, double mu) {
        Result out = new Result();
        out.success = false;

        double r1mag = r1.magnitude();
        double r2mag = r2.magnitude();
        double dot = r1.dot(r2);
        double cosDelta = dot / (r1mag * r2mag);
        cosDelta = Math.max(-1.0, Math.min(1.0, cosDelta));
        double dtheta = Math.acos(cosDelta);

        // choose short way
        if (r1.cross(r2) < 0) {
            // if cross negative, use 2π - dtheta to keep prograde sign
            dtheta = 2 * Math.PI - dtheta;
        }

        double A = Math.sin(dtheta) * Math.sqrt(r1mag * r2mag / (1.0 - Math.cos(dtheta)));
        if (Math.abs(A) < 1e-9) return out;

        // iterate on z to match time of flight
        double z = 0.0;
        double zUpper = 4 * Math.PI * Math.PI;
        double zLower = -4 * Math.PI;
        int maxIter = 200;
        double tol = 1e-6;

        double C = stumpC(z);
        double S = stumpS(z);
        double y = r1mag + r2mag + (A * (z * S - 1.0)) / Math.sqrt(Math.abs(C));
        if (y < 0) y = 0; // safeguard

        double x = Math.sqrt(Math.abs(y / C));
        double tof = (x * x * x * S + A * Math.sqrt(y)) / Math.sqrt(mu);

        int iter = 0;
        // Use bisection combined with Newton-like adjustments
        double zLow = -4 * Math.PI;
        double zHigh = 4 * Math.PI;
        while (Math.abs(tof - dt) > tol && iter < maxIter) {
            // adjust z with secant/bisection style
            if (tof <= dt) {
                zLow = z;
                if (Double.isInfinite(zHigh)) z = (z * 2.0 + zLow) / 3.0;
                else z = (z + zHigh) / 2.0;
            } else {
                zHigh = z;
                if (Double.isInfinite(zLow)) z = (z * 2.0 + zHigh) / 3.0;
                else z = (z + zLow) / 2.0;
            }

            C = stumpC(z);
            S = stumpS(z);

            // safe y
            double denom = Math.sqrt(Math.abs(C));
            if (denom == 0) {
                iter++;
                continue;
            }
            y = r1mag + r2mag + (A * (z * S - 1.0)) / denom;
            if (y < 0) {
                // expand search bracket
                if (z > 0) z *= 2.0;
                else z /= 2.0;
                iter++;
                continue;
            }

            x = Math.sqrt(Math.abs(y / C));
            tof = (x * x * x * S + A * Math.sqrt(y)) / Math.sqrt(mu);
            iter++;
        }

        if (iter >= maxIter) {
            // failure to converge
            return out;
        }

        // Compute f, g, fdot, gdot from universal variable solution
        C = stumpC(z);
        S = stumpS(z);
        y = r1mag + r2mag + (A * (z * S - 1.0)) / Math.sqrt(Math.abs(C));
        double f = 1.0 - y / r1mag;
        double g = A * Math.sqrt(y / mu);
        double gdot = 1.0 - y / r2mag;

        // Ensure g is not zero
        if (Math.abs(g) < 1e-12) return out;

        Vector2 v1 = r2.subtract(r1.multiply(f)).divide(g);
        Vector2 v2 = r2.multiply(gdot).subtract(r1).divide(g);

        out.success = true;
        out.vDepart = v1;
        out.vArrive = v2;
        return out;
    }

    private static double stumpC(double z) {
        if (z > 0) {
            double sz = Math.sqrt(z);
            return (1.0 - Math.cos(sz)) / z;
        } else if (z < 0) {
            double sz = Math.sqrt(-z);
            return (1.0 - Math.cosh(sz)) / z;
        } else {
            return 0.5;
        }
    }

    private static double stumpS(double z) {
        if (z > 0) {
            double sz = Math.sqrt(z);
            return (sz - Math.sin(sz)) / (sz * sz * sz);
        } else if (z < 0) {
            double sz = Math.sqrt(-z);
            return (Math.sinh(sz) - sz) / (sz * sz * sz);
        } else {
            return 1.0 / 6.0;
        }
    }
}
