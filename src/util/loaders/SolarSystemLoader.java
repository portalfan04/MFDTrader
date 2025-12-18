package util.loaders;

import org.json.JSONArray;
import org.json.JSONObject;

import simulation.Celestial;
import simulation.CelestialType;
import simulation.OrbitalElements;
import util.Vector2;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Loads Simulation.Celestial hierarchy from JSON.
 * Supports parentless (root) bodies and simulation.OrbitalElements for orbiters.
 */
public class SolarSystemLoader
{
    public static Celestial loadFromFile(String path) throws IOException
    {
        String content = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject rootJson = new JSONObject(content);
        return parseCelestial(rootJson, null);
    }

    private static Celestial parseCelestial(JSONObject json, Celestial parent)
    {
        String name = json.getString("name");
        double mass = json.getDouble("mass");
        double radius = json.getDouble("radius");
        String typeStr = json.optString("type", "TERRESTRIAL");
        String colorStr = json.optString("color", "#FFFFFF");
        boolean isBarycenter = json.optBoolean("isBarycenter", false);

        Celestial body = new Celestial(name);
        body.mass = mass;
        body.radius = radius;
        body.color = Color.decode(colorStr);
        body.parent = parent;
        body.type = parseType(typeStr);
        body.isBarycenter = isBarycenter;


        // Orbital elements (if orbiting a parent)
        JSONObject orbJson = json.optJSONObject("orbitalElements");
        if (orbJson != null && parent != null)
        {
            double a = orbJson.optDouble("a", 0);
            double e = orbJson.optDouble("e", 0);
            double argPeriapsis = orbJson.optDouble("argPeriapsis", 0);
            double meanAnomalyAtEpoch = orbJson.optDouble("meanAnomalyAtEpoch", 0);

            body.orbitalElements = new OrbitalElements(
                    a, e, argPeriapsis, meanAnomalyAtEpoch, parent.mass
            );
        }

        // Root static map position (optional)
        JSONObject posJson = json.optJSONObject("position");
        if (parent == null)
        {
            if (posJson != null)
            {
                double x = posJson.optDouble("x", 0);
                double y = posJson.optDouble("y", 0);
                body.mapPosition = new Vector2(x, y);
            }
            else
            {
                body.mapPosition = new Vector2(0, 0);
            }
        }

        // Children (moons, planets, etc.)
        JSONArray children = json.optJSONArray("children");
        if (children != null)
        {
            for (int i = 0; i < children.length(); i++)
            {
                JSONObject childJson = children.getJSONObject(i);
                Celestial child = parseCelestial(childJson, body);
                body.children.add(child);
            }
        }

        return body;
    }

    private static CelestialType parseType(String typeStr)
    {
        try
        {
            return CelestialType.valueOf(typeStr.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Unknown celestial type: " + typeStr + ", defaulting to TERRESTRIAL");
            return CelestialType.TERRESTRIAL;
        }
    }
}
