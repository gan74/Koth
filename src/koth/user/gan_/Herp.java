package koth.user.gan_;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import koth.game.*;

public class Herp implements AI {
	
	private GameContext context;
	private Iterator<Action> actionSequence;
	private Priority actionPrio;
	
	private boolean printStackTrace = false;

	@Override
	public void initialize(Game game, int team, Rules rules) {
		context = new GameContext(game, rules, team);
	}

	@Override
	public Action play(Game game, int actions) {
		context.update(game);
		updateActionState(actions);
		if(actionSequence != null && actionSequence.hasNext()) {
			return stupidityFilter(actionSequence.next());
		}
		//System.err.println("No action !");
		return null;
	}
	
	private void updateActionState(int actions) {
		PrioritizedActionSequence seq = computeActionSequence(actions);
		if(seq != null && (actionSequence == null || !actionSequence.hasNext() || seq.getPriority().compareTo(actionPrio) <= 0)) {
			actionSequence = new ClampedIterator<>(seq.getActionSequence().iterator(), seq.getPriority().clamped(actions));
			actionPrio = seq.getPriority();
			//System.out.println("[" + Utils.getTeamName(context.getTeam()) + "] " + actionPrio + "s !");
		}
	}
	
	private PrioritizedActionSequence computeActionSequence(int actions) {
		Set<Pawn> enemies = context.getEnemies();
		PawnActionSequence seq = computeMoves(context, context.killActions(enemies), actions);
		if(seq != null) {
			return new PrioritizedActionSequence(seq, Priority.Kill);
		}
		
		seq = computeMoves(context, context.hurtActions(enemies), actions);
		if(seq != null) {
			return new PrioritizedActionSequence(seq, Priority.Hurt);
		}
		
		seq = reposition(context, enemies);
		if(seq != null) {
			return new PrioritizedActionSequence(seq, Priority.Move);
		}
		//System.err.println("No action !");
		return null;
	}

	private PawnActionSequence computeMoves(GameContext context, List<? extends PotencialAction> actions, int actionPts) {
		for(Pawn pawn : context.getTeamPawns()) {
			for(PawnActionSequence actionSeq : Utils.toPawnActionSequence(context, pawn, actions)) {
				int cost = actionSeq.getActionCost();
				if(cost <= actionPts) {
					return actionSeq;
				}
			}
		}
		return null;
	}
	
	private PawnActionSequence reposition(GameContext context, Set<Pawn> enemies) {
		return null;
	}
	
	private Action stupidityFilter(Action ac) {
		return ac;
	}
	
	
	/*private PawnActionSequence reposition(GameContext context, Set<Pawn> enemies) {
		List<Pawn[]> targets = new ArrayList<>();
		for(Pawn pawn : context.getTeamPawns()) {
			Pawn target = null;
			for(Pawn enemy : enemies) {
				if(target == null || pawn.getLocation().manhattan(enemy.getLocation()) > pawn.getLocation().manhattan(target.getLocation())) {
					target = enemy;
				}
			}
			targets.add(new Pawn[]{pawn, target});
		}
		Pawn[] thisTurn = null;
		for(Pawn[] pawns : targets) {
			if(thisTurn == null || thisTurn[0].getLocation().manhattan(thisTurn[1].getLocation()) < pawns[0].getLocation().manhattan(pawns[1].getLocation())) {
				thisTurn = pawns;
			}
		}
		if(thisTurn[0].getLocation().manhattan(thisTurn[1].getLocation()) > context.getBoardSize().sum() / 4) {
			return AaaattttTTTTTTAAAaaacccCCCKKKK(context, enemies);
		}
		return new PawnActionSequence(context, thisTurn[0], thisTurn[1].getLocation());
	}
	
	private PawnActionSequence AaaattttTTTTTTAAAaaacccCCCKKKK(GameContext context, Set<Pawn> enemies) {
		Vector avg = new Vector();
		for(Pawn e : enemies) {
			avg = avg.add(e.getLocation());
		}
		avg = avg.div(enemies.size());
		Pawn enemy = null;
		for(Pawn e : enemies) {
			if(enemy == null || e.getLocation().manhattan(avg) < enemy.getLocation().manhattan(avg)) {
				enemy = e;
			}
		}
		Pawn far = null;
		for(Pawn p : context.getTeamPawns()) {
			if(far == null || far.getLocation().manhattan(enemy.getLocation()) < p.getLocation().manhattan(enemy.getLocation())) {
				far = p;
			}
		}
		return new PawnActionSequence(context, far, enemy.getLocation());
	}
	
	
	private Action stupidityFilter(Action ac) {
		if(ac == null) {
			System.err.println("[" + Utils.getTeamName(context.getTeam()) + "] Skipping turn.");
			return null;
		}
		if(context.getGame().isVoid(ac.getPawn().getLocation().add(ac.getMove()))) {
			//System.err.println("/!\\ WHAT ARE YOU DOING ?");
			if(printStackTrace) {
				StackTraceElement[] st = (new Exception()).getStackTrace();
				System.err.print("[" + Utils.getTeamName(context.getTeam()) + "] Stupid move : " + ac.getMove());
				for(StackTraceElement trace : st) {
					if(trace.getClassName().contains("gan_.Herp") && !trace.getMethodName().equals("stupidityFilter")) {
						System.err.print("\t" + trace);
					}
					System.err.println("");
				}
				System.err.println(context.getGame());
			}
			return ac;
		}
		return ac;
	}*/
}
