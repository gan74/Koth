
package koth.game;

import koth.util.Vector;

/**
 * Store informations about a pawn.
 * A pawn is uniquely defined by a team and an id (i.e. two pawns are equals if they have the same teams/ids).
 */
public final class Pawn implements Comparable<Pawn> {

    private final int team, id;
    private final Vector location;
    private final Stance stance;
    private final int health;

    /**
     * Create a new pawn with specified values.
     */
    public Pawn(int team, int id, Vector location, Stance stance, int health) {
        if (location == null || stance == null)
            throw new NullPointerException();
        this.team = team;
        this.id = id;
        this.location = location;
        this.stance = stance;
        this.health = health < 0 ? 0 : health;
    }

    /**
     * Create a new pawn with specified values.
     */
    public Pawn(int team, int id, Vector location, Rules rules) {
        this(team, id, location, rules.getStance(), rules.getHealth());
    }

    /**
     * Get associated team.
     */
    public int getTeam() {
        return team;
    }

    /**
     * Get identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Get current location.
     */
    public Vector getLocation() {
        return location;
    }

    /**
     * Get current stance.
     */
    public Stance getStance() {
        return stance;
    }

    /**
     * Get current health points.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Get whether this pawn is alive.
     */
    public boolean isAlive() {
        return getHealth() > 0;
    }

    /**
     * Get whether this pawn is dead.
     */
    public boolean isDead() {
        return !isAlive();
    }

    /**
     * Create a modified copy of this pawn, at another location.
     */
    public Pawn moved(Vector delta) {
        return delta.isZero() ? this : new Pawn(team, id, location.add(delta), stance, health);
    }

    /**
     * Create a modified copy of this pawn, at another location.
     */
    public Pawn teleported(Vector location) {
        return location.equals(this.location) ? this : new Pawn(team, id, location, stance, health);
    }

    /**
     * Create a modified copy of this pawn, using another stance.
     */
    public Pawn changed(Stance stance) {
        return stance == this.stance ? this : new Pawn(team, id, location, stance, health);
    }

    /**
     * Create a modified copy of this pawn, with less health points.
     */
    public Pawn damaged(int damage) {
        return damage == 0 ? this : new Pawn(team, id, location, stance, health - damage);
    }

    @Override
    public int compareTo(Pawn o) {
        int r = Integer.compare(team, o.team);
        if (r == 0)
            return Integer.compare(id, o.id);
        return r;
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || o.getClass() != Pawn.class) && equals((Pawn)o);
    }

    public boolean equals(Pawn o) {
        return o != null && team == o.team && id == o.id;
    }

    @Override
    public int hashCode() {
        return 31 * team + id;
    }

    @Override
    public String toString() {
        return team + ":" + id + "@" + location;
    }

}
