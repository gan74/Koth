package koth.user.gan_;

import koth.game.*;

public class PawnActionSequence implements Comparable<PawnActionSequence> {
	
	private PotencialAction action;
	private Pawn pawn;
	private GameContext context;
	private int cost;
	private float correction;
	
	public PawnActionSequence(GameContext c, Pawn p, PotencialAction a) {
		pawn = p;
		action = a;
		context = c;
		cost = action.getCost(context, pawn);
		correction = context.getCorrectionFactor(pawn, action.getMove());
		
		Pawn target = c.getGame().getPawn(getFinalPos());
		if(target != null) {
			if(target.getStance().equals(pawn.getStance().getStrong())) {
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
	
	public int getActionCost() {
		return cost;
	}
	
	public float getCorrectedCost() {
		return cost + correction;
	}
	
	public PotencialAction getPredicted() {
		return action;
	}
	
	public Action toAction() {
		if(pawn.getLocation().equals(action.getPos())) {
			return new Action(pawn, action.getStance(), action.getMove());
		}
		return new Action(pawn, action.getStance(), context.goToward(pawn.getLocation(), action.getPos()));
	}
	
	public Vector getFinalPos() {
		return pawn.getLocation().add(action.getMove());
	}

	@Override
	public int compareTo(PawnActionSequence o) {
		return -Float.compare(o.getCorrectedCost(), getCorrectedCost());
	}

}
