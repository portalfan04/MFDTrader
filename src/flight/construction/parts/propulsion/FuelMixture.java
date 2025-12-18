package flight.construction.parts.propulsion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class FuelMixture
{
    private Map<String, Double> components = new LinkedHashMap<>();

    public Map<String, Double> getComponents()
    {
        return components;
    }

    // Adds a fuel component; ratio is mass fraction (un-normalized)
    public void add(String resource, double ratio)
    {
        components.merge(resource, ratio, Double::sum);
    }

    // Normalize so total fraction = 1.0
    public void normalize()
    {
        double total = components.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0)
        {
            components.replaceAll((r, v) -> v / total);
        }
    }

    // Convenience: get as a list of components for iteration
    public List<Component> getComponentList()
    {
        List<Component> list = new ArrayList<>();
        for (Map.Entry<String, Double> e : components.entrySet())
        {
            list.add(new Component(e.getKey(), e.getValue()));
        }
        return list;
    }

    // Inner class for ease of iteration
    public static class Component
    {
        private final String resourceId;
        private final double massFraction;

        public Component(String resourceId, double massFraction)
        {
            this.resourceId = resourceId;
            this.massFraction = massFraction;
        }

        public String resourceId() { return resourceId; }
        public double massFraction() { return massFraction; }
    }
}
