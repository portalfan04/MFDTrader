package util;

import java.io.Serializable;

public class Vector2 implements Serializable
{
    private static final long serialVersionUID = 1L;

    public double x;
    public double y;

    // constructors
    public Vector2()
    {
        this(0, 0);
    }

    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    // identity
    public void set(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double magnitude()
    {
        return Math.sqrt(x * x + y * y);
    }

    // arithmetic
    public Vector2 add(Vector2 other)
    {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other)
    {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 multiply(double scalar)
    {
        return new Vector2(x * scalar, y * scalar);
    }

    public Vector2 divide(double scalar)
    {
        return new Vector2(this.x / scalar, this.y / scalar);
    }

    public double dot(Vector2 other)
    {
        return this.x * other.x + this.y * other.y;
    }

    public double cross(Vector2 other)
    {
        return this.x * other.y - this.y * other.x;
    }

    public Vector2 normalize()
    {
        double mag = magnitude();
        if (mag == 0) return new Vector2(0, 0);
        return new Vector2(x / mag, y / mag);
    }

}
