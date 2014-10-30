
package koth.system;

import koth.game.*;
import koth.util.Renderer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Generate and interpolate cubes for a game simulation.
 * Resulting cube set can be used with a <code>Renderer</code>.
 * This class is thread-safe.
 */
public class Animator implements Game.Listener {

    // TODO add idle animation using some time bias, no frame based

    private static class State {
        public Game game;
        public LinkedList<HashSet<Pawn>> frames;

        public State(Game game) {
            this.game = game;
            frames = new LinkedList<HashSet<Pawn>>();
            frames.add(new HashSet<Pawn>());
        }

        public void updated(Pawn after) {
            HashSet<Pawn> pawns = frames.getLast();
            pawns.remove(after);
            pawns.add(after);
        }

        public void frame() {
            if (!frames.getLast().isEmpty())
                frames.add(new HashSet<Pawn>());
        }

        public void done() {
            if (frames.getLast().isEmpty())
                frames.removeLast();
        }

        public Pawn get(int f, Pawn ref) {
            while (--f >= 0)
                if (f < frames.size())
                    for (Pawn p : frames.get(f))
                        if (ref.equals(p))
                            return p;
            return game.getPawn(ref);
        }
    }

    private LinkedList<State> states;
    private int frame;
    private float delta;

    /**
     * Create a new animator for specified game.
     */
    public Animator(Game game) {
        states = new LinkedList<State>();
        states.add(new State(game));
        frame = 0;
        delta = 0;
    }

    @Override
    public synchronized void updated(Pawn before, Pawn after) {
        states.getLast().updated(after);
    }

    @Override
    public synchronized void frame() {
        states.getLast().frame();
    }

    @Override
    public synchronized void done(Game before, Game after) {
        states.getLast().done();
        if (states.getLast().frames.isEmpty())
            states.removeLast();
        states.add(new State(after));
        notifyAll();
    }

    /**
     * Simulate some delta time.
     */
    public synchronized void step(float dt) {
        delta += dt;
        frame += (int)Math.floor(delta);
        delta %= 1.0f;
        while (states.size() > 1 && frame > states.getFirst().frames.size()) {
            frame -= states.getFirst().frames.size();
            states.removeFirst();
        }
        if (states.size() <= 1)
            frame = 0;
    }

    /**
     * Get whether all frames have been played.
     */
    public synchronized boolean isExhausted() {
        return states.size() <= 1;
    }

    /**
     * Generate cube set for current time.
     */
    public synchronized Set<Renderer.Cube> getCubes() {
        HashSet<Renderer.Cube> cubes = new HashSet<Renderer.Cube>();
        State s = states.getFirst();
        boolean empty = states.size() == 1;
        board(cubes, s.game.getBoard());
        for (Pawn p : s.game.getPawns()) {
            if (empty)
                pawn(cubes, p, p, delta);
            else {
                Pawn before = s.get(frame - 1, p);
                Pawn after = s.get(frame, p);
                pawn(cubes, before, after, delta);
            }
        }
        return cubes;
    }

    protected float interpolate(float a, float b, float x) {
        x = -2 * x * x * x + 3 * x * x;
        return a + (b - a) * x;
    }

    protected void healthbar(Set<Renderer.Cube> cubes, int health, int team, float px, float py, float pz) {
        // TODO Animator needs to handle more than 6 hp?
        // TODO Animator needs to support more than 2 teams
        // TODO Animator fades hp cubes out when damaged?
        final float size = 0.1f, offset = 0.15f;
        float tr, tg, tb;
        if (team == 0) {
            tr = 0.8f;
            tg = tb = 0.2f;
        } else {
            tr = tg = 0.2f;
            tb = 0.8f;
        }
        for (int i = 0; i < health; ++i) {
            float delta = (i - (health - 1) * 0.5f) * offset;
            cubes.add(new Renderer.Cube(px + delta, py, pz + 0.5f, size, size, size, tr, tg, tb));
        }
    }

    protected void pawn(Set<Renderer.Cube> cubes, Pawn before, Pawn after, float x) {
        float px = interpolate(before.getLocation().getX(), after.getLocation().getX(), x);
        float py = interpolate(before.getLocation().getY(), after.getLocation().getY(), x);
        float pz = 0.3f;
        final float rr = 179 / 255.0f, rg = 147 / 255.0f, rb = 73 / 255.0f;
        final float pr = 77 / 255.0f, pg = 158 / 255.0f, pb = 14 / 255.0f;
        final float sr = 132 / 255.0f, sg = 173 / 255.0f, sb = 166 / 255.0f;
        float r1, g1, b1, r2, g2, b2;
        switch (before.getStance()) {
            case Paper: r1 = pr; g1 = pg; b1 = pb; break;
            case Scissors: r1 = sr; g1 = sg; b1 = sb; break;
            default: r1 = rr; g1 = rg; b1 = rb;
        }
        float s;
        if (after.isDead()) {
            r2 = g2 = b2 = 0.3f;
            s = x >= 0.5f ? 0 : 0.8f * (0.5f - x);
        } else {
            switch (after.getStance()) {
                case Paper: r2 = pr; g2 = pg; b2 = pb; break;
                case Scissors: r2 = sr; g2 = sg; b2 = sb; break;
                default: r2 = rr; g2 = rg; b2 = rb;
            }
            s = 0.6f;
            healthbar(cubes, after.getHealth(), after.getTeam(), px, py, pz);
        }
        float r = interpolate(r1, r2, x);
        float g = interpolate(g1, g2, x);
        float b = interpolate(b1, b2, x);
        cubes.add(new Renderer.Cube(px, py, pz, s, s, s, r, g, b));
    }

    protected void tile(Set<Renderer.Cube> cubes, Vector location) {
        cubes.add(new Renderer.Cube(location.getX(), location.getY(), -0.1f, 0.9f, 0.9f, 0.2f, 0.5f, 0.5f, 0.5f));
    }

    protected void board(Set<Renderer.Cube> cubes, Board board) {
        for (Vector l : board.getTiles())
            tile(cubes, l);
    }

}
