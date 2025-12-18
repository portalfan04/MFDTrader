import economy.resource.ResourceContainer;
import flight.*;
import flight.construction.parts.ContainerPart;
import flight.construction.parts.propulsion.Engine;
import flight.construction.parts.propulsion.FuelMixture;
import simulation.Celestial;
import simulation.Organisation;
import simulation.SolarSystem;
import simulation.Universe;
import util.loaders.DirectoryLoader;
import util.save.SaveManager;

import java.io.IOException;
import java.util.List;

public class Main
{
    public static boolean paused;

    public static void main(String[] args) throws IOException
    {
        // Try to load existing universe
        Universe universe = SaveManager.loadUniverse("saves/universe.dat");

        if (universe == null)
        {
            System.out.println("No save found, loadin’ universe from JSON files…");

            List<SolarSystem> solarSystems = DirectoryLoader.loadDefaultDirectory();
            if (solarSystems.isEmpty())
            {
                return;
            }

            universe = new Universe();
            for (SolarSystem ss : solarSystems)
            {
                universe.addSolarSystem(ss);
            }

            // Save immediately so next launch loads the serialized version
            SaveManager.saveUniverse(universe, "saves/universe.dat");
        }
        else
        {
            System.out.println("Loaded universe from save!");
        }

        GUIManager guiManager = new GUIManager(universe);
        paused = true;

        // One warm-up update
        universe.update(1);

        // Find Earth (unchanged)
        Celestial earth = null;
        for (SolarSystem ss : universe.getSolarSystems())
        {
            if (ss.getRoot().name.equals("Sun"))
            {
                for (Celestial planet : ss.getAllBodies())
                {
                    if (planet.name.equals("Earth"))
                    {
                        earth = planet;
                    }
                }
            }
        }

        final Universe uni = universe;
        final GUIManager gui = guiManager;

        new javax.swing.Timer(33, e ->
        {
            if (!paused)
            {
                uni.tick();
            }
            gui.render();
        }).start();


        // The stuff below only runs when the save is first created.
        // If you want organisations/ships also saved, we can add them to Universe.
        Organisation sinSpaceEngineering = new Organisation("Sin Space Engineering", universe, earth);
        universe.addOrganisation(sinSpaceEngineering);

        Ship vonBraun = new Ship("Von Braun", earth, sinSpaceEngineering);

        FuelMixture methalox = new FuelMixture();

        methalox.add("LCH4", 1.0);
        methalox.add("LOX", 3.6);
        methalox.normalize();

        Engine engine = new Engine("methalox", 1500, methalox, 2000, 380);

        ContainerPart methaloxTank = new ContainerPart(500);

        for (FuelMixture.Component comp : methalox.getComponentList())
        {
            double mass = 3000 * comp.massFraction();
            ResourceContainer rs = new ResourceContainer(comp.resourceId(), mass);
            methaloxTank.addContainer(rs);
        }

        methaloxTank.fillAll();

        vonBraun.newSection("tank", 0, "tank", "service");
        vonBraun.addPartBasedOnSockets(methaloxTank);
        vonBraun.addEngine(engine);
    }
}
