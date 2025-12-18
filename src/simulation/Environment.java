package simulation;

import java.util.HashMap;

public class Environment
{
    private Celestial planet;

    private HashMap<String, Double> atmosphericComposition = new HashMap<>();
    private double temperature; // measured in Kelvin
    private double surfacePressure; // measured in Pascals
    private double rotationalPeriod; // measured in seconds

    public double getSurfacePressure()
    {
        return surfacePressure;
    }

    public void setSurfacePressure(double surfacePressure)
    {
        this.surfacePressure = surfacePressure;
    }

    public double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(double temperature)
    {
        this.temperature = temperature;
    }

    public HashMap<String, Double> getAtmosphericComposition()
    {
        return atmosphericComposition;
    }

    public void setAtmosphericComposition(HashMap<String, Double> atmosphericComposition)
    {
        this.atmosphericComposition = atmosphericComposition;
    }

    public Celestial getPlanet()
    {
        return planet;
    }

    public void setPlanet(Celestial planet)
    {
        this.planet = planet;
    }

    public double getRotationalPeriod()
    {
        return rotationalPeriod;
    }

    public void setRotationalPeriod(double rotationalPeriod)
    {
        this.rotationalPeriod = rotationalPeriod;
    }
    public double getPartialPressure(String gas)
    {
        if (atmosphericComposition == null) return 0.0;
        Double fraction = atmosphericComposition.get(gas);
        if (fraction == null) return 0.0;
        return fraction * surfacePressure;
    }

    public boolean isValidAtmosphere()
    {
        if (atmosphericComposition == null || atmosphericComposition.isEmpty())
        {
            return false;
        }

        double sum = 0.0;
        for (double v : atmosphericComposition.values())
        {
            if (v < 0.0) return false;
            sum += v;
        }

        return Math.abs(sum - 1.0) < 0.001;
    }
    public double getDayLengthHours()
    {
        return rotationalPeriod / 3600.0;
    }

    public double getSurfacePressureATM()
    {
        return surfacePressure / 101325.0;
    }

    public double getTemperatureCelsius()
    {
        return temperature - 273.15;
    }
}
