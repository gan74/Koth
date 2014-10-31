
package koth.system;

import koth.game.Board;
import koth.game.Action;
import koth.user.Human;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.Set;

public class Display implements Runnable {

    private final Object lock = new Object();

    private final Simulator simulator;
    private final Animator animator;
    private koth.util.Renderer renderer;

    private volatile boolean running, paused;
    private float camx, camy, camr;
    private float dcamx, dcamy, dcamr;
    private volatile float speed;
    private Action action;

    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy strategy;

    public Display(Simulator simulator) {
        // Create simulation components
        this.simulator = simulator;
        animator = new Animator(simulator.getGame());
        renderer = new koth.util.Renderer.Isometric();
        // Initialize control variables
        running = true;
        paused = false;
        key('=');
       // key('*');
       // key('*');
        camx = dcamx;
        camy = dcamy;
        camr = dcamr;
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
            case 27:
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
                    if (renderer instanceof koth.util.Renderer.Isometric)
                        renderer = new koth.util.Renderer.Orthogonal();
                    else
                        renderer = new koth.util.Renderer.Isometric();
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
        synchronized (lock) {
            switch (button) {
                // Left button to interact
                case MouseEvent.BUTTON1:
                    // TODO
                    break;
                // Right button to move camera
                case MouseEvent.BUTTON3:
                    Point.Float p = renderer.unproject(x, y, 0);
                    dcamx = p.x;
                    dcamy = p.y;
                    break;
            }
        }
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
        Set<koth.util.Renderer.Cube> cubes = animator.getCubes();
        synchronized (lock) {
            renderer.setCubes(cubes);
            renderer.setCamera(camx, camy, 0, camr);
            renderer.setViewport(w, h);
            renderer.paint(g);
        }
        // Draw HUD
        // TODO HUD (paused, current team/actions, turn...)
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
        // TODO vsync with Toolkit.getDefaultToolkit().sync()?
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
                Action action = null;
                synchronized (lock) {
                    // TODO wait on event
                }
                human.set(action);
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
