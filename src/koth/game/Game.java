
package koth.game;

import java.util.*;

/**
 * Represent the status of a game, including the board and living pawns.
 */
public final class Game {

    /**
     * Callback functions, used during update.
     */
    public static interface Listener {

        /**
         * Called when a pawn is updated (moved, stance changed, damaged).
         */
        public void updated(Pawn before, Pawn after);

        /**
         * Called at the end of a simultaneous chain of events.
         */
        public void frame();

        /**
         * Called when update is complete.
         */
        public void done(Game before, Game after);

    }

    private final Board board;
    private final Set<Pawn> pawns;
    private final Map<Vector, Pawn> coords;
    private final Map<Integer, Set<Pawn>> teams;

    /**
     * Creates a new game state, with given board and pawns.
     * Dead pawns are silently discarded.
     * @throws java.lang.IllegalArgumentException if more than one pawn is at a given location
     */
    public Game(Board board, Set<Pawn> pawns) {
        if (board == null || pawns == null)
            throw new NullPointerException();
        this.board = board;
        // Filter dead pawns
        Set<Pawn> pbuf = new HashSet<Pawn>();
        for (Pawn p : pawns)
            if (p.isAlive())
                pbuf.add(p);
        this.pawns = Collections.unmodifiableSet(new HashSet<Pawn>(pbuf));
        // Map coordinates
        Map<Vector, Pawn> cbuf = new HashMap<Vector, Pawn>();
        for (Pawn p : pbuf)
            if (cbuf.containsKey(p.getLocation()))
                throw new IllegalArgumentException("More than one pawn at " + p.getLocation());
            else
                cbuf.put(p.getLocation(), p);
        coords = Collections.unmodifiableMap(cbuf);
        // Build teams
        Map<Integer, Set<Pawn>> tbuf = new HashMap<Integer, Set<Pawn>>();
        for (Pawn p : pbuf) {
            Set<Pawn> s = tbuf.get(p.getTeam());
            if (s == null) {
                s = new HashSet<Pawn>();
                tbuf.put(p.getTeam(), s);
            }
            s.add(p);
        }
        Map<Integer, Set<Pawn>> team = new HashMap<Integer, Set<Pawn>>();
        for (Map.Entry<Integer, Set<Pawn>> e : tbuf.entrySet())
            team.put(e.getKey(), Collections.unmodifiableSet(e.getValue()));
        this.teams = Collections.unmodifiableMap(team);
    }

    /**
     * Get underlying board.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Get whether specified location is a tile.
     */
    public boolean isTile(Vector location) {
        return board.isTile(location);
    }

    /**
     * Get whether specified location is not a tile (i.e. out of the board).
     */
    public boolean isVoid(Vector location) {
        return board.isVoid(location);
    }

    /**
     * Get a set of living pawns.
     */
    public Set<Pawn> getPawns() {
        return pawns;
    }

    /**
     * Get a mapping between locations and pawns.
     */
    public Map<Vector, Pawn> getCoords() {
        return coords;
    }

    /**
     * Get current version of given pawn (i.e. the pawn with same team, same id).
     */
    public Pawn getPawn(Pawn equivalent) {
        return getPawn(pawns, equivalent);
    }

    /**
     * Get pawn at given location (or null if none).
     */
    public Pawn getPawn(Vector location) {
        return coords.get(location);
    }

    /**
     * Get whether there is a pawn at given location.
     */
    public boolean hasPawn(Vector location) {
        return getPawn(location) != null;
    }

    /**
     * Get a mapping between team ids and pawns.
     */
    public Map<Integer, Set<Pawn>> getTeams() {
        return teams;
    }

    /**
     * Get how many (non-empty) teams are in game.
     */
    public int getTeamCount() {
        return teams.size();
    }

    /**
     * Get ordered team list (sorted by team id).
     */
    public List<Set<Pawn>> getTeamList() {
        List<Set<Pawn>> lst = new ArrayList<Set<Pawn>>(teams.values());
        Collections.sort(lst, new Comparator<Set<Pawn>>() {
            @Override
            public int compare(Set<Pawn> a, Set<Pawn> b) {
                return Integer.compare(a.iterator().next().getTeam(), b.iterator().next().getTeam());
            }
        });
        return lst;
    }

    /**
     * Get team from ordered list.
     */
    public Set<Pawn> getTeamByIndex(int index) {
        List<Set<Pawn>> lst = getTeamList();
        return index >= 0 && index < lst.size() ? lst.get(index) : null;
    }

    /**
     * Get a set of living pawns of specified team.
     */
    public Set<Pawn> getPawns(int team) {
        Set<Pawn> result = teams.get(team);
        return result == null ? Collections.<Pawn>emptySet() : result;
    }

    /**
     * Get how many pawns are in game.
     */
    public int getPawnCount() {
        return pawns.size();
    }

    /**
     * Get how many pawns are in given team.
     */
    public int getPawnCount(int team) {
        return getPawns(team).size();
    }

    /**
     * Get ordered pawn list (sorted by team and id).
     */
    public List<Pawn> getPawnList() {
        List<Pawn> lst = new ArrayList<Pawn>(pawns);
        Collections.sort(lst);
        return lst;
    }

    /**
     * Get ordered pawn list for given team (sorted by id).
     */
    public List<Pawn> getPawnList(int team) {
        List<Pawn> lst = new ArrayList<Pawn>(getPawns(team));
        Collections.sort(lst);
        return lst;
    }

    /**
     * Get pawn from ordered list.
     */
    public Pawn getPawnByIndex(int index) {
        List<Pawn> lst = getPawnList();
        return index >= 0 && index < lst.size() ? lst.get(index) : null;
    }

    /**
     * Get pawn from ordered list.
     */
    public Pawn getPawnByIndex(int team, int index) {
        List<Pawn> lst = getPawnList(team);
        return index >= 0 && index < lst.size() ? lst.get(index) : null;
    }

    /**
     * If game has ended, get the winning team (-1 otherwise).
     */
    public int getWinner() {
        return teams.size() == 1 ? teams.keySet().iterator().next() : -1;
    }

    /**
     * Get whether this game has a winner.
     */
    public boolean hasWinner() {
        return getWinner() >= 0;
    }

    /**
     * Get whether this game is finished (may not have a winner).
     */
    public boolean isFinished() {
        return teams.size() <= 1;
    }

    /**
     * Get whether this game is not finished.
     */
    public boolean isPlaying() {
        return !isFinished();
    }

    /**
     * Get whether this game is finished and has no winner.
     */
    public boolean isDraw() {
        return teams.isEmpty();
    }

    /**
     * Get pawn at given location.
     */
    public static Pawn getPawn(Set<Pawn> pawns, Vector l) {
        for (Pawn p : pawns)
            if (p.getLocation().equals(l))
                return p;
        return null;
    }

    /**
     * Get the equivalent of <code>e</code> in <code>pawns</code>.
     */
    public static Pawn getPawn(Set<Pawn> pawns, Pawn e) {
        for (Pawn p : pawns)
            if (p.equals(e))
                return p;
        return null;
    }

    private void move(Listener listener, HashSet<Pawn> pawns, Pawn pawn, Move move) {
        // 1. Test if out of world -> Dies instantly
        Vector dest = pawn.getLocation().add(move.getDelta());
        Pawn next = pawn;
        Pawn target = null;
        if (board.isVoid(dest))
            next = next.damaged(next.getHealth());
        else {
            // 2. Check if there is an ennemy -> fight him
            target = getPawn(pawns, dest);
            if (target != null) {
                // 2.1. Status quo -> Both are pushed
                if (next.getStance() == target.getStance()) {
                    move(listener, pawns, target, move);
                // 2.2. Strong vs weak -> Target is damaged
                } else if (next.getStance().getWeak() == target.getStance()) {
                    Pawn nextTarget = target.damaged(1);
                    pawns.remove(target);
                    if (nextTarget.isAlive())
                        pawns.add(nextTarget);
                    if (listener != null)
                        listener.updated(target, nextTarget);
                // 2.3. Weak vs strong -> Pawn is damaged, target is pushed twice
                } else {
                    move(listener, pawns, target, move);
                    target = getPawn(pawns, target);
                    if (target != null)
                        move(listener, pawns, target, move);
                    next = next.damaged(1);
                }
                target = getPawn(pawns, dest);
            }
        }
        // 4. If the tile is free, move
        if (target == null)
            next = next.teleported(dest);
        // 5. Register events and update pawns
        if (listener != null)
            listener.updated(pawn, next);
        pawns.remove(next);
        if (next.isAlive())
            pawns.add(next);
    }

    /**
     * Compute an updated version of this game, after specified action.
     */
    public Game updated(Action action, Listener listener) {
        // 1. Safety checks
        if (action == null)
            throw new NullPointerException();
        Pawn pawn = getPawn(action.getPawn());
        if (pawn == null)
            throw new IllegalArgumentException("Pawn doesn't exists in this game");
        // 2. Check if some change is required
        Game sequel = this;
        if (action.getMove() != Move.None || action.getStance() != pawn.getStance()) {
            HashSet<Pawn> pawns = new HashSet<Pawn>(this.pawns);
            if (pawn.getStance() != action.getStance()) { // TODO improve this, add more frame (for instance, when a pawn falls)
                Pawn tmp = pawn;
                pawn = pawn.changed(action.getStance());
                if (listener != null) {
                    listener.updated(tmp, pawn);
                    listener.frame();
                }
            }
            Pawn next = pawn;
            // 3.1. Check if no movement is required
            if (action.getMove() == Move.None) {
                pawns.remove(pawn);
                pawns.add(next);
                if (listener != null)
                    listener.updated(pawn, next);
            // 3.2. Recursive computation of moves
            } else
                move(listener, pawns, next, action.getMove());
            sequel = new Game(board, pawns);
        }
        // Notify event and return
        if (listener != null) {
            listener.frame();
            listener.done(this, sequel);
        }
        return sequel;
    }

    /**
     * Compute an updated version of this game, after specified action.
     */
    public Game updated(Action action) {
        return updated(action, null);
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || o.getClass() != Game.class) && equals((Game)o);
    }

    public boolean equals(Game o) {
        return o != null && board.equals(o.board) && pawns.equals(o.pawns);
    }

    @Override
    public int hashCode() {
        return pawns.hashCode();
    }

    @Override
    public String toString() {
        String txt = "";
        for (int y = board.getMax().getY(); y >= board.getMin().getY(); --y) {
            for (int x = board.getMin().getX(); x <= board.getMax().getX(); ++x) {
                char c = ' ';
                Vector l = new Vector(x, y);
                Pawn p = getPawn(l);
                if (p != null) {
                    assert p.getTeam() >= 0 && p.getTeam() < 10;
                    c = (char)(p.getTeam() + '0');
                } else if (board.isTile(l))
                    c = '.';
                txt += c;
            }
            txt += "\r\n";
        }
        return txt;
    }

}
