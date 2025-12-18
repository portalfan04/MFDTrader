package flight;

import economy.resource.ResourceContainer;
import flight.construction.IMass;
import flight.construction.parts.ContainerPart;
import flight.construction.parts.Part;
import flight.construction.parts.propulsion.Engine;
import flight.construction.parts.propulsion.FuelMixture;
import flight.construction.sections.Section;
import flight.construction.superstructures.*;
import flight.procedure.FlightProcedure;
import flight.step.FlightStep;
import simulation.Celestial;
import simulation.Organisation;
import util.ConversionHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Ship implements IMass
{

    private String name;
    public Organisation owner;

    // physical characteristics

    private Truss truss;
    private PayloadShipModule payloadModule;
    private ServiceShipModule serviceModule;
    private EngineShipModule engineBlock;
    private Map<String, Section> socketsBySectionName = new HashMap<>();

    // some object to store current crew...

    // flight logic
    private ArrayDeque<FlightStep> flightQueue = new ArrayDeque<>();
    private FlightPlan plan;
    public double timeToWait;

    // flight states
    public ShipState stepState;
    public FlightState flightState;
    public ShipTaskState taskState;

    // journey locations
    public Celestial location;
    public Celestial target;
    public FlightProcedure currentProcedure;
    private double currentSimTime = 0; // keeps track of total sim time for step scheduling

    // TODO replace this with actual fuelling
    private String propellantType = "default";

    public Ship(String name, Celestial location, Organisation owner)
    {
        this.name = name;
        this.location = location;
        this.owner = owner;
        owner.addShip(this);
        this.currentProcedure = null;
        this.stepState = ShipState.WAITING;
        this.flightState = FlightState.ORBITING;
        this.taskState = ShipTaskState.OFF;

        // placeholders for now
        this.engineBlock = new EngineShipModule();
        this.payloadModule = new PayloadShipModule();
        this.serviceModule = new ServiceShipModule();
        this.truss = new Truss();
    }

    // -----------------
    // flight operations
    // -----------------
    public void update(double dt)
    {
        // Don’t do anything if OFF
        if (taskState == ShipTaskState.OFF)
            return;

        if (stepState != ShipState.WAITING)
            return;

        currentSimTime += dt;
        timeToWait -= dt;

        while (timeToWait <= 0)
        {
            // When AUTO, refill only from plan
            if (taskState == ShipTaskState.AUTO)
            {
                refillQueueIfNeeded();
            }

            if (flightQueue.isEmpty())
            {
                stepState = ShipState.INACTIVE;
                return;
            }

            FlightStep step = flightQueue.poll();
            if (step == null)
                break;

            step.execute(this);
            timeToWait = step.getWait();
        }
    }

    public void setFlightPlan(FlightPlan fp)
    {
        this.plan = fp;
        if (fp == null)
        {
            return;
        }
        if (!fp.getOrigin().equals(this.location))
        {
            System.out.println("PlanFault on ship " + name + ": origin does not match current location!");
            stepState = ShipState.PLAN_FAULT;
            return;
        }
        if (taskState == ShipTaskState.AUTO)
        {
            refillQueueIfNeeded(); // pull in first procedure right away
        }
    }

    public FlightPlan getFlightPlan()
    {
        return this.plan;
    }

    private void refillQueueIfNeeded()
    {
        if (plan == null) return;
        if (flightQueue.isEmpty())
        {
            FlightProcedure proc = plan.getNextProcedure();
            currentProcedure = proc;
            if (proc == null) return; // done
            flightQueue.addAll(proc.generateSteps(this, currentSimTime));
        }
    }

    public void burn(double dv)
    {
        if (dv <= getDeltaV())
        {

        System.out.println(name + " burned " + dv + " m/s. Remaining Δv: " + getDeltaV());
        }
        else
        {
            System.out.println("PlanFault on ship " + name + ": insufficient deltaV!");
            stepState = ShipState.PLAN_FAULT;
        }
    }

    public void setTaskState(ShipTaskState newState)
    {
        // If nothing changes, skip
        if (this.taskState == newState)
            return;

        // Handle mode transitions
        switch (newState)
        {
            case OFF ->
            {
                // OFF: completely clear queue, ignore all new procedures
                flightQueue.clear();
                plan = null;
                stepState = ShipState.INACTIVE;
                System.out.println(name + " switched OFF — all queued steps cleared.");
            }

            case MANUAL ->
            {
                // MANUAL: retain whatever’s in queue, allow manual procedures only
                System.out.println(name + " switched to MANUAL mode.");
                if (taskState == ShipTaskState.OFF)
                {
                    stepState = ShipState.WAITING;
                }
            }

            case AUTO ->
            {
                // AUTO: complete remaining queue, then follow the plan
                System.out.println(name + " switched to AUTO mode.");

                // If there’s still queued work, finish it first
                if (!flightQueue.isEmpty())
                {
                    System.out.println(name + " finishing remaining steps before starting flight plan...");
                    // let update() naturally finish these steps
                }
                else
                {
                    // Queue empty? Begin flight plan
                    refillQueueIfNeeded();
                }

                stepState = ShipState.WAITING;
            }
        }

        this.taskState = newState;
    }

    // -----------------------------
    // string representation methods
    // -----------------------------
    public void setStateByString(String state)
    {
        switch (state)
        {
            case "orb":
            {
                this.flightState = FlightState.ORBITING;
                break;
            }
            case "dock":
            {
                this.flightState = FlightState.DOCKED;
                break;
            }
            case "wnd":
            {
                this.flightState = FlightState.WAIT_WINDOW;
                break;
            }
            case "xfer":
            {
                this.flightState = FlightState.XFER;
                break;
            }
        }
    }

    public String getShortStatus()
    {
        switch (flightState)
        {
            case FlightState.DOCKED:
            {
                return "Docked at "; // + dockedBerth
            }
            case FlightState.ORBITING:
            {
                return "Orbiting " + location;
            }
            case FlightState.XFER:
            {
                return "En route to " + target + ", arrival in " + Math.round(ConversionHelper.secondToDay(timeToWait)) + " days";
            }
            case FlightState.WAIT_WINDOW:
            {
                return "Orbiting " + location + ", burning in " + Math.round(ConversionHelper.secondToDay(timeToWait)) + " days";
            }
            default:
            {
                return "???";
            }
        }
    }

    public String getProcedureStatus()
    {
        if (currentProcedure == null)
        {
            return "No procedure in progress";
        }
        switch (currentProcedure.getTypeName())
        {
            case "Wait":
            {
                return "Waiting at " + location.name + " for " + Math.round(ConversionHelper.secondToDay(timeToWait)) + " days";
            }
            case "Print":
            {
                return "Printing to console";
            }
            case "HohmannTransfer", "LunarTransfer":
            {
                return "Transferring from " + location.name + " to " + target.name;
            }
            case "LunarReturn":
            {
                return "Returning from " + target.name + " to " + location.name;
            }
            default:
            {
                return "Unknown procedure";
            }
        }
    }

    // -----------------
    // getters & setters
    // -----------------
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    // ------------
    // deltav, mass
    // ------------

    /**
     * Returns the wet (full) mass of a ship in kg
     * @return wet mass (kg)
     */
    @Override
    public double getMass()
    {
        return payloadModule.getMass() + serviceModule.getMass() + engineBlock.getMass();
    }

    /**
     * Returns the dry mass of a ship in kg
     * Finds the wet mass, and subtracts the mass of the propellant
     * @return dry mass (kg)
     */
    public double getDryMass()
    {
        return getMass() - getPropellantMass();
    }
    public double getPropellantMass()
    {
        double total = 0.0;

        if (engineBlock == null)
        {
            return total;
        }

        FuelMixture mix = engineBlock.getFuelMixture();

        if (mix == null || mix.getComponents().isEmpty())
        {
            return total;
        }


        for (var entry : mix.getComponents().entrySet())
        {
            String resource = entry.getKey();
            double ratio = entry.getValue();

            // Multiply by actual resource mass?
            // No — we want TOTAL mass of all propellants that exist regardless of ratio.
            total += getResourceMass(resource);
        }

        return total;
    }

    public double getResourceMass(String resourceID)
    {
        double total = 0.0;

        for (Part part : getParts())
        {
            if (part instanceof ContainerPart tank)
            {
                total += tank.getResourceMass(resourceID);
            }
        }

        return total;
    }


    public double getDeltaV()
    {
        FuelMixture mix = engineBlock.getFuelMixture();

        if (mix == null || mix.getComponentList().isEmpty())
        {
            return 0.0;
        }

        // Step 1: Determine limiting mix units
        double limitingUnits = Double.POSITIVE_INFINITY;

        for (FuelMixture.Component comp : mix.getComponentList())
        {
            double availableMass = getResourceMass(comp.resourceId());
            double units = availableMass / comp.massFraction();

            if (units < limitingUnits)
            {
                limitingUnits = units;
            }
        }

        // Step 2: If zero fuel left, no Δv
        if (limitingUnits <= 0 || Double.isInfinite(limitingUnits))
        {
            return 0.0;
        }

        // Total burnable propellant mass
        double totalMixtureMass = 0.0;

        for (FuelMixture.Component comp : mix.getComponentList())
        {
            totalMixtureMass += comp.massFraction();
        }

        double usablePropellant = limitingUnits * totalMixtureMass;

        // Step 3: Apply rocket equation
        double dryMass = getDryMass();
        double wetMass = dryMass + usablePropellant;

        double isp = engineBlock.getSpecificImpulse();

        return isp * Math.log(wetMass / dryMass);
    }

    public double drainPropellant(double totalMassToRemove)
    {
        // grab mixture from active engines
        FuelMixture mix = engineBlock.getFuelMixture();
        if (mix == null || mix.getComponentList().isEmpty())
        {
            return 0.0;
        }

        ArrayList<Part> parts = this.getParts();

        double totalRemoved = 0.0;

        for (FuelMixture.Component comp : mix.getComponentList())
        {
            String resource = comp.resourceId();
            double fraction = comp.massFraction();

            double desired = totalMassToRemove * fraction;

            double removed = drainSingleResource(parts, resource, desired);

            totalRemoved += removed;
        }

        return totalRemoved;
    }

    private double drainSingleResource(ArrayList<Part> parts, String resourceId, double massToDrain)
    {
        double remaining = massToDrain;
        double removedTotal = 0.0;

        for (Part part : parts)
        {
            if (remaining <= 0)
            {
                break;
            }

            if (part instanceof ContainerPart tankPart)
            {
                if (tankPart.hasResource(resourceId))
                {
                    ResourceContainer rc = tankPart.getContainers()
                            .stream()
                            .filter(c -> c.getResourceID().equals(resourceId))
                            .findFirst()
                            .orElse(null);

                    if (rc != null)
                    {
                        double available = rc.getCurrentMass();
                        double toRemove = Math.min(available, remaining);

                        // reduce tank content
                        rc.add(-toRemove);

                        remaining -= toRemove;
                        removedTotal += toRemove;
                    }
                }
            }
        }

        return removedTotal;
    }



    // ------------------
    // physical structure
    // ------------------
    public ArrayList<Part> getParts()
    {
        ArrayList<Part> parts = new ArrayList<>(engineBlock.getAllEngines());
        parts.addAll(serviceModule.getParts());
        parts.addAll(payloadModule.getParts());
        return parts;
    }
    public void addEngine(Engine engine)
    {
        engineBlock.addEngine(engine);
        engineBlock.enableEngine(engine);
    }
    public void addPartToSection(Part p, Section s)
    {
        ShipModule m = s.getModule();
        m.addPartToSection(p, s);
    }

    public void newSection(String name, double mass, String socketName, ShipModule module)
    {

    }

    public void newSection(String name, double mass, String socketName, String module)
    {
        Section section = new Section(name, mass, socketName);
        switch (module)
        {
            case "payload":
                payloadModule.addSection(section);
                section.setModule(payloadModule);
            case "service":
                serviceModule.addSection(section);
                section.setModule(serviceModule);
        }
        socketsBySectionName.put(socketName, section);
    }
    public void addPartBasedOnSockets(ContainerPart methaloxTank)
    {
        String socket = methaloxTank.getSocketName();
        if (socketsBySectionName.containsKey(socket))
        {
            Section section = socketsBySectionName.get(socket);
            addPartToSection(methaloxTank, section);
        }
        else
        {
            System.out.println("Warning: no section found for socket " + socket);
        }
    }
}