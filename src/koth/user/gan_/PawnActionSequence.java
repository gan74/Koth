package koth.user.gan_;

import java.util.ArrayList;
import java.util.Iterator;

import koth.game.*;

public class PawnActionSequence implements Comparable<PawnActionSequence>, Iterable<Action> {
	
	private PotencialAction action;
	private Path path;
	private Pawn pawn;
	private GameContext context;
	private float correction;
	
	public PawnActionSequence(GameContext c, Pawn p, PotencialAction a) {
		pawn = p;
		action = a;
		context = c;
		path = context.path(pawn.getLocation(), a.getPos(), context.getTeamPawns());
		computeCorrection();
	}
	
	public PawnActionSequence(GameContext c, Pawn p, Vector goal) {
		pawn = p;
		context = c;
		path = context.path(pawn.getLocation(), goal, context.getTeamPawns());
		computeCorrection();
	}
	
	public int getActionCost() {
		int cost = (path.size() + 1) * context.getRules().getMoveCost();
		if(action != null && action.getStance() != pawn.getStance()) {
			cost += context.getRules().getStanceCost();
		}
		return cost;
	}
	
	public float getCorrectionFactor() {
		return correction;
	}
	
	public float getCorrectedCost() {
		return getActionCost() + correction;
	}
	
	public Vector getFinalPos() {
		return pawn.getLocation().add(action == null ? Move.None : action.getMove());
	}

	@Override
	public int compareTo(PawnActionSequence o) {
		return -Float.compare(o.getCorrectedCost(), getCorrectedCost());
	}

	@Override
	public Iterator<Action> iterator() {
		ArrayList<Action> actions = new ArrayList<>();
		for(Move m : path) {
			actions.add(new Action(pawn, m));
		}
		if(action != null) {
			if(pawn.getStance() != action.getStance()) {
				actions.add(new Action(pawn, action.getStance()));
			}
			actions.add(new Action(pawn, action.getMove()));
		}
		return actions.iterator();
	}
	
	@Override
	public String toString() {
		String str = "{\n";
		for(Move m : path) {
			str = str + "  " + m + "\n";
		}
		return str + "} " + action == null ? "" : ("=> (" + (action.getStance().equals(pawn.getStance()) ? "#" : action.getStance()) + " => " + action.getMove() + ")");
	}
	
	private void computeCorrection() {
		correction = context.getCorrectionFactor(pawn, action == null ? Move.None : action.getMove());
		Pawn target = context.getGame().getPawn(getFinalPos());
		if(target != null) {
			if(target.getStance().equals((action == null ? pawn.getStance() : action.getStance()).getStrong())) {
				correction += context.getHurtCorrection();
				if(pawn.getHealth() < 2) {
					correction += context.getDeathCorrection();
				}
			}
		}
		if(correction < context.getDeathCorrection() && context.getGame().isVoid(getFinalPos())) {
			correction += context.getDeathCorrection();
		}
	}

}
