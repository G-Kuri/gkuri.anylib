package gkuri.anylib.scheduler;

@FunctionalInterface
public interface SchedulerTask {

	public void task();
	
	default public void taskCatchException(Exception exception) {
		
	}
}
