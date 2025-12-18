package util;

import flight.procedure.*;
import flight.*;
import simulation.Celestial;
import simulation.Universe;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanIO
{
    public static void savePlansToFile(List<FlightPlan> plans, File file) throws IOException
    {
        JSONArray root = new JSONArray();

        for (FlightPlan fp : plans)
        {
            JSONObject planObj = new JSONObject();
            planObj.put("name", fp.getName());
            planObj.put("repeat", fp.isRepeat());
            planObj.put("description", fp.getDescription() != null ? fp.getDescription() : "");

            JSONArray procs = new JSONArray();
            for (FlightProcedure proc : fp.getProcedures())
            {
                JSONObject p = new JSONObject();
                p.put("type", proc.getClass().getSimpleName());

                if (proc instanceof HohmannTransferProcedure htp)
                {
                    p.put("origin", htp.getOrigin() != null ? htp.getOrigin().name : JSONObject.NULL);
                    p.put("destination", htp.getDestination() != null ? htp.getDestination().name : JSONObject.NULL);
                    p.put("maxDeltaV", htp.getMaxDeltaV());
                    p.put("roughTime", htp.getRoughTime());
                }
                else if (proc instanceof LunarTransferProcedure ltp)
                {
                    p.put("planet", ltp.getPlanet() != null ? ltp.getPlanet().name : JSONObject.NULL);
                    p.put("moon", ltp.getMoon() != null ? ltp.getMoon().name : JSONObject.NULL);
                    p.put("maxDeltaV", ltp.getMaxDeltaV());
                }
                else if (proc instanceof LunarReturnProcedure lrp)
                {
                    p.put("planet", lrp.getPlanet() != null ? lrp.getPlanet().name : JSONObject.NULL);
                    p.put("moon", lrp.getMoon() != null ? lrp.getMoon().name : JSONObject.NULL);
                    p.put("maxDeltaV", lrp.getMaxDeltaV());
                }
                else if (proc instanceof WaitProcedure wp)
                {
                    p.put("duration", wp.getDuration());
                }
                else if (proc instanceof PrintProcedure pp)
                {
                    p.put("message", pp.getMessage());
                }

                procs.put(p);
            }

            planObj.put("procedures", procs);
            root.put(planObj);
        }

        Files.writeString(file.toPath(), root.toString(2)); // pretty print w/ indent
    }

    public static List<FlightPlan> loadPlansFromFile(File file, Universe universe) throws IOException
    {
        String content = Files.readString(file.toPath());
        JSONArray root = new JSONArray(content);
        List<FlightPlan> plans = new ArrayList<>();

        for (int i = 0; i < root.length(); i++)
        {
            JSONObject planObj = root.getJSONObject(i);
            FlightPlan fp = new FlightPlan(planObj.getBoolean("repeat"));
            fp.setName(planObj.getString("name"));
            fp.setDescription(planObj.optString("description", ""));

            JSONArray procs = planObj.getJSONArray("procedures");
            for (int j = 0; j < procs.length(); j++)
            {
                JSONObject p = procs.getJSONObject(j);
                String type = p.getString("type");

                FlightProcedure proc = switch (type)
                {
                    case "HohmannTransferProcedure" ->
                    {
                        Celestial origin = universe.findCelestialByName(p.optString("origin", null));
                        Celestial dest = universe.findCelestialByName(p.optString("destination", null));
                        double dv = p.optDouble("maxDeltaV", 0);
                        double time = p.optDouble("roughTime", 0);
                        yield new HohmannTransferProcedure(origin, dest, dv, time);
                    }
                    case "LunarTransferProcedure" ->
                    {
                        Celestial planet = universe.findCelestialByName(p.optString("planet", null));
                        Celestial moon = universe.findCelestialByName(p.optString("moon", null));
                        double dv = p.optDouble("maxDeltaV", 0);
                        yield new LunarTransferProcedure(planet, moon, dv);
                    }
                    case "LunarReturnProcedure" ->
                    {
                        Celestial moon = universe.findCelestialByName(p.optString("moon", null));
                        double dv = p.optDouble("maxDeltaV", 0);
                        yield new LunarReturnProcedure(moon, dv);
                    }
                    case "WaitProcedure" ->
                    {
                        double dur = p.optDouble("duration", 0);
                        yield new WaitProcedure(dur);
                    }
                    case "PrintProcedure" ->
                    {
                        String msg = p.optString("message", "");
                        yield new PrintProcedure(msg);
                    }
                    default -> null;
                };

                if (proc != null) fp.addProcedure(proc);
            }

            plans.add(fp);
        }

        return plans;
    }
}
