package util.loaders;

import simulation.Celestial;
import simulation.SolarSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads all solar systems from a directory of JSON files.
 */
public class DirectoryLoader {

    /**
     * Loads all JSON files from the given directory path.
     * Each file represents one Simulation.SolarSystem.
     *
     * @param dirPath The directory containing solar system JSON files.
     * @return List of Simulation.SolarSystem objects (one per JSON file).
     * @throws IOException
     */
    public static List<SolarSystem> loadFromDirectory(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Directory does not exist: " + dirPath);
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) return new ArrayList<>();

        List<SolarSystem> solarSystems = new ArrayList<>();
        for (File file : files) {
            Celestial rootCelestial = SolarSystemLoader.loadFromFile(file.getAbsolutePath());
            SolarSystem system = new SolarSystem(rootCelestial); // each file = one solar system
            solarSystems.add(system);
        }

        return solarSystems;
    }

    /**
     * Default loader: looks in "resources/solarsystems" relative to project.
     */
    public static List<SolarSystem> loadDefaultDirectory() throws IOException {
        return loadFromDirectory("resources/systems/main");
    }
}
