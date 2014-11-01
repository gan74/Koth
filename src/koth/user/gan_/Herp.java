package koth.user.gan_;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import koth.game.*;
import koth.util.*;

public class Herp implements AI {
	
	private GameContext context;
    private Map<Vector, Map<Vector, Integer>> distances;

	private Iterator<Action> actionSequence;
	
	private boolean printStackTrace = false;

	@Override
	public void initialize(Game game, int team, Rules rules) {
		context = new GameContext(game, rules, team);
		distances = Algorithms.distances(game.getBoard().getTiles());
	}

	@Override
	public Action play(Game game, int actions) {
		if(isBegin(actions)) {
			begin();
		}
		if(actionSequence == null) {
			tryKill(actions);
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return next();
	}
	
	private boolean isBegin(int actions) {
		return actions == context.getRules().getActions();
	}
	
	private void begin() {
		actionSequence = null;
	}
	
	private boolean tryKill(int actions) {
		List<PotentialAction> potentialKills = context.killActions(context.getEnemies());
		List<ActionSequence> killSequences = new ArrayList<>();
		for(Pawn pawn : context.getTeamPawns()) {
			for(PotentialAction k : potentialKills) {
				ActionSequence seq = k.toPawnAction(context, pawn);
				if(seq.getCost() <= actions) {
					killSequences.add(seq);
				}
			}
		}
		ActionSequence best = null;
		for(ActionSequence seq : killSequences) {
			if(best == null || seq.getCost() < best.getCost()) {
				best = seq;
			}
		}
		if(best != null) {
			actionSequence = best.iterator();
			return true;
		}
		return false;
	}
	
	
	private Action next() {
		if(actionSequence == null) {
			return null;
		}
		Action a = null;
		if(actionSequence.hasNext()) {
			a = actionSequence.next();
		}
		if(!actionSequence.hasNext()) {
			actionSequence = null;
		}
		return a;
	}
}
