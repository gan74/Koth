package koth.user.gan_;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import koth.game.*;

public class Herp implements AI {
	private GameContext context;
	private boolean printSt = true;

	@Override
	public void initialize(Game game, int team, Rules rules) {
		context = new GameContext(game, rules, team);
	}

	@Override
	public Action play(Game game, int actions) {
		context.update(game);
		
		Set<Pawn> enemies = context.getEnemies();
		Action action = computeMoves(context, context.killActions(enemies), actions);
		if(action == null) {
			List<? extends PotencialAction> acc = context.hurtActions(enemies);
			//System.err.println(acc.size());
			action = computeMoves(context, acc, actions);
		}
		if(action == null) {
			action = reposition(context, enemies);
		}
		return stupidityFilter(action);
	}
	
	private Action stupidityFilter(Action ac) {
		if(ac != null && context.getGame().isVoid(ac.getPawn().getLocation().add(ac.getMove()))) {
			//System.err.println("/!\\ WHAT ARE YOU DOING ?");
			if(printSt) {
				StackTraceElement[] st = (new Exception()).getStackTrace();
				System.err.print("Stupid move:");
				for(StackTraceElement trace : st) {
					if(trace.getClassName().contains("gan_.Herp") && !trace.getMethodName().equals("stupidityFilter")) {
						if(trace.getMethodName() == "play") {
							System.err.println("");
							break;
						}
						System.err.print("\t" + trace);
					}
					System.err.println("");
				}
				System.err.println(context.getGame());
			}
			return null;
		}
		return ac;
	}

	private Action computeMoves(GameContext context, List<? extends PotencialAction> actions, int actionPts) {
		for(Pawn pawn : context.getPawns()) {
			for(PawnActionSequence action : Utils.toPawnAction(context, pawn, actions)) {
				int cost = action.getActionCost();
				if(cost <= actionPts) {
					return stupidityFilter(action.toAction());
				}
			}
		}
		return null;
	}
	
	private Action reposition(GameContext context, Set<Pawn> enemies) {
		List<Pawn[]> targets = new ArrayList<>();
		for(Pawn pawn : context.getPawns()) {
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
			return stupidityFilter(AaaattttTTTTTTAAAaaacccCCCKKKK(context, enemies));
		}
		return stupidityFilter(new Action(thisTurn[0], context.goToward(thisTurn[0], thisTurn[1])));
	}
	
	private Action AaaattttTTTTTTAAAaaacccCCCKKKK(GameContext context, Set<Pawn> enemies) {
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
		for(Pawn p : context.getPawns()) {
			if(far == null || far.getLocation().manhattan(enemy.getLocation()) < p.getLocation().manhattan(enemy.getLocation())) {
				far = p;
			}
		}
		return stupidityFilter(new Action(far, context.goToward(far, enemy)));
	}
	
	
}
