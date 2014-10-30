
package koth.user.gan_;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import koth.game.*;

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
			moves.add(Move.fromDirection(end.sub(beg)));
		}
		for(int i = 0; i != moves.size(); i++) {
			Vector a = waypoints.get(i);
			Vector b = waypoints.get(i + 1);
			if(!a.add(moves.get(i)).equals(b)) {
				System.err.println("ERROR : " + a + " + " + moves.get(i) + " != " + b);
			}
		}
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
