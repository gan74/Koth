
package koth.system;

import koth.game.*;
import koth.util.ClassManager;

import javax.swing.*;
import java.util.Map;

// TODO documentation of Program

public class Program {

    public static void main(String[] args) {
        // Parse arguments
        // TODO parse args (size, external jars, simulation parameters...)

        // Setup system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        // Load factories
        Map<String, ClassManager.Factory<Generator>> generators = ClassManager.getSubclasses(Generator.class, "koth.user");
        System.out.println(generators.size() + " Generator(s) found:");
        for (String name : generators.keySet())
            System.out.println("  " + name);
        Map<String, ClassManager.Factory<AI>> ais = ClassManager.getSubclasses(AI.class, "koth.user");
        System.out.println(ais.size() + " AI(s) found:");
        for (String name : ais.keySet())
            System.out.println("  " + name);
        
        ais.remove("Human");

        // Create and launch simulation
        Simulator simulator = Configurator.show(null, generators.values(), ais.values());
        if (simulator != null) {
            Display display = new Display(simulator);
            display.run();
        } else {
            Tournament.Descriptor descriptor = new Tournament.Descriptor(ais.values(), generators.get("jlb.Cross"), new Rules(), 2);
            Tournament tournament = new Tournament(descriptor);
            tournament.run();
        }
    }

    public static boolean showDialog(JComponent parent, JComponent content, String title) {
        return JOptionPane.showOptionDialog(
            parent, content, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, new String[] {"OK", "Cancel"}, "OK") == JOptionPane.OK_OPTION;
    }

}
