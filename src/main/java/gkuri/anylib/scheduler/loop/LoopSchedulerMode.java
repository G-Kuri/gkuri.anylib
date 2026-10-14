package gkuri.anylib.scheduler.loop;

public enum LoopSchedulerMode {

	FIXED_DELAY_MODE("fixed delay mode"),
	FIXED_RATE_MODE("fixed rate mode");
	
	private final String modeName;
	
	private LoopSchedulerMode(String modeName) {
		this.modeName = modeName;
	}
	
	@Override
	public String toString() {
		return "LoopSchedulerMode[ modeName : %s ]".formatted(this.modeName);
	}
}
