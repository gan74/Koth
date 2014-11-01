
package koth.game;

/**
 * Encapsulate a set of rules (health, number of pawns...) used to tweak the game.
 */
public final class Rules {

    private final int pawns, actions, health, moveCost, stanceCost;
    private final Stance stance;

    /**
     * Create rules according to arguments.
     */
    public Rules(int pawns, int actions, int health, int moveCost, int stanceCost, Stance stance) {
        if (pawns <= 0 || actions <= 0 || health <= 0 || moveCost < 0 || stanceCost < 0)
            throw new IllegalArgumentException();
        if (stance == null)
            throw new NullPointerException();
        this.pawns = pawns;
        this.actions = actions;
        this.health = health;
        this.moveCost = moveCost;
        this.stanceCost = stanceCost;
        this.stance = stance;
    }

    /**
     * Create default rules for given number of pawns, actions and health points.
     * Move costs 1 point, stance change costs 2 points, starting stance is Rock.
     */
    public Rules(int pawns, int actions, int health) {
        this(pawns, actions, health, 1, 2, Stance.Rock);
    }

    /**
     * Create default rules for <code>N</code> pawns (<code>N+1</code> actions, <code>N+2</code> health points).
     */
    public Rules(int pawns) {
        this(pawns, pawns == 1 ? 8 : (pawns + 1), pawns + 2);
    }

    /**
     * Create default rules (3 pawns).
     */
    public Rules() {
        this(4);
    }

    /**
     * Get how many pawns are spawned.
     */
    public int getPawns() {
        return pawns;
    }

    /**
     * Get how many actions points an AI has for one turn.
     */
    public int getActions() {
        return actions;
    }

    /**
     * Get initial health of pawns.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Get cost for moving.
     */
    public int getMoveCost() {
        return moveCost;
    }

    /**
     * Get cost for changing stance.
     */
    public int getStanceCost() {
        return stanceCost;
    }

    /**
     * Get default stance for spawning pawns.
     */
    public Stance getStance() {
        return stance;
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || o.getClass() != Rules.class) && equals((Rules)o);
    }

    public boolean equals(Rules o) {
        return o != null && pawns == o.pawns && actions == o.actions && health == o.health &&
            moveCost == o.moveCost && stanceCost == o.stanceCost && stance == o.stance;
    }

    @Override
    public int hashCode() {
        return pawns ^ actions ^ health ^ moveCost ^ stanceCost ^ stance.hashCode();
    }

    @Override
    public String toString() {
        return "Rules{" + "pawns=" + pawns + ", actions=" + actions + ", health=" + health +
            ", moveCost=" + moveCost + ", stanceCost=" + stanceCost + ", stance=" + stance + "}";
    }

}
