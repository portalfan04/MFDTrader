package util;

public class Vector3
{
    public final double x;
    public final double y;
    public final double z;

    public Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // ----------------------------------------------------
    // Static helpers
    // ----------------------------------------------------
    public static Vector3 zero()
    {
        return new Vector3(0, 0, 0);
    }

    public static Vector3 unitX()
    {
        return new Vector3(1, 0, 0);
    }

    public static Vector3 unitY()
    {
        return new Vector3(0, 1, 0);
    }

    public static Vector3 unitZ()
    {
        return new Vector3(0, 0, 1);
    }

    // ----------------------------------------------------
    // Basic arithmetic
    // ----------------------------------------------------
    public Vector3 add(Vector3 v)
    {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 subtract(Vector3 v)
    {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 multiply(double s)
    {
        return new Vector3(x * s, y * s, z * s);
    }

    public Vector3 divide(double s)
    {
        if (s == 0.0)
        {
            throw new IllegalArgumentException("Vector divide by zero");
        }
        return new Vector3(x / s, y / s, z / s);
    }

    // ----------------------------------------------------
    // Magnitude and normalization
    // ----------------------------------------------------
    public double magnitude()
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double sqrMagnitude()
    {
        return x * x + y * y + z * z;
    }

    public Vector3 normalized()
    {
        double m = magnitude();
        if (m == 0.0)
        {
            return zero();
        }
        return divide(m);
    }

    // ----------------------------------------------------
    // Dot & Cross products
    // ----------------------------------------------------
    public double dot(Vector3 v)
    {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3 cross(Vector3 v)
    {
        return new Vector3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x
        );
    }

    // ----------------------------------------------------
    // Distance helpers
    // ----------------------------------------------------
    public double distanceTo(Vector3 v)
    {
        return subtract(v).magnitude();
    }

    public double sqrDistanceTo(Vector3 v)
    {
        return subtract(v).sqrMagnitude();
    }

    // ----------------------------------------------------
    // Utility
    // ----------------------------------------------------
    @Override
    public String toString()
    {
        return "Vector3(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof Vector3))
        {
            return false;
        }

        Vector3 v = (Vector3)obj;
        return Double.compare(v.x, x) == 0
                && Double.compare(v.y, y) == 0
                && Double.compare(v.z, z) == 0;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x);
        bits = bits * 31 + Double.doubleToLongBits(y);
        bits = bits * 31 + Double.doubleToLongBits(z);
        return (int)(bits ^ (bits >>> 32));
    }
}
