
package koth.user;

import koth.game.*;

/**
 * Proxy used by graphical interface to allow a user to play the game.
 */
public final class Human implements AI {

    private Action action;

    public void set(Action action) {
        this.action = action;
    }

    @Override
    public void initialize(Game game, int team, Rules rules) {}

    @Override
    public Action play(Game game, int actions) {
        return action;
    }

}
