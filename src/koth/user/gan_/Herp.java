package koth.user.gan_;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import koth.game.*;
import koth.util.*;

public class Herp implements AI {
	
	private GameContext context;
	private Iterator<Action> actionSequence;
	private Priority actionPrio;
    private Map<Vector, Map<Vector, Integer>> distances;
	
	private boolean printStackTrace = false;

	@Override
	public void initialize(Game game, int team, Rules rules) {
		context = new GameContext(game, rules, team);
		distances = Algorithms.distances(game.getBoard().getTiles());
	}

	@Override
	public Action play(Game game, int actions) {
		return null;
	}
}
