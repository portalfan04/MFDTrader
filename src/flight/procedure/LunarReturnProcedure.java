package flight.procedure;

import flight.*;
import flight.step.*;
import simulation.Celestial;
import util.CONST;
import util.ConversionHelper;

import java.util.ArrayList;

/**
 * Lunar return procedure: ship departs a moon and captures into orbit around the parent planet.
 */
public class LunarReturnProcedure extends DeltaVProcedure
{
    private Celestial planet;
    private Celestial moon;

    public LunarReturnProcedure(Celestial moon, double maxDeltaV)
    {
        this.moon = moon;
        this.maxDeltaV = maxDeltaV;

        if(moon != null)
        {
            this.planet = moon.parent;
        }
    }

    @Override
    public ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime)
    {
        if(s.location != moon)
        {
            throw new RuntimeException("Ship not in orbit around the moon for lunar return!");
        }

        // Orbital radii (planet-centered)
        double rMoon = moon.orbitalElements.a;
        double rPlanet = planet.getParkingAltitude(); // use your dynamic parking altitude

        double mu = CONST.G * planet.mass;

        // Hohmann-style return transfer
        double aTransfer = (rMoon + rPlanet) / 2.0;
        double vMoonOrbit = Math.sqrt(mu / rMoon);
        double vTransferDepart = Math.sqrt(mu * (2.0 / rMoon - 1.0 / aTransfer));
        double vTransferArrive = Math.sqrt(mu * (2.0 / rPlanet - 1.0 / aTransfer));
        double vPlanetOrbit = Math.sqrt(mu / rPlanet);

        double dvDepart = Math.abs(vTransferDepart - vMoonOrbit);
        double dvArrive = Math.abs(vPlanetOrbit - vTransferArrive);
        double totalDV = dvDepart + dvArrive;

        if(totalDV > maxDeltaV)
        {
            throw new RuntimeException("Lunar return exceeds allowed delta-V!");
        }

        // Time of flight (half the orbital period of the transfer ellipse)
        double tof = Math.PI * Math.sqrt(Math.pow(aTransfer, 3) / mu);

        ArrayList<FlightStep> steps = new ArrayList<>();

        // Step 1: Departure burn
        steps.add(new PrintStep("Departing moon " + moon.name));
        steps.add(new BurnStep(dvDepart));
        steps.add(new InterceptStep(moon.parent));
        steps.add(new StateStep("xfer"));

        // Step 2: Coast to planet
        steps.add(new WaitStep(tof));

        // Step 3: Capture burn into planet parking orbit
        steps.add(new BurnStep(dvArrive));
        steps.add(new InterceptStep(planet));
        steps.add(new StateStep("orb"));

        s.target = planet;

        System.out.println("Lunar return computed:");
        System.out.println("From moon " + moon.name + " to planet " + planet.name);
        System.out.println("Departure burn: " + dvDepart + " m/s, arrival burn: " + dvArrive + " m/s");
        System.out.println("Total Δv: " + totalDV + " m/s, time of flight: " + ConversionHelper.secondToDay(tof) + " days");

        return steps;
    }

    @Override
    public String getTimeLineDescription()
    {
        String planetName = (planet != null ? planet.name : "Unknown Planet");
        String moonName = (moon != null ? moon.name : "Unknown Moon");
        return String.format("Lunar Return: %s → %s (Δv ≤ %.1f m/s)", planetName, moonName, maxDeltaV);
    }

    @Override
    public String getTypeName()
    {
        return "LunarReturn";
    }

    @Override
    public Celestial getProcedureOrigin()
    {
        return moon;
    }

    @Override
    public Celestial getProcedureDestination()
    {
        return planet;
    }

    public void setPlanet(Celestial planet)
    {
        this.planet = planet;
    }
    public void setMoon(Celestial moon)
    {
        this.moon = moon;
    }

    public Celestial getPlanet()
    {
        return planet;
    }

    public Celestial getMoon()
    {
        return moon;
    }

}
