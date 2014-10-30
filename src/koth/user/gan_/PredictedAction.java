package koth.user.gan_;

import java.util.List;

import koth.game.*;

public class PredictedAction {
	private Stance stance;
	private Vector pos;
	private Move move;
	
	public PredictedAction(Stance s, Vector p, Move m) {
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
		List<Vector> path = context.path(p.getLocation(), pos);
		int cost = path.size() * context.getRules().getMoveCost();
		if(stance != p.getStance()) {
			cost += context.getRules().getStanceCost();
		}
		return cost;
	}
	
	public PawnAction toPawnAction(GameContext c, Pawn p) {
		return new PawnAction(c, p, this);
	}
	
	@Override
	public String toString() {
		return "{" + stance + ", " + move + ", " + pos + "}";
	}
}
