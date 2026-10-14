package gkuri.anylib.scheduler.loop.delay;

import java.util.concurrent.TimeUnit;

import gkuri.anylib.scheduler.loop.LoopScheduleTask;
import gkuri.anylib.scheduler.loop.LoopScheduler;
import gkuri.anylib.scheduler.loop.LoopSchedulerMode;
import gkuri.anylib.scheduler.loop.TaskContext;

public class DelaySchedulerBuilder {

	private LoopScheduleTask task;
	private TimeUnit spanTimeUnit;
	private int span;
	private int schedulerThreadPool;
	private LoopSchedulerMode loopSchedulerMode;
	private TaskContext defaultTaskContext;
	private long maxLoopCount;
	
	public DelaySchedulerBuilder() {
		this.task = null;
		this.spanTimeUnit = TimeUnit.MILLISECONDS;
		this.span = 0;
		this.schedulerThreadPool = 1;
		this.loopSchedulerMode = LoopSchedulerMode.FIXED_DELAY_MODE;
		this.defaultTaskContext = null;
		this.maxLoopCount = LoopScheduler.INFINITY_LOOP;
	}
	
	public DelaySchedulerBuilder(LoopScheduleTask task, int span) {
		this.task = task;
		this.setSpan(span);
	}
	
	public LoopScheduler build() {
		if(this.task == null || this.span == 0) throw new IllegalStateException("task or span is undefined.");
		
		switch (this.loopSchedulerMode) {
			case FIXED_DELAY_MODE:
				return new SchedulerDelay(task, spanTimeUnit, span, schedulerThreadPool, maxLoopCount, defaultTaskContext);
			case FIXED_RATE_MODE:
				return new SchedulerRate(task, spanTimeUnit, span, schedulerThreadPool, maxLoopCount, defaultTaskContext);
		}
		throw new IllegalStateException("unknown loop scheduler mode");
	}
	
	public DelaySchedulerBuilder setTask(LoopScheduleTask task) {
		this.task = task;
		return this;
	}
	
	public DelaySchedulerBuilder setSpanTimeUnit(TimeUnit timeUnit) {
		this.spanTimeUnit = timeUnit;
		return this;
	}
	
	public DelaySchedulerBuilder setSpan(int span) {
		if(1 <= span) {
			this.span = span;
			return this;
		}
		this.span = 0;
		return this;
	}
	
	public DelaySchedulerBuilder setSchedulerThreadPool(int schedulerThreadPool) {
		if(schedulerThreadPool < 1) return this;
		this.schedulerThreadPool = schedulerThreadPool;
		return this;
	}
	
	public DelaySchedulerBuilder setLoopSchedulerMode(LoopSchedulerMode loopSchedulerMode) {
		this.loopSchedulerMode = loopSchedulerMode;
		return this;
	}
	
	public DelaySchedulerBuilder setDefaultTaskContext(TaskContext context) {
		this.defaultTaskContext = context;
		return this;
	}
	
	public DelaySchedulerBuilder setMaxLoopCount(long maxLoopCount) {
		if(maxLoopCount == 0) return this;
		this.maxLoopCount = Math.max(LoopScheduler.INFINITY_LOOP, maxLoopCount);
		return this;
	}
	
	
}
