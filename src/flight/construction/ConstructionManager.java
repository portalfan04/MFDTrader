package flight.construction;

import flight.construction.parts.Part;
import flight.construction.sections.Section;

import java.util.ArrayList;

public class ConstructionManager
{

    private static ArrayList<Part> parts = new ArrayList<>();
    private static ArrayList<Section> sections = new ArrayList<>();
    private static ArrayList<Module> modules = new ArrayList<>();

    public static ArrayList<Part> getParts()
    {
        return parts;
    }
    public static ArrayList<Section> getSections()
    {
        return sections;
    }
    public static ArrayList<Module> getModules()
    {
        return modules;
    }
    public static void populateParts(ArrayList<Part> parts)
    {
        ConstructionManager.parts = parts;
    }
    public static void populateSections(ArrayList<Section> sections)
    {
        ConstructionManager.sections = sections;
    }
    public static void populateModules(ArrayList<Module> modules)
    {
        ConstructionManager.modules = modules;
    }
}
