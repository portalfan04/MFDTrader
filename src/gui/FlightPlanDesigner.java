package gui;

import flight.*;
import flight.procedure.*;
import simulation.Celestial;
import simulation.Universe;
import util.ConversionHelper;
import util.FlightPlanIO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Main window for designing flight plans
public class FlightPlanDesigner extends JFrame
{
    private DefaultListModel<FlightProcedure> planModel = new DefaultListModel<>();
    private JList<FlightProcedure> planList;
    private JCheckBox repeatCheck;
    private JTextField planNameField;
    private JTextArea planDescriptionArea;
    private Universe universe;
    private List<Celestial> allCelestials;

    private Celestial startingLocation = null;        // starting planet for the flight plan
    private JLabel startingPlanetLabel;
    private Celestial currentLocation = null;       // current location for next procedure
    private JLabel currentLocationLabel;          // shows current plan location



    public FlightPlanDesigner(Universe universe, List<Celestial> celestials)
    {
        super("Flight Plan Designer");
        this.universe = universe;
        this.allCelestials = celestials;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        setupNamePanel();
        setupToolboxAndPlanList();
        setupButtons();
        setupBottomPanel();

        setVisible(true);
    }

    // ------------------------------
    // Panel for flight plan name
    // ------------------------------
    private void setupNamePanel()
    {
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topRow.add(new JLabel("Flight Plan Name:"));
        planNameField = new JTextField("Untitled Flight Plan", 20);
        topRow.add(planNameField);
        namePanel.add(topRow);

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("Plan Description"));
        planDescriptionArea = new JTextArea(3, 25);
        planDescriptionArea.setLineWrap(true);
        planDescriptionArea.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(planDescriptionArea), BorderLayout.CENTER);
        namePanel.add(descPanel);

        add(namePanel, BorderLayout.NORTH);
    }

    private void updateLabels()
    {
        // Starting planet: first procedure with an origin/planet
        Celestial start = null;
        for (int i = 0; i < planModel.size(); i++)
        {
            FlightProcedure p = planModel.get(i);
            if (p instanceof HohmannTransferProcedure htp && htp.getOrigin() != null)
            {
                start = htp.getOrigin();
                break;
            }
            else if (p instanceof LunarTransferProcedure ltp && ltp.getPlanet() != null)
            {
                start = ltp.getPlanet();
                break;
            }
            else if (p instanceof LunarReturnProcedure lrp && lrp.getPlanet() != null)
            {
                start = lrp.getPlanet();
                break;
            }
        }
        startingPlanetLabel.setText("Start: " + (start != null ? start.name : "None"));

        // Current procedure location: last added procedure with origin/destination
        Celestial current = null;
        for (int i = planModel.size() - 1; i >= 0; i--)
        {
            FlightProcedure p = planModel.get(i);
            if (p instanceof HohmannTransferProcedure htp && htp.getDestination() != null)
            {
                current = htp.getDestination();
                break;
            }
            else if (p instanceof LunarTransferProcedure ltp && ltp.getMoon() != null)
            {
                current = ltp.getMoon();
                break;
            }
            else if (p instanceof LunarReturnProcedure lrp && lrp.getPlanet() != null)
            {
                current = lrp.getPlanet();
                break;
            }
        }
        currentLocationLabel.setText("Current: " + (current != null ? current.name : "None"));
    }
    // ------------------------------
    // Current location label
    // ------------------------------
    private void setupLocationLabel()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        startingPlanetLabel = new JLabel("Starting Planet: None");
        panel.add(startingPlanetLabel);

        currentLocationLabel = new JLabel("Current Plan Location: None");
        panel.add(currentLocationLabel);

        add(panel, BorderLayout.SOUTH);
    }
    private void updatePlanetLabels()
    {
        setTerminators();

        updateStartingPlanetLabel();
        updateCurrentLocationLabel();
    }
    private void updateStartingPlanetLabel()
    {
        String txt = (startingLocation != null) ? startingLocation.toString() : "None";
        startingPlanetLabel.setText("Starting Planet: " + txt);
    }
    private void updateCurrentLocationLabel()
    {
        String txt = (currentLocation != null) ? currentLocation.toString() : "None";
        currentLocationLabel.setText("Current Plan Location: " + txt);
    }
    public void setTerminators()
    {
        getAndSetOrigin();
        getAndSetDestination();
    }
    public Celestial getAndSetOrigin()
    {
        startingLocation = null;
        if (planModel.isEmpty()) return null;
        for (int i = 0; i < planModel.size(); i++)
        {
            FlightProcedure proc = planModel.get(i);
            Celestial origin = proc.getProcedureOrigin();
            if (origin != null)
            {
                startingLocation = origin;
                return origin;
            }
        }
        return null;
    }
    public Celestial getAndSetDestination()
    {
        currentLocation = null;
        for (int i = planModel.size() - 1; i >= 0; i--)
        {
            FlightProcedure proc = planModel.get(i);
            Celestial dest = proc.getProcedureDestination();
            if (dest != null)
            {
                currentLocation = dest;
                return dest;
            }
        }
        return null;
    }
    private void updateCurrentLocationAfterDrop(Celestial newOrigin)
    {
        if (startingLocation == null && newOrigin != null)
        {
            startingLocation = newOrigin;
            updateStartingPlanetLabel();
        }
        currentLocation = newOrigin;
        updateCurrentLocationLabel();
    }

    // ------------------------------
    // Toolbox list and plan timeline
    // ------------------------------
    private void setupToolboxAndPlanList()
    {
        // Toolbox contains templates of procedures
        DefaultListModel<FlightProcedure> toolboxModel = new DefaultListModel<>();
        toolboxModel.addElement(new HohmannTransferProcedure(null, null, 0, 0) {
            @Override public String toString() { return "Hohmann Transfer"; }
        });
        toolboxModel.addElement(new LunarTransferProcedure(null, null, 0) {
            @Override public String toString() { return "Lunar Transfer"; }
        });
        toolboxModel.addElement(new LunarReturnProcedure(null,  0) {
            @Override public String toString() { return "Lunar Return"; }
        });
        toolboxModel.addElement(new WaitProcedure(ConversionHelper.dayToSecond(1)) {
            @Override public String toString() { return "Wait"; }
        });
        toolboxModel.addElement(new PrintProcedure("Message") {
            @Override public String toString() { return "Print Message"; }
        });

        JList<FlightProcedure> toolboxList = new JList<>(toolboxModel);
        toolboxList.setBorder(new TitledBorder("Procedure Toolbox"));
        toolboxList.setDragEnabled(true);
        toolboxList.setTransferHandler(new ProcedureExportHandler());

        // Flight plan timeline (target of drops)
        planList = new JList<>(planModel);
        planList.setBorder(new TitledBorder("Flight Plan Timeline"));
        planList.setDropMode(DropMode.INSERT); // insert at drop index
        planList.setTransferHandler(new ProcedureImportHandler(planList, planModel));
        planList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer to show getTimelineDescription
        planList.setCellRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof FlightProcedure proc)
                {
                    setText(proc.getTimeLineDescription());
                }

                return this;
            }
        });

        // Split pane to show toolbox and plan list side by side
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(toolboxList), new JScrollPane(planList));
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }

    // ------------------------------
    // Repeat checkbox
    // ------------------------------
    private void setupRepeatCheckbox()
    {
        repeatCheck = new JCheckBox("Repeat Flight Plan");
        repeatCheck.addActionListener(e ->
        {
            boolean checked = repeatCheck.isSelected();
        });
        add(repeatCheck, BorderLayout.WEST);
    }


    // ------------------------------
    // Buttons panel
    // ------------------------------
    private void setupButtons()
    {
        JPanel buttonPanel = new JPanel(new GridLayout(8, 1, 5, 5));

        // 1. Edit Selected
        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(this::editSelectedProcedure);

        // 2. Remove Last
        JButton removeButton = new JButton("Remove Last");
        removeButton.addActionListener(this::removeLastProcedure);

        // 3. Save Plan
        JButton saveButton = new JButton("Save Plan");
        saveButton.addActionListener(e -> saveCurrentPlanToRepository());

        // 4. Load Plan
        JButton loadButton = new JButton("Load Plan");
        loadButton.addActionListener(e -> loadPlanFromRepository());

        // 5. New Plan
        JButton newPlanButton = new JButton("New Plan");
        newPlanButton.addActionListener(e -> newPlan());

        // 6. Save Plan File
        JButton savePlanFileButton = new JButton("Save Plan File");
        savePlanFileButton.addActionListener(e -> savePlanFile());

        // 7. Load Plan File
        JButton loadPlanFileButton = new JButton("Load Plan File");
        loadPlanFileButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this,
                    "Warning: Loading will overwrite currently loaded plans.\nContinue?",
                    "Load Warning", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
            {
                loadPlanFile();
            }
        });

        // 8. New Plan File
        JButton newPlanFileButton = new JButton("New Plan File");
        newPlanFileButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this,
                    "Warning: This will clear all flight plans in memory.\nContinue?",
                    "Create New Plan File", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION)
            {
                FlightPlanRepository.allPlans.clear();
                planModel.clear();
                planNameField.setText("Untitled Flight Plan");
                startingLocation = null;
                currentLocation = null;
                updatePlanetLabels();
                JOptionPane.showMessageDialog(this, "New plan file created.");
            }
        });

        // add buttons in order
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(newPlanButton);
        buttonPanel.add(savePlanFileButton);
        buttonPanel.add(loadPlanFileButton);
        buttonPanel.add(newPlanFileButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    private void saveCurrentPlanToRepository()
    {
        String name = planNameField.getText().trim();
        if (name.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Please enter a flight plan name first!");
            return;
        }

        FlightPlan fp = new FlightPlan(repeatCheck.isSelected());
        fp.setName(name);
        fp.setDescription(planDescriptionArea.getText().trim());
        if (repeatCheck.isSelected())
        {
           if (!startingLocation.equals(currentLocation))
           {
               JOptionPane.showMessageDialog(this, "Ensure starting destination equals current destination for a repeat plan!");
               return;
           }
        }
        for (int i = 0; i < planModel.size(); i++)
        {
            fp.addProcedure(planModel.get(i));
        }

        FlightPlanRepository.allPlans.removeIf(p -> p.getName().equals(name)); // overwrite same name
        FlightPlanRepository.allPlans.add(fp);

        JOptionPane.showMessageDialog(this, "Plan '" + name + "' saved to repository!");
    }
    private void loadPlanFromRepository()
    {
        if (FlightPlanRepository.allPlans.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "No saved plans in repository!");
            return;
        }

        String[] names = FlightPlanRepository.allPlans.stream()
                .map(FlightPlan::getName)
                .toArray(String[]::new);

        String chosen = (String) JOptionPane.showInputDialog(this,
                "Select plan to load:", "Load Flight Plan",
                JOptionPane.PLAIN_MESSAGE, null, names, null);

        if (chosen == null) return;

        FlightPlan plan = FlightPlanRepository.allPlans.stream()
                .filter(p -> p.getName().equals(chosen))
                .findFirst().orElse(null);

        if (plan != null)
        {
            planModel.clear();
            for (FlightProcedure p : plan.getProcedures())
            {
                planModel.addElement(p);
            }
            planNameField.setText(plan.getName());
            repeatCheck.setSelected(plan.isRepeat());
            planDescriptionArea.setText(plan.getDescription());
            updatePlanetLabels();
            JOptionPane.showMessageDialog(this, "Loaded plan '" + plan.getName() + "'");
        }
    }
    private void newPlan()
    {
        int res = JOptionPane.showConfirmDialog(this,
                "Start a new plan? Unsaved progress will be lost.",
                "New Plan", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION)
        {
            planModel.clear();
            planNameField.setText("Untitled Flight Plan");
            planDescriptionArea.setText("");
            repeatCheck.setSelected(false);
            startingLocation = null;
            currentLocation = null;
            updatePlanetLabels();
            JOptionPane.showMessageDialog(this, "New blank plan created.");
        }
    }

    // Save all repository plans to file
    private void savePlanFile()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle("Save Plan File");

        // Only show JSON files
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile();
            // make sure file ends with .json
            if (!file.getName().toLowerCase().endsWith(".json"))
            {
                file = new File(file.getAbsolutePath() + ".json");
            }

            try
            {
                FlightPlanIO.savePlansToFile(FlightPlanRepository.allPlans, file);
                JOptionPane.showMessageDialog(this, "All plans saved to file: " + file.getName());
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
            }
        }
    }
    private void loadPlanFile()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle("Load Plan File");

        // Only show JSON files
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile();
            try
            {
                List<FlightPlan> loaded = FlightPlanIO.loadPlansFromFile(file, universe);
                FlightPlanRepository.allPlans.clear();
                FlightPlanRepository.allPlans.addAll(loaded);

                JOptionPane.showMessageDialog(this,
                        "Loaded " + loaded.size() + " flight plans from " + file.getName() + ".");

                if (!loaded.isEmpty())
                {
                    FlightPlan first = loaded.get(0);
                    planModel.clear();
                    for (FlightProcedure p : first.getProcedures())
                    {
                        planModel.addElement(p);
                    }
                    planNameField.setText(first.getName());
                    repeatCheck.setSelected(first.isRepeat());
                    planDescriptionArea.setText(first.getDescription()); // include description
                    updatePlanetLabels();
                }
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
            }
        }
    }



    // ------------------------------
    // Edit selected procedure
    // ------------------------------
    private void editSelectedProcedure(ActionEvent e)
    {
        FlightProcedure proc = planList.getSelectedValue();
        if (proc == null) return;

        if (proc instanceof HohmannTransferProcedure htp) selectCelestialsForHohmann(htp);
        else if (proc instanceof LunarTransferProcedure ltp) selectCelestialsForLunar(ltp);
        else if (proc instanceof LunarReturnProcedure lrp) selectCelestialsForLunarReturn(lrp);
        else if (proc instanceof WaitProcedure wp)
        {
            String input = JOptionPane.showInputDialog(this, "Enter wait time (seconds):", wp.getDuration());
            if (input != null) { try { wp.setDuration(Double.parseDouble(input)); } catch (Exception ignored) {} }
        }
        else if (proc instanceof PrintProcedure pp)
        {
            String input = JOptionPane.showInputDialog(this, "Enter message:", pp.getMessage());
            if (input != null) pp.setMessage(input);
        }
        updatePlanetLabels();
        planList.repaint();
    }
    private void removeLastProcedure(ActionEvent e)
    {
        planModel.remove(planModel.size()-1); // remove last element
        updatePlanetLabels();
    }
    // ------------------------------
    // Celestial selection dialogs
    // ------------------------------
    private void selectCelestialsForHohmann(HohmannTransferProcedure proc)
    {
        int idx = planList.getSelectedIndex();
        boolean isFirst = (idx == 0);
        boolean isLast = (idx == planModel.size() - 1);

        Celestial origin = proc.getOrigin();
        Celestial destination = proc.getDestination();

        // Only ask for origin if allowed
        if (isFirst)
        {
            origin = (Celestial) JOptionPane.showInputDialog(this, "Select origin planet:", "Origin",
                    JOptionPane.PLAIN_MESSAGE, null, allCelestials.toArray(), origin);
            if (origin == null) return; // cancelled
        }

        // Only ask for destination if allowed
        if (isLast)
        {
            destination = (Celestial) JOptionPane.showInputDialog(this, "Select target planet:", "Target",
                    JOptionPane.PLAIN_MESSAGE, null, universe.findCoOrbitals(origin).toArray(), destination);
            if (destination == null) return; // cancelled
        }

        // Ask for max delta-V
        String dvInput = JOptionPane.showInputDialog(this, "Max delta-V:", proc.getMaxDeltaV());
        if (dvInput == null) return; // cancelled

        // Ask for rough travel time
        String timeInput = JOptionPane.showInputDialog(this, "Rough travel time (days):", ConversionHelper.secondToDay(proc.getRoughTime()));
        if (timeInput == null) return; // cancelled

        try
        {
            if (isFirst) proc.setOrigin(origin);
            if (isLast) proc.setDestination(destination);

            proc.setMaxDeltaV(Double.parseDouble(dvInput));
            proc.setRoughTime(ConversionHelper.dayToSecond(Double.parseDouble(timeInput)));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Invalid input, procedure not updated!");
        }
        updatePlanetLabels();
    }
    private void selectCelestialsForLunar(LunarTransferProcedure proc)
    {
        int idx = planList.getSelectedIndex();
        boolean isFirst = (idx == 0);

        Celestial planet = proc.getPlanet();
        // Only allow changing origin if first procedure
        if (isFirst || planet == null)
        {
            planet = (Celestial) JOptionPane.showInputDialog(this, "Select origin planet:", "Planet",
                    JOptionPane.PLAIN_MESSAGE, null, universe.returnParents().toArray(), null);
            if (planet == null) return; // cancelled
        }

        List<Celestial> moons = planet.children;
        if (moons.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Selected planet has no moons!");
            return;
        }
        Celestial moon = proc.getMoon();
        if (moon == null)
        {
            moon = (Celestial) JOptionPane.showInputDialog(this, "Select target moon:", "Moon",
                    JOptionPane.PLAIN_MESSAGE, null, moons.toArray(), null);
            if (moon == null) return; // cancelled
        }
        String dvInput = JOptionPane.showInputDialog(this, "Max delta-V:", proc.getMaxDeltaV());
        if (dvInput == null) return;

        try
        {
            proc.setPlanet(planet);
            proc.setMoon(moon);
            proc.setMaxDeltaV(Double.parseDouble(dvInput));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Invalid input, procedure not updated!");
        }

        updatePlanetLabels();
    }

    private void selectCelestialsForLunarReturn(LunarReturnProcedure proc)
    {
        int idx = planList.getSelectedIndex();
        boolean isFirst = (idx == 0);

        Celestial moon =  proc.getMoon();
        if (moon == null)
        {
            moon = (Celestial) JOptionPane.showInputDialog(this, "Select moon:", "Moon",
                    JOptionPane.PLAIN_MESSAGE, null, universe.returnMoons().toArray(), null);
            if (moon == null) return;
        }

        String dvInput = JOptionPane.showInputDialog(this, "Max delta-V:", proc.getMaxDeltaV());
        if (dvInput == null) return;

        try
        {
            proc.setPlanet(moon.parent);
            proc.setMoon(moon);
            proc.setMaxDeltaV(Double.parseDouble(dvInput));
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Invalid input, procedure not updated!");
        }

        updatePlanetLabels();
    }
    private void setupBottomPanel()
    {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Repeat checkbox at the bottom
        repeatCheck = new JCheckBox("Repeat Flight Plan");
        repeatCheck.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Labels stacked neatly
        startingPlanetLabel = new JLabel("Starting Planet: None");
        startingPlanetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        currentLocationLabel = new JLabel("Current Plan Location: None");
        currentLocationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel.add(startingPlanetLabel);
        bottomPanel.add(currentLocationLabel);
        bottomPanel.add(Box.createVerticalStrut(8)); // lil’ spacer
        bottomPanel.add(repeatCheck);

        add(bottomPanel, BorderLayout.SOUTH);
    }



    private static class ProcedureExportHandler extends TransferHandler
    {
        @Override
        public int getSourceActions(JComponent c)
        {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            JList<?> list = (JList<?>) c;
            Object val = list.getSelectedValue();
            return new TransferableFlightProcedure((FlightProcedure) val);
        }
    }

    private class ProcedureImportHandler extends TransferHandler
    {
        private final JList<FlightProcedure> targetList;
        private final DefaultListModel<FlightProcedure> targetModel;

        public ProcedureImportHandler(JList<FlightProcedure> list, DefaultListModel<FlightProcedure> model)
        {
            this.targetList = list;
            this.targetModel = model;
        }

        @Override
        public boolean canImport(TransferSupport support)
        {
            boolean supported = support.isDataFlavorSupported(TransferableFlightProcedure.procedureFlavor);
            return supported;
        }

        @Override
        public boolean importData(TransferSupport support)
        {
            if (!canImport(support))
                return false;

            try
            {
                FlightProcedure proc = (FlightProcedure) support.getTransferable()
                        .getTransferData(TransferableFlightProcedure.procedureFlavor);
                FlightProcedure newProc = null;

                // Prompt for parameters and cancel if user hits cancel
                if (proc instanceof HohmannTransferProcedure htp)
                {
                    Celestial origin = currentLocation;
                    if (origin == null)
                    {
                        origin = (Celestial) JOptionPane.showInputDialog(null, "Select origin planet:", "Origin",
                                JOptionPane.PLAIN_MESSAGE, null, allCelestials.toArray(), null);
                        if (origin == null) return false; // user canceled
                    }
                    Celestial dest = (Celestial) JOptionPane.showInputDialog(null, "Select target planet:", "Destination",
                            JOptionPane.PLAIN_MESSAGE, null, universe.findCoOrbitals(origin).toArray(), null);
                    if (dest == null) return false; // user canceled

                    String dvInput = JOptionPane.showInputDialog(null, "Max Δv:", htp.getMaxDeltaV());
                    if (dvInput == null) return false;

                    String roughTravelTime = JOptionPane.showInputDialog(null, "Rough travel time (days):", htp.getRoughTime());
                    if (roughTravelTime == null) return false;

                    currentLocation = dest;
                    if(startingLocation == null) startingLocation = origin;

                    newProc = new HohmannTransferProcedure(origin, dest, Double.parseDouble(dvInput), ConversionHelper.dayToSecond(Double.parseDouble(roughTravelTime)));
                }
                else if (proc instanceof LunarTransferProcedure ltp)
                {
                    Celestial origin = currentLocation;
                    if (origin == null)
                    {
                        origin = (Celestial) JOptionPane.showInputDialog(null, "Select origin planet:", "Origin",
                                JOptionPane.PLAIN_MESSAGE, null, universe.returnParents().toArray(), null);
                        if (origin == null) return false; // cancel
                    }

                    Celestial moon = (Celestial) JOptionPane.showInputDialog(null, "Select target moon:", "Destination",
                            JOptionPane.PLAIN_MESSAGE, null, origin.children.toArray(), null);
                    if (moon == null) return false;

                    String dvInput = JOptionPane.showInputDialog(null, "Max Δv:", ltp.getMaxDeltaV());
                    if (dvInput == null) return false;

                    currentLocation = moon;
                    if(startingLocation == null) startingLocation = origin;
                    newProc = new LunarTransferProcedure(origin, moon, Double.parseDouble(dvInput));
                }
                else if (proc instanceof LunarReturnProcedure lrp)
                {
                    Celestial moon = lrp.getMoon();
                    moon = currentLocation;
                    if (moon == null)
                    {
                        moon = (Celestial) JOptionPane.showInputDialog(null, "Select origin planet:", "Origin",
                            JOptionPane.PLAIN_MESSAGE, null, universe.returnMoons().toArray(), null);
                        if (moon == null) return false; // cancel
                    }
                    String dvInput = JOptionPane.showInputDialog(null, "Max Δv:", lrp.getMaxDeltaV());
                    if (dvInput == null) return false;


                    currentLocation = moon.parent;
                    if(startingLocation == null) startingLocation = moon;
                    newProc = new LunarReturnProcedure(moon, Double.parseDouble(dvInput));
                }
                else if (proc instanceof WaitProcedure wp)
                {
                    String input = JOptionPane.showInputDialog(null, "Wait time (seconds):", wp.getDuration());
                    if (input == null) return false;
                    newProc = new WaitProcedure(Double.parseDouble(input));
                }
                else if (proc instanceof PrintProcedure pp)
                {
                    String msg = JOptionPane.showInputDialog(null, "Message:", pp.getMessage());
                    if (msg == null) return false;
                    newProc = new PrintProcedure(msg);
                }
                else
                {
                    return false; // unknown type
                }

                // Only now do we insert
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int index = dl.getIndex();
                if (index < 0) index = targetModel.getSize();

                targetModel.add(index, newProc);

                updatePlanetLabels();
                return true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return false;
            }
        }


    }

    private Celestial getProcedureOriginSafe(FlightProcedure proc)
    {
        try
        {
            Celestial o = proc.getProcedureOrigin();
            return o != null ? o : currentLocation;
        }
        catch (Exception e) { return currentLocation; }
    }

    private Celestial getProcedureDestinationSafe(FlightProcedure proc)
    {
        try
        {
            Celestial d = proc.getProcedureDestination();
            return d != null ? d : getProcedureOriginSafe(proc);
        }
        catch (Exception e) { return getProcedureOriginSafe(proc); }
    }

    private static class TransferableFlightProcedure implements Transferable
    {
        static DataFlavor procedureFlavor = new DataFlavor(FlightProcedure.class, "FlightProcedure");
        private final FlightProcedure proc;

        public TransferableFlightProcedure(FlightProcedure proc)
        {
            this.proc = proc;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[]{procedureFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return procedureFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return proc;
        }
    }
}
