package gkuri.anylib.scheduler.loop.time;

import gkuri.anylib.scheduler.loop.LoopScheduleTask;
import gkuri.anylib.scheduler.loop.LoopScheduler;
import gkuri.anylib.scheduler.loop.TaskContext;

public class TimeSchedulerBuilder {

	private LoopScheduleTask task;
	private int schedulerThreadPool;
	private TaskContext defaultTaskContext;
	private long maxLoopCount;
	
	private TimeSchedulerMode timeSchedulerMode;
	private SchedulerTime schedulerTime;
	
	public TimeSchedulerBuilder() {
		this.task = null;
		this.schedulerThreadPool = 1;
		this.defaultTaskContext = null;
		this.maxLoopCount = LoopScheduler.INFINITY_LOOP;
		
		this.timeSchedulerMode = null;
		this.schedulerTime = null;
	}
	
	public TimeSchedulerBuilder(LoopScheduleTask task, TimeSchedulerMode timeSchedulerMode, SchedulerTime schedulerTime) {
		this.task = task;
		this.timeSchedulerMode = timeSchedulerMode;
		this.schedulerTime = schedulerTime;
	}
	
	public LoopScheduler build() {
		if(this.task == null || this.timeSchedulerMode == null || this.schedulerTime == null) throw new IllegalStateException("task or time scheduler mode or scheduler time is undefined.");
		
		return new TimeLoopScheduler(this.task, this.schedulerThreadPool, this.maxLoopCount, this.defaultTaskContext, this.timeSchedulerMode, this.schedulerTime);
	}
	
	public TimeSchedulerBuilder setTask(LoopScheduleTask task) {
		this.task = task;
		return this;
	}
	
	public TimeSchedulerBuilder setSchedulerThreadPool(int schedulerThreadPool) {
		if(schedulerThreadPool < 1) return this;
		this.schedulerThreadPool = schedulerThreadPool;
		return this;
	}
	
	public TimeSchedulerBuilder setDefaultTaskContext(TaskContext context) {
		this.defaultTaskContext = context;
		return this;
	}
	
	public TimeSchedulerBuilder setMaxLoopCount(long maxLoopCount) {
		if(maxLoopCount == 0) return this;
		this.maxLoopCount = Math.max(LoopScheduler.INFINITY_LOOP, maxLoopCount);
		return this;
	}
	
	public TimeSchedulerBuilder setTimeSchedulerMode(TimeSchedulerMode timeSchedulerMode) {
		this.timeSchedulerMode = timeSchedulerMode;
		return this;
	}
	
	public TimeSchedulerBuilder setSchedulerTime(SchedulerTime schedulerTime) {
		this.schedulerTime = schedulerTime;
		return this;
	}
}
