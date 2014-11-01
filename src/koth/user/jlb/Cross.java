
package koth.user.jlb;

import koth.game.Board;
import koth.game.Generator;
import koth.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Create a crosshair-like board
 *
 * <pre>{@code
 *   .............
 *   .............
 *   ..    .    ..
 *   ..    .    ..
 *   ..  .....  ..
 *   ..  .....  ..
 *   .............
 *   ..  .....  ..
 *   ..  .....  ..
 *   ..    .    ..
 *   ..    .    ..
 *   .............
 *   .............
 * }</pre>
 */
public class Cross implements Generator {

    private static int at(int x, int y, int outer, int inner, int bridgeWidth, int bridgeLength) {
        // Symmetric
        int ax = Math.abs(x), ay = Math.abs(y);
        // Inner platform
        if (ax <= inner && ay <= inner)
            return 0;
        // Outside
        if (ax > inner + bridgeLength + outer || ay > inner + bridgeLength + outer)
            return -1;
        // Outer ring
        if (ax > inner + bridgeLength || ay > inner + bridgeLength) {
            if (ax <= inner + bridgeLength) {
                if (y < 0)
                    return 3;
                return 4;
            }
            if (ay <= inner + bridgeLength) {
                if (x < 0)
                    return 1;
                return 2;
            }
            return 0;
        }
        // Bridge
        if (ax <= bridgeWidth || ay <= bridgeWidth)
            return 0;
        return -1;
    }

    public static Board create(int teams, int outer, int inner, int bridgeWidth, int bridgeLength) {
        if (teams > 4 || outer <= 0 || inner < 0 || bridgeWidth < 0 || bridgeLength < 0 || bridgeWidth > inner)
            throw new IllegalArgumentException();
        Set<Vector> tiles = new HashSet<Vector>();
        List<Set<Vector>> spawns = new ArrayList<Set<Vector>>();
        for (int i = 0; i < 4; ++i)
            spawns.add(new HashSet<Vector>());
        int n = inner + bridgeLength + outer;
        for (int x = -n; x <= n; ++x)
            for (int y = -n; y <= n; ++y) {
                int style = at(x, y, outer, inner, bridgeWidth, bridgeLength);
                if (style < 0)
                    continue;
                if (style == 0)
                    tiles.add(new Vector(x, y));
                else
                    spawns.get(style - 1).add(new Vector(x, y));
            }
        return new Board(tiles, spawns);
    }

    @Override
    public Board create(int teams) {
        return create(teams, 2, 2, 0, 2);
    }

}
