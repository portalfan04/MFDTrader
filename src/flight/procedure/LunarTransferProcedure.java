package flight.procedure;

import flight.*;
import flight.step.*;
import simulation.Celestial;
import util.CONST;
import util.ConversionHelper;

import java.util.ArrayList;

public class LunarTransferProcedure extends DeltaVProcedure
{
    private Celestial planet;
    private Celestial moon;

    public LunarTransferProcedure(Celestial planet, Celestial moon, double maxDeltaV)
    {
        this.planet = planet;
        this.moon = moon;
        this.maxDeltaV = maxDeltaV;
    }

    @Override
    public ArrayList<FlightStep> generateSteps(Ship s, double cumulativeTime)
    {
        if (s.location != planet)
        {
            throw new RuntimeException("Ship not in orbit around the planet for lunar transfer!");
        }

        Celestial reference = planet.isBarycenter && planet.parent != null
                ? planet.parent
                : planet;

        double mu = CONST.G * reference.mass;

        double rPlanet = planet.radius + planet.getParkingAltitude();
        double rMoon = (moon.orbitalElements != null)
                ? moon.orbitalElements.a
                : moon.radius + moon.getParkingAltitude();

        double aTransfer = (rPlanet + rMoon) / 2.0;
        double vParking = Math.sqrt(mu / rPlanet);
        double vTransferDepart = Math.sqrt(mu * (2.0 / rPlanet - 1.0 / aTransfer));
        double vTransferArrive = Math.sqrt(mu * (2.0 / rMoon - 1.0 / aTransfer));
        double vMoonOrbit = Math.sqrt(mu / rMoon);

        double dvDepart = Math.abs(vTransferDepart - vParking);
        double dvArrive = Math.abs(vMoonOrbit - vTransferArrive);
        double totalDV = dvDepart + dvArrive;

        if (totalDV > maxDeltaV)
        {
            throw new RuntimeException("Lunar transfer exceeds allowed delta-V! Needed: " + totalDV + " m/s");
        }

        double tof = Math.PI * Math.sqrt(Math.pow(aTransfer, 3) / mu);

        ArrayList<FlightStep> steps = new ArrayList<>();
        steps.add(new StateStep("wnd"));
        steps.add(new WaitStep(0));
        steps.add(new BurnStep(dvDepart));
        steps.add(new InterceptStep(reference));
        steps.add(new StateStep("xfer"));
        steps.add(new WaitStep(tof));
        steps.add(new BurnStep(dvArrive));
        steps.add(new InterceptStep(moon));
        steps.add(new StateStep("orb"));

        s.target = moon;

        System.out.printf(
                "Lunar transfer computed:%nReference: %s%s%nFrom %s to %s%nΔv Depart = %.1f m/s, Δv Arrive = %.1f m/s%nTotal Δv = %.1f m/s%nTime of Flight = %.2f days%n",
                reference.name,
                (reference.isBarycenter ? " (Barycenter)" : ""),
                planet.name,
                moon.name,
                dvDepart,
                dvArrive,
                totalDV,
                ConversionHelper.secondToDay(tof)
        );

        return steps;
    }

    @Override
    public String getTimeLineDescription()
    {
        String planetName = (planet != null ? planet.name : "Unknown Planet");
        String moonName = (moon != null ? moon.name : "Unknown Moon");
        return String.format("Lunar Transfer: %s → %s (Δv ≤ %.1f m/s)", planetName, moonName, maxDeltaV);
    }

    @Override
    public String getTypeName()
    {
        return "LunarTransfer";
    }

    @Override
    public Celestial getProcedureOrigin()
    {
        return planet;
    }

    @Override
    public Celestial getProcedureDestination()
    {
        return moon;
    }

    public Celestial getPlanet()
    {
        return planet;
    }

    public Celestial getMoon()
    {
        return moon;
    }

    public void setPlanet(Celestial planet)
    {
        this.planet = planet;
    }

    public void setMoon(Celestial moon)
    {
        this.moon = moon;
    }

}
