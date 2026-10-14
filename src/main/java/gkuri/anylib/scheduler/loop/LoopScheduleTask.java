package gkuri.anylib.scheduler.loop;

@FunctionalInterface
public interface LoopScheduleTask {

	public void task(TaskContext context);
	
	default public void taskCatchException(TaskContext context, Exception exception) {
		
	}
	
	default public void init(TaskContext context) {
		
	}
	
	default public void finished(TaskContext context) {
		
	}
}
