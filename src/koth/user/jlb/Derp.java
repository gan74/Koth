
package koth.user.jlb;

import koth.game.*;

import java.util.Random;

/**
 * This dummy AI chooses a random pawn, a random stance and a random direction.
 * To avoid early suicide, it tries not to walk into the deep void.
 */
public class Derp implements AI {

    private final Random random = new Random();
    private int team;

    @Override
    public void initialize(Game game, int team, Rules rules) {
        // We only care about our team index
        this.team = team;
    }

    @Override
    public Action play(Game game, int actions) {
        // Choose one pawn
        int index = random.nextInt(game.getPawnCount(team));
        Pawn pawn = game.getPawnByIndex(team, index);
        // Select a stance
        Stance stance = Stance.fromInt(random.nextInt(3));
        // Select a move, such that we do not fall in the void
        Move move;
        do {
            move = Move.fromInt(random.nextInt(5));
        } while (game.isVoid(pawn.getLocation().add(move.getDelta())));
        // Return action
        return new Action(pawn, stance, move);
    }

}
