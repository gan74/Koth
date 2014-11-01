
package koth.user.jlb;

import koth.game.*;
import koth.util.Vector;
import koth.util.Algorithms;

import java.util.*;

/**
 * This AI tries to avoid enemies.
 */
public class Flee implements AI {

    private int team;
    private Map<Vector, Map<Vector, Integer>> distances;

    @Override
    public void initialize(Game game, int team, Rules rules) {
        this.team = team;
        distances = Algorithms.distances(game.getBoard().getTiles());
    }

    @Override
    public Action play(Game game, int actions) {
        // Get enemies
        Set<Pawn> foes = new HashSet<Pawn>();
        for (Pawn p : game.getPawns())
            if (p.getTeam() != team)
                foes.add(p);
        // Get minimal distance for all ally to enemies
        List<Pawn> allies = game.getPawnList();
        List<Integer> dists = new ArrayList<Integer>();
        for (Pawn a : allies)
            dists.add(distanceToFoes(a.getLocation(), foes));
        Algorithms.sort(Algorithms.ReversedComparator.<Integer>getInstance(), dists, allies);
        // Most endangered pawn must flee!
        for (int i = 0; i < allies.size(); ++i) {
            Pawn a = allies.get(i);
            for (Move m : Move.getNonzeros()) {
                Vector t = a.getLocation().add(m);
                if (game.isFree(t) && distanceToFoes(t, foes) > dists.get(i))
                    return new Action(a, m);
            }
        }
        // We can't flee anymore :'(
        return null;
    }

    private int distanceToFoes(Vector loc, Set<Pawn> foes) {
        int min = Integer.MAX_VALUE;
        for (Pawn f : foes) {
            int d = distances.get(loc).get(f.getLocation());
            if (d < min)
                min = d;
        }
        return min;
    }

}
