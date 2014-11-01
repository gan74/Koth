
package koth.game;

import koth.util.Vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represent a direction, for instance used to describe a movement.
 */
public enum Move {

    /**
     * No direction
     */
    None,

    /**
     * West direction (negative X-axis)
     */
    West,

    /**
     * North direction (positive Y-axis)
     */
    North,

    /**
     * East direction (positive X-axis)
     */
    East,

    /**
     * South direction (negative Y-axis)
     */
    South;

    private Move opposite;
    private Vector delta;

    private static List<Move> all, nonzeros;

    static {
        None.opposite = None;
        West.opposite = East;
        North.opposite = South;
        East.opposite = West;
        South.opposite = North;
        None.delta = new Vector();
        West.delta = new Vector(-1, 0);
        North.delta = new Vector(0, 1);
        East.delta = new Vector(1, 0);
        South.delta = new Vector(0, -1);
        all = Collections.unmodifiableList(Arrays.asList(None, West, North, East, South));
        nonzeros = Collections.unmodifiableList(Arrays.asList(West, North, East, South));
    }

    /**
     * Get the opposite direction.
     */
    public Move getOpposite() {
        return opposite;
    }

    /**
     * Get a <code>Vector</code> of length <code>1</code> (except for <code>None</code>) that represent the direction.
     */
    public Vector getDelta() {
        return delta;
    }

    /**
     * Get move associated to given direction.
     * For instance, <code>(3, 2)</code> returns <code>West</code>.
     * When X and Y coordinates are equals, Y-axis is preferred (i.e. <code>(1, 1)</code> returns <code>North</code>).
     */
    public static Move fromDirection(Vector dir) {
        if (dir.isZero())
            return None;
        int ax = Math.abs(dir.getX()), ay = Math.abs(dir.getY());
        if (ax > ay)
            return dir.getX() > 0 ? East : West;
        return dir.getY() > 0 ? North : South;
    }

    /**
     * Cast an integer to a direction.
     */
    public static Move fromInt(int i) {
        switch (i) {
            case 1: return West;
            case 2: return North;
            case 3: return East;
            case 4: return South;
            default: return None;
        }
    }

    /**
     * Cast a direction to an integer (equivalent to <code>ordinal()</code>).
     */
    public int toInt() {
        return ordinal();
    }

    /**
     * Get all moves.
     */
    public static List<Move> getAll() {
        return all;
    }

    /**
     * Get West, North, East and South.
     */
    public static List<Move> getNonzeros() {
        return nonzeros;
    }

}
