package koth.user.gan_;

public class PrioritizedActionSequence {
	private PawnActionSequence seq;
	private Priority prio;
	
	public PrioritizedActionSequence(PawnActionSequence s, Priority p) {
		prio = p;
		seq = s;
	}
	
	public Priority getPriority() {
		return prio;
	}
	
	public PawnActionSequence getActionSequence() {
		return seq;
	}
}
