package koth.user.gan_;

import koth.game.*;

public class PotencialAction {
	private Stance stance;
	private Vector pos;
	private Move move;
	
	public PotencialAction(Stance s, Vector p, Move m) {
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
	
	public int getCost(GameContext context, Pawn p) {
		Path path = context.path(p.getLocation(), pos);
		int cost = path.size() * context.getRules().getMoveCost();
		if(stance != p.getStance()) {
			cost += context.getRules().getStanceCost();
		}
		return cost;
	}
	
	public PawnActionSequence toPawnAction(GameContext c, Pawn p) {
		return new PawnActionSequence(c, p, this);
	}
	
	@Override
	public String toString() {
		return "{" + stance + ", " + move + ", " + pos + "}";
	}
}