package gui;

import flight.FlightPlan;
import flight.FlightPlanRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FlightPlanBrowser extends JDialog
{
    private FlightPlan selectedPlan;

    public FlightPlanBrowser(Frame owner)
    {
        super(owner, "Browse Flight Plans", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        DefaultListModel<FlightPlan> model = new DefaultListModel<>();
        for (FlightPlan fp : FlightPlanRepository.getAllPlans())
        {
            model.addElement(fp);
        }

        JList<FlightPlan> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FlightPlan fp)
                {
                    setText(fp.getName());
                }
                return this;
            }
        });

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        list.addListSelectionListener(e ->
        {
            if (!e.getValueIsAdjusting())
            {
                FlightPlan fp = list.getSelectedValue();
                if (fp != null)
                {
                    descriptionArea.setText(fp.getDescription());
                }
            }
        });

        JButton loadBtn = new JButton(new AbstractAction("Load into Editor")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                selectedPlan = list.getSelectedValue();
                if (selectedPlan != null)
                {
                    dispose();
                }
            }
        });

        JButton cancelBtn = new JButton(new AbstractAction("Cancel")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                selectedPlan = null;
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadBtn);
        buttonPanel.add(cancelBtn);

        add(new JScrollPane(list), BorderLayout.CENTER);
        add(descScroll, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    public FlightPlan getSelectedPlan()
    {
        return selectedPlan;
    }
}
