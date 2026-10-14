package scheduler.loop.delay;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import gkuri.anylib.scheduler.loop.LoopScheduleTask;
import gkuri.anylib.scheduler.loop.TaskContext;

public class DelayTask implements LoopScheduleTask {
	
	private final String id;
	
	public DelayTask(String id) {
		this.id = id;
	}
	
	@Override
	public void init(TaskContext context) {
		long executeTime = System.currentTimeMillis();
		System.out.println("id（init) : " + id + " || time : " + executeTime + " || context : " + context);
	}
	
	@Override
	public void finished(TaskContext context) {
		long executeTime = System.currentTimeMillis();
		System.out.println("id（finished) : " + id + " || time : " + executeTime + " || context : " + context);
	}

	@Override
	public void task(TaskContext context) {
		long executeTime = System.currentTimeMillis();
		try {
			Thread.sleep(400);
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("id : " + id + " || time : " + executeTime + " || context : " + context + " || time : " + LocalDateTime.ofInstant(Instant.ofEpochMilli(executeTime), ZoneId.systemDefault()));
		if(context.getLoopCount() % 10 == 0) {
			throw new IllegalStateException("loopCount % 10 == 0");
		}
	}
	
	@Override
	public void taskCatchException(TaskContext context, Exception exception) {
		System.err.println("id : " + id + " || msg : " + exception.getMessage());
	}
}
