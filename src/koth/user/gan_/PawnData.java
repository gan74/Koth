package koth.user.gan_;

import koth.game.*;
import koth.util.*;

public class PawnData {
	public Vector[] lastPos;
	
	public PawnData(Vector p) {
		lastPos = new Vector[]{p, p};
	}
	
	public Vector getLastPosition() {
		return lastPos[1];
	}
}
