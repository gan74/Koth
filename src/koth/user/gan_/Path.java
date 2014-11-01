
package koth.user.gan_;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import koth.game.*;
import koth.util.*;

public class Path implements Iterable<Move> {
	
	private ArrayList<Vector> waypoints;
	private ArrayList<Move> moves;
	
	public Path(List<Vector> path) {
		waypoints = new ArrayList<>();
		waypoints.addAll(path);
		moves = new ArrayList<>();
		for(int i = 1; i < waypoints.size(); i++) {
			Vector beg = waypoints.get(i - 1);
			Vector end = waypoints.get(i);
			Move move = Move.fromDirection(end.sub(beg));
			if(move != Move.None) {
				moves.add(move);
			}
		}
	}
	
	public List<Vector> getWaypoints() {
		ArrayList<Vector> v = new ArrayList<>();
		v.addAll(waypoints);
		return v;
	}
	
	public int size() {
		return moves.size();
	}

	@Override
	public Iterator<Move> iterator() {
		return moves.iterator();
	}
	
	public static Path emptyPath() {
		return new Path(new ArrayList<Vector>());
	}
}
