
package koth.game;

/**
 * A <code>Generator</code> creates a <code>Board</code>.
 */
public interface Generator {

    /**
     * Given the number of teams, generates a new Board.
     */
    public Board create(int teams);

}
