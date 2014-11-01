
package koth.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represent a stance, for instance used to describe a pawn status.
 */
public enum Stance {

    /**
     * Rock stance, which beats scissors and is beaten by paper.
     */
    Rock,

    /**
     * Paper stance, which beats rock and is beaten by scissors.
     */
    Paper,

    /**
     * Scissors stance, which beats paper and is beaten by rock.
     */
    Scissors;

    private static final List<Stance> all = Collections.unmodifiableList(Arrays.asList(Rock, Paper, Scissors));

    /**
     * Get the associated weak stance (the one which is beated by current stance).
     */
    public Stance getWeak() {
        switch (this) {
            case Paper: return Rock;
            case Scissors: return Paper;
            default: return Scissors;
        }
    }

    /**
     * Get the associated strong stance (the one which beats current stance).
     */
    public Stance getStrong() {
        switch (this) {
            case Paper: return Scissors;
            case Scissors: return Rock;
            default: return Paper;
        }
    }

    /**
     * Cast an integer to a stance.
     */
    public static Stance fromInt(int i) {
        switch (i) {
            case 1: return Paper;
            case 2: return Scissors;
            default: return Rock;
        }
    }

    /**
     * Cast a stance to an integer (equivalent to <code>ordinal()</code>).
     */
    public int toInt() {
        return ordinal();
    }

    /**
     * Get all stances.
     */
    public static List<Stance> getAll() {
        return all;
    }
}
