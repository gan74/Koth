
package koth.system;

import koth.game.*;
import koth.game.Vector;
import koth.util.ClassManager;

import java.util.*;

/**
 * Encapsulate a game and AIs used to simulate a play.
 */
public final class Simulator {

    private Rules rules;
    private List<AI> ais;
    private Game initial, game;
    private List<Action> history;
    private int currentTeam, currentPoints;
    private int lastHealthSum, staleCount;
    private int turn;

    /**
     * Create a new simulator with specified board, AIs and rules.
     */
    public Simulator(Board board, List<AI> ais, Rules rules) {
        if (board == null || ais == null)
            throw new NullPointerException();
        this.rules = rules;
        List<List<Vector>> spawns = selectSpawns(board, ais.size(), rules);
        this.ais = Collections.unmodifiableList(new ArrayList<AI>(ais));
        Set<Pawn> pawns = new HashSet<Pawn>();
        for (int i = 0; i < this.ais.size(); ++i) {
            for (int j = 0; j < spawns.get(i).size(); ++j)
                pawns.add(new Pawn(i, j, spawns.get(i).get(j), rules.getStance(), rules.getHealth()));
        }
        game = initial = new Game(board, pawns);
        history = new LinkedList<Action>();
        currentTeam = 0;
        currentPoints = rules.getActions();
        lastHealthSum = computeHealthSum(game);
        staleCount = 0;
        turn = 0;
        for (int i = 0; i < this.ais.size(); ++i) {
            AI ai = this.ais.get(i);
            try {
                // TODO sandbox that in another thread (with a timeout)
                ai.initialize(game, i, rules);
            } catch (Exception e) {
                //System.err.println(i + " (" + ai + ") failed to initiate!");
                //e.printStackTrace();
            }
        }
    }

    /**
     * Create a new simulator with specified board, AIs and rules.
     */
    public Simulator(Generator generator, List<AI> ais, Rules rules) {
        this(generator.create(ais.size()), ais, rules);
    }

    /**
     * Create a new simulator with specified board, AIs and rules.
     */
    public Simulator(ClassManager.Factory<Generator> generator, List<ClassManager.Factory<AI>> ais, Rules rules) {
        this(generator.create(), ClassManager.create(ais), rules);
    }

    private List<List<Vector>> selectSpawns(Board board, int teams, Rules rules) {
        List<List<Vector>> spawns = new ArrayList<List<Vector>>();
        for (int t = 0; t < teams; ++t) {
            List<Vector> s = new ArrayList<Vector>(board.getSpawns(t));
            for (int i = 0; i < t; ++i)
                s.removeAll(spawns.get(i));
            Collections.shuffle(s);
            if (s.size() < rules.getPawns())
                throw new IllegalArgumentException("Not enough spawns for team " + t + " (" + spawns.size() + " available, " + rules.getPawns() + " required)");
            spawns.add(s.subList(0, rules.getPawns()));
        }
        return spawns;
    }

    /**
     * Get associated rules.
     */
    public Rules getRules() {
        return rules;
    }

    /**
     * Get in-game AIs.
     */
    public List<AI> getAis() {
        return ais;
    }

    /**
     * Get the original game state.
     */
    public Game getInitialGame() {
        return initial;
    }

    /**
     * Get the chronological list of actions.
     */
    public List<Action> getHistory() {
        return new ArrayList<Action>(history);
    }

    /**
     * Get current turn.
     */
    public int getTurn() {
        return turn;
    }

    /**
     * Get current game.
     */
    public Game getGame() {
        return game;
    }

    /**
     * Get current team (the one that will play at next <code>play()</code>).
     */
    public int getTeam() {
        return currentTeam;
    }

    /**
     * Get AI associated to current team.
     */
    public AI getAi() {
        return ais.get(currentTeam);
    }

    /**
     * Get how many points current team has left for this turn.
     */
    public int getPoints() {
        return currentPoints;
    }

    private int computeHealthSum(Game game) {
        int s = 0;
        for (Pawn p : game.getPawns())
            s += p.getHealth();
        return s;
    }

    /**
     * Ask the current team to play.
     */
    public void play(Game.Listener listener) {
        // TODO define logging system
        // If simulation is finished, do nothing
        if (game.isFinished())
            return;
        // Ask AI for an action
        AI ai = ais.get(currentTeam);
        Action action;
        try {
            // TODO sandbox that in another thread (with a timeout)
            action = ai.play(game, currentPoints);
        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println(currentTeam + " (" + ai + ") failed to play!");
            action = null;
        }
        //System.err.println("<");
        // Apply action (if enough points)
        if (action != null) {
            Pawn pawn = game.getPawn(action.getPawn());
            // Check if pawn exists
            if (pawn == null) {
                //System.err.println(currentTeam + " (" + ai + ") tried to play " + action + ", but this pawn does not exist!");
                action = null;
            // Check if pawn has correct team
            } else if (pawn.getTeam() != currentTeam) {
                //System.err.println(currentTeam + " (" + ai + ") tried to play " + action + ", but does not own this pawn!");
                action = null;
            } else {
                int cost = 0;
                // Moving costs
                if (action.getMove() != Move.None)
                    cost += rules.getMoveCost();
                // Changing stance costs
                if (pawn.getStance() != action.getStance())
                    cost += rules.getStanceCost();
                // To avoid infinite loop, at least one point is removed
                if (cost == 0)
                    cost = 1;
                // Check if enough action points
                if (cost > currentPoints) {
                    //System.err.println(currentTeam + " (" + ai + ") tried to play " + action + ", but has not enough action points!");
                    action = null;
                } else {
                    // Execute movement
                    //System.out.println(action);

                    game = game.updated(action, listener);
                    currentPoints -= cost;
                    history.add(action);
                    // Check for end
                    if (game.isFinished()) {

                        //System.out.println("Game finished (" + game.getWinner() + " wins)");
                        return;
                    }
                }
            }
        }
        // Decrease action points and switch team if points are exhausted (or an invalid action was issued)
        if (currentPoints <= 0 || action == null) {
            currentPoints = rules.getActions();
            do {
                ++currentTeam;
                if (currentTeam == ais.size()) {
                    currentTeam = 0;
                    ++turn;
                    // Check for idle game (to avoid infinite loop)
                    int sum = computeHealthSum(game);
                    if (sum != lastHealthSum) {
                        staleCount = 0;
                    } else if (++staleCount >= 100) { // TODO put this constant somewhere
                        game = new Game(game.getBoard(), new HashSet<Pawn>());
                        //System.out.println("Force draw, nothing happened for too long!");
                        return;
                    }
                    lastHealthSum = sum;
                }
            } while (game.getPawnCount(currentTeam) == 0);
        }
    }

    /**
     * Ask the current team to play.
     */
    public void play() {
        play(null);
    }

}
