package koth.user.gan_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import koth.game.*;
import koth.util.*;

public class ActionSequence implements Comparable<ActionSequence>, Iterable<Action> {
	private GameContext context;
	private ArrayList<Action> actions;
	
	public ActionSequence(GameContext c, ArrayList<Action> ac) {
		context = c;
		actions = new ArrayList<>();
		actions.addAll(ac);
	}
	
	public ActionSequence(GameContext c, Pawn pawn, PotencialAction a) {
		context = c;
		pawn = context.getGame().getPawn(pawn);
		Path path = context.path(pawn.getLocation(), a.getPos(), context.getTeamPawns());
		actions = new ArrayList<>();
		for(Move m : path) {
			actions.add(new Action(pawn, m));
		}
		if(!a.getStance().equals(pawn.getStance())) {
			actions.add(new Action(pawn, a.getStance()));
		}
		if(!a.getMove().equals(Move.None)) {
			actions.add(new Action(pawn, a.getMove()));
		}
	}
	
	public ActionSequence(GameContext c, Pawn pawn, Vector goal, boolean removeLast) {
		context = c;
		pawn = context.getGame().getPawn(pawn);
		Path path = context.path(pawn.getLocation(), goal, context.getTeamPawns());
		actions = new ArrayList<>();
		for(Move m : path) {
			actions.add(new Action(pawn, m));
		}
		if(removeLast && actions.size() > 0) {
			actions.remove(actions.size() - 1);
		}
	}
	
	public ActionSequence(GameContext c, Pawn pawn, Vector goal) {
		this(c, pawn, goal, false);
	}
	
	public int getCost() {
		int cost = 0;
		Map<Pawn, Stance> map = new HashMap<>();
		for(Pawn p : context.getGame().getPawns()) {
			map.put(p, p.getStance());
		}
		for(Action a : actions) {
			Stance st = map.get(a.getPawn());
			if(!a.getStance().equals(st)) {
				cost += context.getRules().getStanceCost();
				map.put(a.getPawn(), a.getStance());
			}
			if(!a.getMove().equals(Move.None)) {
				cost += context.getRules().getMoveCost();
			}
		}
		return cost; 
	}
	

	@Override
	public int compareTo(ActionSequence o) {
		return -Float.compare(o.getCost(), getCost());
	}

	@Override
	public Iterator<Action> iterator() {
		return actions.iterator();
	}

}
