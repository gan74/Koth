package koth.user.gan_;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import koth.game.*;
public class Utils {
	
	private Utils() {
	}
	
	public final static Vector[] dirs = {new Vector(1, 0), new Vector(-1, 0), new Vector(0, 1),  new Vector(0, -1)};
	

	
	public static <E> E[]shuffled(E[] t) {
		E[] result = t.clone();
		Random random = new Random();
		for (int i = result.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			E temp = result[index];
			result[index] = result[i];
			result[i] = temp;
		}
		return result;
	}
	
	public static List<PawnAction> toPawnAction(GameContext context, Pawn pawn, List<? extends PredictedAction> lst) {
		List<PawnAction> pa = new ArrayList<>();
		for(PredictedAction p : lst) {
			pa.add(p.toPawnAction(context, pawn));
		}
		Collections.sort(pa);
		return pa;
	}
	
	@SafeVarargs
	public static <E> Set<E> removed(Set<E> set, E... rs) {
		Set<E> s = new HashSet<>();
		for(E e : set) {
			boolean contains = false;
			for(E r : rs) {
				if(e.equals(r)) {
					contains = true;
					break;
				}
			}
			if(!contains) {
				s.add(e);
			}
		}
		return s;
	} 
	
	public static String teamName(int t) {
		switch(t) {
		case 0: return "Red";
		case 1: return "Blu";
		default: throw new IllegalArgumentException();
		}
	}
}
