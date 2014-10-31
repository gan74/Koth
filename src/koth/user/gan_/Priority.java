package koth.user.gan_;

public enum Priority {
	Kill,
	Hurt,
	Move,
	None;
	
	private double clampFactor;
	
	static {
		Kill.clampFactor = 1;
		Hurt.clampFactor = 0;
		Move.clampFactor = 0;
		None.clampFactor = 0;
	}
	
	
	public int clamped(int actions) {
		return Math.min(1, (int)Math.ceil(actions * clampFactor));
	}
}
