
package koth.system;

import koth.game.*;

// TODO documentation of Human

public class Human implements AI {

    private final Object lock = new Object();
    private Action action;

    public void execute(Action action) {
        synchronized (lock) {
            this.action = action;
            lock.notifyAll();
        }
    }

    @Override
    public void initialize(Game game, int team, Rules rules) {}

    @Override
    public Action play(Game game, int actions) {
        synchronized (lock) {
            try {
                while (action == null)
                    lock.wait();
            } catch (InterruptedException e) {
                return null;
            }
            Action result = action;
            action = null;
            return result;
        }
    }

}
