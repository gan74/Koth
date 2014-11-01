package koth.user.gan_;

public class PrioritizedActionSequence {
	private ActionSequence seq;
	private Priority prio;
	
	public PrioritizedActionSequence(ActionSequence s, Priority p) {
		prio = p;
		seq = s;
	}
	
	public Priority getPriority() {
		return prio;
	}
	
	public ActionSequence getActionSequence() {
		return seq;
	}
}
