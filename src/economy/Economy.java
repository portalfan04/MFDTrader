package economy;

import simulation.Celestial;
import simulation.Organisation;

public class Economy
{
    private double totalOrganicPopulation; // total number of working humans alive on this planet.

    private Celestial planet;
    private Organisation organisation;
    // private Site location;
    //private ArrayList<Construction> constructions;

    public Economy(Celestial planet, Organisation organisation)
    {
        this.planet = planet;
        this.organisation = organisation;
    }
}
