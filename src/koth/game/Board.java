
package koth.game;

import java.util.*;

/**
 * Represent a collection of tiles, which defines a game board.
 */
public final class Board {

    private final Set<Vector> tiles;
    private final List<Set<Vector>> spawns;
    private final Vector min, max;

    /**
     * Create a new board from given sets of tiles and spawn locations.
     * A tile can host multiple spawns.
     */
    public Board(Set<Vector> tiles, List<Set<Vector>> spawns) {
        Set<Vector> tilesCopy = tiles == null ? new HashSet<Vector>() : new HashSet<Vector>(tiles);
        List<Set<Vector>> spawnsCopy = new ArrayList<Set<Vector>>();
        if (spawns != null)
            for (Set<Vector> s : spawns)
                if (s == null)
                    spawnsCopy.add(new HashSet<Vector>());
                else {
                    spawnsCopy.add(new HashSet<Vector>(s));
                    tilesCopy.addAll(s);
                }
        this.tiles = Collections.unmodifiableSet(tilesCopy);
        this.spawns = Collections.unmodifiableList(spawnsCopy);
        if (this.tiles.isEmpty())
            min = max = new Vector();
        else {
            Iterator<Vector> it = this.tiles.iterator();
            Vector i = it.next(), min = i, max = i;
            while (it.hasNext()) {
                i = it.next();
                min = min.min(i);
                max = max.max(i);
            }
            this.min = min;
            this.max = max;
        }
    }

    /**
     * Get minimum corner (south-west) of bounding square.
     */
    public Vector getMin() {
        return min;
    }

    /**
     * Get maximum corner (north-east) of bounding square.
     */
    public Vector getMax() {
        return max;
    }

    /**
     * Get tiles locations.
     */
    public Set<Vector> getTiles() {
        return tiles;
    }

    /**
     * Get whether specified location is a tile.
     */
    public boolean isTile(Vector location) {
        return tiles.contains(location);
    }

    /**
     * Get whether specified location is not a tile (i.e. out of the board).
     */
    public boolean isVoid(Vector location) {
        return !isTile(location);
    }

    /**
     * Get a set of spawn locations for each team.
     */
    public List<Set<Vector>> getSpawns() {
        return spawns;
    }

    /**
     * Get a set of spawn locations for specified team.
     */
    public Set<Vector> getSpawns(int team) {
        return team < 0 || team >= spawns.size() ? Collections.<Vector>emptySet() : spawns.get(team);
    }

    /**
     * Get whether specified location is a spawn.
     */
    public boolean isSpawn(Vector location) {
        for (int i = 0; i < spawns.size(); ++i)
            if (spawns.get(i).contains(location))
                return true;
        return false;
    }

    // TODO pathfinding algorithms? other helpers?

    @Override
    public boolean equals(Object o) {
        return !(o == null || o.getClass() != Board.class) && equals((Board)o);
    }

    public boolean equals(Board o) {
        return o != null && tiles.equals(o.tiles) && spawns.equals(o.spawns);
    }

    @Override
    public int hashCode() {
        return tiles.hashCode();
    }

    @Override
    public String toString() {
        String txt = "";
        for (int y = max.getY(); y >= min.getY(); --y) {
            for (int x = min.getX(); x <= max.getX(); ++x)
                txt += isTile(new Vector(x, y)) ? '.' : ' ';
            txt += "\r\n";
        }
        return txt;
    }

}
