
package koth.system;

import koth.game.Board;
import koth.game.Action;
import koth.game.Pawn;
import koth.util.Vector;
import koth.user.Human;
import koth.util.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.Set;

public class Display implements Runnable {

    private final Object lock = new Object();

    private final Simulator simulator;
    private final Animator animator;
    private Renderer renderer;

    private volatile boolean running, paused;
    private float camx, camy, camr;
    private float dcamx, dcamy, dcamr;
    private volatile float speed;
    private boolean waiting;
    private Pawn pawn;
    private Action action;

    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy strategy;

    public Display(Simulator simulator) {
        // Create simulation components
        this.simulator = simulator;
        animator = new Animator(simulator.getGame());
        renderer = new Renderer.Isometric();
        // Initialize control variables
        running = true;
        paused = false;
        key('=');
        camx = dcamx;
        camy = dcamy;
        camr = dcamr;
        waiting = false;
        pawn = null;
        action = null;
        // Create window
        frame = new JFrame("Match");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                key((char)27);
            }
        });
        frame.setIgnoreRepaint(true);
        frame.setBackground(Color.BLACK);
        frame.setFocusable(false);
        frame.setFocusTraversalKeysEnabled(false);
        // Create canvas with double-buffering
        canvas = new Canvas();
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouse(e.getX(), e.getY(), e.getButton());
            }
        });
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                key(e.getKeyChar());
            }
        });
        canvas.setIgnoreRepaint(true);
        canvas.setBackground(Color.BLACK);
        canvas.setFocusable(true);
        canvas.setFocusTraversalKeysEnabled(false);
        frame.add(canvas);
        frame.pack();
        frame.setSize(1280, 768);
        frame.setLocationRelativeTo(null);
        canvas.createBufferStrategy(2);
        strategy = canvas.getBufferStrategy();
    }

    private void key(char c) {
        switch (c) {
            // Use ESCAPE to exit
            case KeyEvent.VK_ESCAPE:
                running = false;
                // TODO interrupt?
                break;
            // Use SPACE to toggle pause
            case ' ':
                paused = !paused;
                break;
            // Use TAB to switch view mode
            case '\t':
                synchronized (lock) {
                    if (renderer instanceof Renderer.Isometric)
                        renderer = new Renderer.Orthogonal();
                    else
                        renderer = new Renderer.Isometric();
                }
                break;
            // Use + to zoom
            case '+':
                synchronized (lock) {
                    dcamr /= 1.4f;
                }
                break;
            // Use - to unzoom
            case '-':
                synchronized (lock) {
                    dcamr *= 1.4f;
                }
                break;
            // Use '*' to increase speed
            case '*':
                speed *= 2;
                break;
            // Use '/' to decrease speed
            case '/':
                speed /= 2;
                break;
            // Use '=' to reset camera and time
            case '=':
                synchronized (lock) {
                    Board board = simulator.getGame().getBoard();
                    dcamx = (board.getMax().getX() - board.getMin().getX()) * 0.5f;
                    dcamy = (board.getMax().getY() - board.getMin().getY()) * 0.5f;
                    dcamr = 2 * (float)Math.sqrt(dcamx * dcamx + dcamy * dcamy);
                    dcamx += board.getMin().getX();
                    dcamy += board.getMin().getY();
                    speed = 2;
                }
                break;
            // TODO arrows to move camera
        }
    }

    private void mouse(int x, int y, int button) {
        switch (button) {
            // Left button to interact
            case MouseEvent.BUTTON1:
                synchronized (lock) {
                    if (waiting) {
                        if (pawn == null) {
                            // If there is no pawn selected, check if we clicked one
                            Point.Float p = renderer.unproject(x, y, 0);
                            Vector l = new Vector(Math.round(p.x), Math.round(p.y));
                            Pawn pawn = simulator.getGame().getPawn(l);
                            if (pawn != null && pawn.getTeam() == simulator.getTeam()) {
                                this.pawn = pawn;
                                System.out.println(this.pawn);
                            }
                        } else {
                            // There already is a selected pawn, check if we clicked on the HUD
                            pawn = null;
                            // TODO check for interface click and play (or cancel)
                            waiting = false;
                            lock.notifyAll();
                        }
                    }
                }
                break;
            // Right button to move camera
            case MouseEvent.BUTTON3:
                synchronized (lock) {
                    Point.Float p = renderer.unproject(x, y, 0);
                    dcamx = p.x;
                    dcamy = p.y;
                    break;
                }
            }
    }

    private void drawString(Graphics2D g, String txt, float cx, float cy, int h, int v) {
        Rectangle2D rect = g.getFontMetrics().getStringBounds(txt, g);
        cx -= rect.getWidth() * (1 - h) * 0.5f;
        cy += rect.getHeight() * (1 - v) * 0.5f;
        g.drawString(txt, cx, cy);
    }

    private void animate(float dt, Graphics2D g, int w, int h) {
        // Update camera
        final float halflife = 0.2f;
        float c = (float)Math.pow(2, -dt / halflife);
        synchronized (lock) {
            camx = c * camx + (1 - c) * dcamx;
            camy = c * camy + (1 - c) * dcamy;
            camr = c * camr + (1 - c) * dcamr;
        }
        // Clear background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        // Compute and paint new frame
        animator.step(dt);
        Set<Renderer.Cube> cubes = animator.getCubes();
        synchronized (lock) {
            renderer.setCubes(cubes);
            renderer.setCamera(camx, camy, 0, camr);
            renderer.setViewport(w, h);
            renderer.paint(g);
        }
        // Check for human player
        synchronized (lock) {
            if (pawn != null) {
                // If there is a pawn, draw interactive HUD
                // TODO draw interactive HUD
                Point.Float p = renderer.project(pawn.getLocation().getX(), pawn.getLocation().getY(), 0.5f);
                g.setColor(Color.ORANGE);
                g.drawOval((int)p.x-4 , (int)p.y-4, 8,8);
            }
        }
        // Draw HUD
        synchronized (lock) {
            // Print info about current turn
            g.setColor(animator.getTeamColor(simulator.getTeam()));
            g.setFont(g.getFont().deriveFont(20.0f));
            drawString(g, "Turn " + simulator.getTurn() + ", team " + simulator.getTeam() + " (" + simulator.getPoints() + " remaining)", 10, 10, 1, -1);
            // Show if game is paused
            if (paused) {
                g.setColor(Color.WHITE);
                g.setFont(g.getFont().deriveFont(100.0f));
                drawString(g, "PAUSED", w / 2, h / 2, 0, 0);
            }
        }
    }

    private void animate(float dt) {
        do {
            Graphics2D g = null;
            try {
                g = (Graphics2D)strategy.getDrawGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                animate(dt, g, canvas.getWidth(), canvas.getHeight());
            } finally {
                if (g != null)
                    g.dispose();
            }
            strategy.show();
        } while (strategy.contentsLost());
    }

    private void animate() {
        long before = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = (now - before) * 0.000000001f * speed;
            if (dt < 0.001f)
                dt = 0.001f;
            animate(dt);
            before = now;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {}
        }
    }

    private void compute() {
        while (true) {
            // If enough animations in queue, sleep
            synchronized (animator) {
                try {
                    while (!animator.isExhausted() || paused) {
                        if (!running || simulator.getGame().isFinished())
                            return;
                        animator.wait(100);
                    }
                } catch (InterruptedException e) {}
            }
            if (!running || simulator.getGame().isFinished())
                return;
            // If it is a human, wait until graphical interaction
            if (simulator.getAi() instanceof Human) {
                Human human = (Human)simulator.getAi();
                synchronized (lock) {
                    waiting = true;
                    pawn = null;
                    action = null;
                    while (waiting && running) {
                        try {
                            lock.wait(100);
                        } catch (InterruptedException e) {}
                    }
                    System.out.println("Human played " + action);
                    human.set(action);
                    waiting = false;
                    pawn = null;
                    action = null;
                }
            }
            // Play and register events
            simulator.play(animator);
        }
    }

    @Override
    public void run() {
        frame.setVisible(true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                animate();
            }
        };
        thread.start();
        compute();
        try {
            thread.join();
        } catch (InterruptedException e) {}
        frame.dispose();
    }

}
