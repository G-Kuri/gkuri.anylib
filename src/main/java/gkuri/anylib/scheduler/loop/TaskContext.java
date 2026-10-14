package gkuri.anylib.scheduler.loop;

import java.time.LocalDateTime;

public class TaskContext {

	private long loopCount;
	private long maxLoopCount;
	
	private LocalDateTime loopStartTime;
	private LocalDateTime beforeExecuteTime;
	private long loopStartCurrentTimeMillis;
	private long beforeExecuteCurrentTimeMillis;
	
	protected TaskContext() {
		
	}
	
	public TaskContext copy() {
		TaskContext context = new TaskContext();
		context.beforeExecuteCurrentTimeMillis = this.beforeExecuteCurrentTimeMillis;
		context.beforeExecuteTime = this.beforeExecuteTime;
		context.loopCount = this.loopCount;
		context.loopStartCurrentTimeMillis = this.loopStartCurrentTimeMillis;
		context.loopStartTime = this.loopStartTime;
		context.maxLoopCount = this.maxLoopCount;
		return context;
	}
	
	public long getLoopCount() {
		return this.loopCount;
	}
	
	protected void setLoopCount(long loopCount) {
		this.loopCount = loopCount;
	}
	
	public long getMaxLoopCount() {
		return this.maxLoopCount;
	}
	
	protected void setMaxLoopCount(long maxLoopCount) {
		this.maxLoopCount = maxLoopCount;
	}
	
	public LocalDateTime getBeforeExecuteTime() {
		return this.beforeExecuteTime;
	}
	
	protected void setBeforeExecuteTime(LocalDateTime beforeExecuteTime) {
		this.beforeExecuteTime = beforeExecuteTime;
	}
	
	public LocalDateTime getLoopStartTime() {
		return this.loopStartTime;
	}
	
	protected void setLoopStartTime(LocalDateTime loopStartTime) {
		this.loopStartTime = loopStartTime;
	}
	
	public long getLoopStartCurrentTimeMillis() {
		return this.loopStartCurrentTimeMillis;
	}
	
	protected void setLoopStartCurrentTimeMillis(long loopStartCurrentTimeMillis) {
		this.loopStartCurrentTimeMillis = loopStartCurrentTimeMillis;
	}
	
	public long getBeforeExecuteCurrentTimeMillis() {
		return this.beforeExecuteCurrentTimeMillis;
	}
	
	protected void setBeforeExecuteCurrentTimeMillis(long beforeExecuteCurrentTimeMillis) {
		this.beforeExecuteCurrentTimeMillis = beforeExecuteCurrentTimeMillis;
	}
	
	@Override
	public String toString() {
		return "TaskContext[ loops : %d/%d || loopStart : %d || beforeExecuteTime : %d ]".formatted(this.loopCount, this.maxLoopCount, this.loopStartCurrentTimeMillis, this.beforeExecuteCurrentTimeMillis);
	}
}
