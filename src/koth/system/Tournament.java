
package koth.system;

import koth.game.AI;
import koth.game.Generator;
import koth.game.Rules;
import koth.user.Human;
import koth.util.ClassManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

// TODO documentation of Tournament

public class Tournament implements Runnable {

    public static class Descriptor implements Iterable<Simulator> {

        private List<ClassManager.Factory<AI>> ais;
        private List<ClassManager.Factory<Generator>> generators;
        private Rules rules;
        private int teams;

        public Descriptor(Collection<ClassManager.Factory<AI>> ais, Collection<ClassManager.Factory<Generator>> generators, Rules rules, int teams) {
            if (ais == null || generators == null || rules == null)
                throw new NullPointerException();
            if (ais.isEmpty() || generators.isEmpty() || teams < 2)
                throw new IllegalArgumentException();
            List<ClassManager.Factory<AI>> as = new ArrayList<ClassManager.Factory<AI>>();
            for (ClassManager.Factory<AI> a : ais)
                if (!a.getClazz().equals(Human.class) && !as.contains(a))
                    as.add(a);
            Collections.sort(as, new Comparator<ClassManager.Factory<AI>>() {
                @Override
                public int compare(ClassManager.Factory<AI> a, ClassManager.Factory<AI> b) {
                    return a.getName().compareTo(b.getName());
                }
            });
            this.ais = Collections.unmodifiableList(as);
            List<ClassManager.Factory<Generator>> gs = new ArrayList<ClassManager.Factory<Generator>>();
            for (ClassManager.Factory<Generator> g : generators)
                if (!gs.contains(g))
                    gs.add(g);
            this.generators = Collections.unmodifiableList(new ArrayList<ClassManager.Factory<Generator>>(generators));
            // TODO check that teams, ais and generators are not too large
            this.rules = rules;
            this.teams = teams;
        }

        public Descriptor(Collection<ClassManager.Factory<AI>> ais, ClassManager.Factory<Generator> generator, Rules rules, int teams) {
            this(ais, Collections.singletonList(generator), rules, teams);
        }

        public List<ClassManager.Factory<AI>> getAis() {
            return ais;
        }

        public List<ClassManager.Factory<Generator>> getGenerators() {
            return generators;
        }

        public Rules getRules() {
            return rules;
        }

        public int getTeams() {
            return teams;
        }

        public long getSize() {
            long r = 1;
            for (int i = 0; i < teams; ++i)
                r *= ais.size();
            return r * generators.size();
        }

        @Override
        public Iterator<Simulator> iterator() {
            return new Iterator<Simulator>() {

                private int[] a = new int[teams];
                private int g;

                @Override
                public boolean hasNext() {
                    return g < generators.size();
                }

                @Override
                public Simulator next() {
                    if (g == generators.size())
                        throw new NoSuchElementException();
                    List<ClassManager.Factory<AI>> lst = new ArrayList<ClassManager.Factory<AI>>();
                    for (int i : a)
                        lst.add(ais.get(i));
                    ClassManager.Factory<Generator> gen = generators.get(g);
                    for (int i = a.length - 1; i >= 0; --i)
                        if (++a[i] == ais.size()) {
                            a[i] = 0;
                            if (i == 0)
                                ++g;
                        } else
                            break;
                    return new Simulator(gen, lst, rules);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

    }

    public static class Executor implements Runnable {

        public static interface Listener {

            public void finished(Simulator simulator);

        }

        private Descriptor descriptor;
        private Listener listener;
        private Iterator<Simulator> iterator;
        private Thread[] threads;
        private boolean running;

        public Executor(Descriptor descriptor, Listener listener) {
            if (descriptor == null || listener == null)
                throw new NullPointerException();
            this.descriptor = descriptor;
            this.listener = listener;
        }

        public Descriptor getDescriptor() {
            return descriptor;
        }

        private Simulator next() {
            if (iterator == null || !iterator.hasNext())
                iterator = descriptor.iterator();
            return iterator.next();
        }

        private synchronized Simulator query() {
            return running ? next() : null;
        }

        private synchronized void publish(Simulator simulator) {
            listener.finished(simulator);
        }

        private Runnable create() {
            return new Runnable() {
                @Override
                public void run() {
                    for (Simulator sim; (sim = query()) != null;) {
                        while (sim.getGame().isPlaying())
                            sim.play();
                        publish(sim);
                    }
                }
            };
        }

        @Override
        public void run() {
            synchronized (this) {
                if (threads != null)
                    throw new IllegalStateException();
                running = true;
                int units = 0; // Runtime.getRuntime().availableProcessors() - 1;
                threads = new Thread[units];
                for (int i = 0; i < threads.length; ++i) {
                    threads[i] = new Thread(create());
                    threads[i].start();
                }
            }
            create().run();
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {}
            }
            synchronized (this) {
                threads = null;
            }
        }

        public synchronized void stop() {
            running = false;
        }

    }

    // TODO public and extract these 3 classes
    private static class Model extends AbstractTableModel implements Executor.Listener {

        private final Descriptor descriptor;
        private Map<Class<?>, Integer> indices;
        private int[][] wins, losses, draws;

        public Model(Descriptor descriptor) {
            this.descriptor = descriptor;
            indices = new HashMap<Class<?>, Integer>();
            int N = descriptor.getAis().size();
            for (int i = 0; i < N; ++i)
                indices.put(descriptor.getAis().get(i).getClazz(), i);
            wins = new int[N][N];
            losses = new int[N][N];
            draws = new int[N][N];
        }

        @Override
        public int getRowCount() {
            return wins.length;
        }

        @Override
        public int getColumnCount() {
            return wins.length + 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return descriptor.getAis().get(rowIndex).getName();
            // TODO use percent instead? show rank? show others infos?
            if (columnIndex == 1) {
                int w = 0, l = 0, d = 0;
                for (int i = 0; i < wins.length; ++i) {
                    w += wins[rowIndex][i];
                    l += losses[rowIndex][i];
                    d += draws[rowIndex][i];
                }
                return w + " / " + l + " / " + d;
            }
            return wins[rowIndex][columnIndex - 2] + " / " +
                losses[rowIndex][columnIndex - 2] + " / " +
                draws[rowIndex][columnIndex - 2];
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0)
                return "AI";
            if (column == 1)
                return "Total";
            return descriptor.getAis().get(column - 2).getName();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public void finished(Simulator simulator) {
            int w = simulator.getGame().getWinner();
            int[] is = new int[simulator.getAis().size()];
            for (int i = 0; i < is.length; ++i)
                is[i] = indices.get(simulator.getAis().get(i).getClass());
            // TODO check if results are correct
            for (int i = 0; i < is.length; ++i)
                for (int j = i + 1; j < is.length; ++j)
                    if (w == i) {
                        wins[is[i]][is[j]]++;
                        losses[is[j]][is[i]]++;
                    } else if (w == j) {
                        losses[is[i]][is[j]]++;
                        wins[is[j]][is[i]]++;
                    } else {
                        draws[is[i]][is[j]]++;
                        draws[is[j]][is[i]]++;
                    }
            for (int i : is)
                fireTableRowsUpdated(i, i);
        }

    }

    private Executor executor;
    private JFrame frame;
    private Model model;

    // TODO save/load results of tournaments

    public Tournament(Descriptor descriptor) {
        if (descriptor == null)
            throw new NullPointerException();
        model = new Model(descriptor);
        executor = new Executor(descriptor, model);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        frame = new JFrame("Tournament");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.stop();
            }
        });
        frame.add(scroll);
        frame.setSize(600, 600);
        frame.pack();
    }

    @Override
    public void run() {
        frame.setVisible(true);
        executor.run();
        frame.dispose();
    }

}
