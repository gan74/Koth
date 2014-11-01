
package koth.user.jlb;

import koth.game.*;
import koth.util.Vector;

import java.util.*;

/**
 * Create a square board
 *
 * <pre>{@code
 *   .........
 *   .........
 *   .........
 *   .........
 *   .........
 *   .........
 *   .........
 *   .........
 *   .........
 * }</pre>
 */
public class Square implements Generator {

    public static Board create(int teams, int size) {
        if (size <= 0)
            throw new IllegalArgumentException();
        // Create tiles
        Set<Vector> tiles = new HashSet<Vector>();
        for (int x = 0; x < size; ++x)
            for (int y = 0; y < size; ++y)
                tiles.add(new Vector(x, y));
        // Add all tiles as spawns
        List<Set<Vector>> spawns = new ArrayList<Set<Vector>>();
        for (int i = 0; i < teams; ++i)
            spawns.add(tiles);
        return new Board(tiles, spawns);
    }

    @Override
    public Board create(int teams) {
        return create(teams, 9);
    }

}
