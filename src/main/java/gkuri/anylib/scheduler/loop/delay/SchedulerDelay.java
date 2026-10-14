package gkuri.anylib.scheduler.loop.delay;

import java.util.concurrent.TimeUnit;

import gkuri.anylib.scheduler.loop.LoopScheduleTask;
import gkuri.anylib.scheduler.loop.LoopScheduler;
import gkuri.anylib.scheduler.loop.LoopSchedulerMode;
import gkuri.anylib.scheduler.loop.TaskContext;

public class SchedulerDelay extends LoopScheduler {

	public SchedulerDelay(LoopScheduleTask task, TimeUnit executeSpan, int span, int schedulerThreadPool, long maxLoopCount, TaskContext defaultTaskContext) {
		super(task, executeSpan, span, schedulerThreadPool, LoopSchedulerMode.FIXED_DELAY_MODE, maxLoopCount, defaultTaskContext);
	}

	@Override
	public void start() {
		this.loopStart(0);
	}

	@Override
	public void cancel() {
		this.loopCancel(false);
	}

}
