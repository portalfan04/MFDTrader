package economy.resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceDatabase
{
    private static final Map<String, ResourceDefinition> resources = new HashMap<>();

    public static void register(ResourceDefinition def)
    {
        resources.put(def.getId(), def);
    }

    public static ResourceDefinition get(String id)
    {
        ResourceDefinition def = resources.get(id);

        if (def == null)
        {
            throw new IllegalArgumentException("Unknown resource: " + id);
        }

        return def;
    }

    public static boolean exists(String id)
    {
        return resources.containsKey(id);
    }
}
