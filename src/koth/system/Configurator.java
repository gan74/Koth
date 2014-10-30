
package koth.system;

import koth.game.*;
import koth.util.ClassManager;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.*;
import java.util.Vector;

// TODO documentation of ConfigPanel

public class Configurator extends JPanel {

    // TODO add some parameters to choose rules (team size, health, actions...)

    private JComboBox first, second, generators;

    public Configurator(Collection<ClassManager.Factory<AI>> ais, Collection<ClassManager.Factory<Generator>> generators) {
        if (generators == null || ais == null || generators.contains(null) || ais.contains(null))
            throw new NullPointerException();
        if (generators.isEmpty() || ais.isEmpty())
            throw new IllegalArgumentException();
        Vector<ClassManager.Factory<AI>> as = new Vector<ClassManager.Factory<AI>>(ais);
        // TODO sort factories (and if Human is available, put it on first position)
        first = new JComboBox(as);
        second = new JComboBox(as);
        Vector<ClassManager.Factory<Generator>> gs = new Vector<ClassManager.Factory<Generator>>(generators);
        this.generators = new JComboBox(gs);
        setLayout(new GridLayout(3, 1, 4, 4));
        add(first);
        add(second);
        add(this.generators);
    }
    
    public ClassManager.Factory<Generator> getGenerator() {
        @SuppressWarnings("unchecked")
        ClassManager.Factory<Generator> f = (ClassManager.Factory<Generator>)generators.getSelectedItem();
        return f;
    }

    public List<ClassManager.Factory<AI>> getAis() {
        List<ClassManager.Factory<AI>> fs = new ArrayList<ClassManager.Factory<AI>>();
        @SuppressWarnings("unchecked")
        ClassManager.Factory<AI> f1 = (ClassManager.Factory<AI>)first.getSelectedItem();
        fs.add(f1);
        @SuppressWarnings("unchecked")
        ClassManager.Factory<AI> f2 = (ClassManager.Factory<AI>)second.getSelectedItem();
        fs.add(f2);
        return fs;
    }

    public Rules getRules() {
        return new Rules();
    }

    public static Simulator show(JComponent parent, Collection<ClassManager.Factory<Generator>> generators, Collection<ClassManager.Factory<AI>> ais) {
        Configurator configurator = new Configurator(ais, generators);
        if (!Program.showDialog(parent, configurator, "Configure simulation"))
            return null;
        return new Simulator(configurator.getGenerator(), configurator.getAis(), configurator.getRules());
    }

}
