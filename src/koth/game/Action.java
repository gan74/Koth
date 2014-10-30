
package koth.game;

/**
 * Represent a single action (move and stance modifier) for a given pawn.
 */
public final class Action {

    private final Pawn pawn;
    private final Stance stance;
    private final Move move;

    /**
     * Create a new action.
     */
    public Action(Pawn pawn, Stance stance, Move move) {
        if (pawn == null || stance == null || move == null)
            throw new NullPointerException();
        this.pawn = pawn;
        this.stance = stance;
        this.move = move;
    }

    /**
     * Create a new action, with no movement.
     */
    public Action(Pawn pawn, Stance stance) {
        this(pawn, stance, Move.None);
    }

    /**
     * Create a new action, without changing stance.
     */
    public Action(Pawn pawn, Move move) {
        this(pawn, pawn.getStance(), move);
    }

    /**
     * Get associated pawn.
     */
    public Pawn getPawn() {
        return pawn;
    }

    /**
     * Get associated stance.
     */
    public Stance getStance() {
        return stance;
    }

    /**
     * Get associated move direction.
     */
    public Move getMove() {
        return move;
    }

    /**
     * Create a copy of this action, associated to another pawn.
     */
    public Action with(Pawn pawn) {
        return new Action(pawn, stance, move);
    }

    /**
     * Create a copy of this action, associated to another stance.
     */
    public Action with(Stance stance) {
        return new Action(pawn, stance, move);
    }

    /**
     * Create a copy of this action, associated to another move direction.
     */
    public Action with(Move move) {
        return new Action(pawn, stance, move);
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || o.getClass() != Action.class) && equals((Action)o);
    }

    public boolean equals(Action o) {
        return o != null && pawn.equals(o.pawn) && stance.equals(o.stance) && move.equals(o.move);
    }

    @Override
    public int hashCode() {
        return 31 * pawn.hashCode() + (31 * stance.hashCode() + move.hashCode());
    }

    @Override
    public String toString() {
        return pawn + " " + stance + " " + move;
    }

}
