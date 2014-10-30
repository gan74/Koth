
package koth.game;

/**
 * An Artificial Intelligence chooses actions for its pawns.
 */
public interface AI {

    /**
     * Called before game starts, used to prepare some data according to current rules.
     */
    public void initialize(Game game, int team, Rules rules);

    /**
     * Given the actual state and remaining action points, return the next move to perform. If <code>null</code> is returned, the turn ends.
     */
    public Action play(Game game, int actions);

}
