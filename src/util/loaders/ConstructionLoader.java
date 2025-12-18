package util.loaders;

import flight.construction.ConstructionManager;
import flight.construction.parts.Part;
import flight.construction.sections.Section;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;

public class ConstructionLoader
{
    public static void loadParts(String filePath)
    {
        try
        {
            JSONArray arr = readJsonArray(filePath);
            ArrayList<Part> parts = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);

                // example fields
                String name = obj.getString("name");
                double mass = obj.getDouble("mass");
                String socket = obj.optString("socket", "default");

                Part p = new Part(name, mass, socket); // make sure your constructor matches
                parts.add(p);
            }

            ConstructionManager.populateParts(parts);
            System.out.println("Loaded " + parts.size() + " parts.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadSections(String filePath)
    {
        try
        {
            JSONArray arr = readJsonArray(filePath);
            ArrayList<Section> sections = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);

                String name = obj.getString("name");
                double mass = obj.getDouble("mass");
                String type = obj.optString("type", "generic");

                Section s = new Section(name, mass, type);
                sections.add(s);
            }

            ConstructionManager.populateSections(sections);
            System.out.println("Loaded " + sections.size() + " sections.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static JSONArray readJsonArray(String path) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
        }

        return new JSONArray(sb.toString());
    }
}
