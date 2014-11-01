package koth.user.gan_;

import koth.game.*;
import koth.util.*;

public class PotentialAction {
	private Stance stance;
	private Vector pos;
	private Move move;
	
	public PotentialAction(Stance s, Vector p, Move m) {
		stance = s;
		pos = p;
		move = m;
	}
	
	public Stance getStance() {
		return stance;
	}
	
	public Vector getPos() {
		return pos;
	}
	
	public Move getMove() {
		return move;
	}
	
	public ActionSequence toPawnAction(GameContext c, Pawn p) {
		return new ActionSequence(c, c.getGame().getPawn(p), this);
	}
	
	@Override
	public String toString() {
		return "{" + stance + ", " + move + ", " + pos + "}";
	}
}
