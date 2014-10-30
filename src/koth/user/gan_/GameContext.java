package koth.user.gan_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import koth.game.*;

public class GameContext {
	private Game game;
	private Rules rules;
	private int team;
	private Map<Pawn, PawnData> pawnData;
	
	public GameContext(Game g, Rules r, int t) {
		pawnData = new HashMap<>();
		game = g;
		rules = r;
		team = t;
	}
	
	public void update(Game g) {
		game = g;
		for(Pawn p : game.getPawns(team)) {
			PawnData pd = getPawnData(p);
			if(!pd.lastPos[0].equals(p.getLocation())) {
				pd.lastPos[1] = pd.lastPos[0];
				pd.lastPos[0] = p.getLocation();
			}
		}
	}
	
	public float getHurtCorrection() {
		return 0.25f;
	}
	
	public float getDeathCorrection() {
		return 2;
	}
	
	public Rules getRules() {
		return rules;
	}
	
	public Game getGame() {
		return game;
	}
	
	public int getTeam() {
		return team;
	}
	
	public Set<Pawn> getPawns() {
		return game.getPawns(team);
	}
	
	public Set<Pawn> getEnemies() {
		Set<Pawn> pawns = new TreeSet<>();
		for (Pawn p : game.getPawns()) {
			if (p.getTeam() != team && p.getHealth() > 0 && !game.isVoid(p.getLocation())) {
				pawns.add(p);
			}
		
		}
		return pawns;
	}
	
	public Move goTo(Vector a, Vector b) {
		Vector dir = a.sub(b);
		return Move.fromDirection(dir);
	}
	
	public Move goToward(Pawn a, Pawn b) {
		return goToward(a.getLocation(), b.getLocation());
	}
	
	public Move goToward(Vector a, Vector b) {
		/*List<Vector> moves = path(a, b, game.getPawns());
		ArrayList<Vector> m2 = new ArrayList<>();
		m2.addAll(moves);
		if(!moves.isEmpty()) {
			Vector v = a;
			while(v.equals(a) && !moves.isEmpty()) {
				v = moves.get(moves.size() - 1);
				moves.remove(moves.size() - 1);
			}
			if(!moves.isEmpty()) {
				return goTo(a, v).getOpposite();
			}
		}*/
		for(Move m : path(a, b, game.getPawns())) {
			return m;
		}
		return Move.None;//Move.fromDirection(a.sub(b)).getOpposite();
	}
	
	public Path pathObstacles(Vector beg, Vector end, Set<Vector> obstacles) {
		class PathData
		{
			public PathData(Vector po, PathData par) {
				pos = po;
				parent = par;
				weight = parent == null ? 0 : parent.weight + 1;
			}
			
			public PathData(Vector p) {
				this(p, null);
			}
			
			public Vector pos;
			public PathData parent;
			public int weight;
		}
		if(game.isVoid(beg)) {
			System.err.println("Starting point in void");
		}
		if(game.isVoid(end)) {
			System.err.println("Ending point in void");
		}
		if(end.equals(beg)) {
			return Path.emptyPath();
		}
		obstacles = Utils.removed(obstacles, beg, end);
		Map<Vector, PathData> map = new HashMap<>();
		List<PathData> opened = new ArrayList<>();
		opened.add(new PathData(beg));
		while(true) {
			if(opened.isEmpty()) {
				return Path.emptyPath();
			}
			PathData current = null;
			for(PathData c : opened) {
				if(current == null || current.pos.manhattan(end) > c.pos.manhattan(end)) {
					current = c;
				}
			}
			opened.remove(current);
			map.put(current.pos, current);
			for(Vector dir : Utils.shuffled(Utils.dirs)) {
				Vector pos = current.pos.add(dir);
				if(pos.equals(end)) {
					ArrayList<Vector> positions = new ArrayList<>();
					positions.add(end);
					while(current != null) {
						positions.add(current.pos);
						current = current.parent;
					}
					Utils.reverse(positions);
					return new Path(positions);
				}
				if(!game.isVoid(pos) && !obstacles.contains(pos)) {
					PathData pd = map.get(pos);
					if(pd != null) {
						if(pd.weight > current.weight + 1) {
							pd.weight = current.weight + 1;
							pd.parent = current;
						}
					} else {
						opened.add(new PathData(pos, current));
					}
				}
			}
		}
	}
	
	public Path path(Vector beg, Vector end) {
		return pathObstacles(beg, end, new HashSet<Vector>());
	}
	
	public Path path(Vector beg, Vector end, Set<Pawn> obstacles) {
		Set<Vector> obs = new HashSet<>();
		for(Pawn p : obstacles) {
			obs.add(p.getLocation());
		}
		return pathObstacles(beg, end, obs);
	}
	
	public float getCorrectionFactor(Pawn pawn, Move move) {
		/*if(pawn.getLocation().add(move).equals(getPawnData(pawn).getLastPosition())) {
			return 1;
		}*/
		return 0;
	}
	
	public List<PotencialAction> killActions(Set<Pawn> targets) {
		List<PotencialAction> actions = new ArrayList<>();
		for(Pawn target : targets) {
			for(Vector dir : Utils.dirs) {
				Vector p = target.getLocation().add(dir);
				if(!game.isVoid(p)) {
					if(target.getHealth() < 2) {
						actions.add(new PotencialAction(target.getStance().getStrong(), p, Move.fromDirection(dir).getOpposite()));
					}
					if(game.isVoid(target.getLocation().sub(dir))) {
						actions.add(new PotencialAction(target.getStance(), p, Move.fromDirection(dir).getOpposite()));
						actions.add(new PotencialAction(target.getStance().getWeak(), p, Move.fromDirection(dir).getOpposite()));
					} else if(game.isVoid(target.getLocation().sub(dir).sub(dir))) {
						Pawn w = game.getPawn(target.getLocation().sub(dir));
						if(w == null || w.getTeam() != team) {
							actions.add(new PotencialAction(target.getStance().getWeak(), p, Move.fromDirection(dir).getOpposite()));
						}
					}
				}
			}
		}
		return actions;
	}
	
	public List<PotencialAction> hurtActions(Set<Pawn> targets) {
		List<PotencialAction> actions = new ArrayList<>();
		for(Pawn target : targets) {
			for(Vector dir : Utils.dirs) {
				Vector p = target.getLocation().add(dir);
				if(!game.isVoid(p)) {
					actions.add(new PotencialAction(target.getStance().getStrong(), p, Move.fromDirection(dir).getOpposite()));
				}
			}
		}
		return actions;
	}
	
	public Vector getBoardSize() {
		return game.getBoard().getMax().sub(game.getBoard().getMin()).abs();
	}
	
	private PawnData getPawnData(Pawn p) {
		PawnData pd = pawnData.get(p);
		if(pd == null) {
			pawnData.put(p, pd = new PawnData(p.getLocation()));
		}
		return pd;
	}
	
	
	
}
