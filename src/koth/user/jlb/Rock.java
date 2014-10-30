
package koth.user.jlb;

import koth.game.*;

/**
 * This dummy AI does not move and always choose Rock stance.
 */
public class Rock implements AI {

    private int team;

    @Override
    public void initialize(Game game, int team, Rules rules) {
        // We only care about our team index
        this.team = team;
    }

    @Override
    public Action play(Game game, int actions) {
        // Find if there is any pawn that is not a big rock
        for (Pawn p : game.getPawns(team))
            if (p.getStance() != Stance.Rock)
                return new Action(p, Stance.Rock);
        // Good, skip turn
        return null;
    }

}
