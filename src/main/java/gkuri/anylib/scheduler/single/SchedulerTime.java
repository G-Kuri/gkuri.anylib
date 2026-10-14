package gkuri.anylib.scheduler.single;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import gkuri.anylib.scheduler.SchedulerTask;

public class SchedulerTime {
	
	private SchedulerTask task;
	private LocalDateTime executeDateTime;
	
	private ScheduledExecutorService service;
	private Future<?> future;

	public SchedulerTime(SchedulerTask task, LocalDateTime executeTime) {
		this.service = Executors.newSingleThreadScheduledExecutor();
		this.task = task;
		this.executeDateTime = executeTime; 
	}
	
	public void start() {
		this.future = service.schedule(() -> {
			try {
				this.task.task();
			} catch (Exception e) {
				try {
					this.task.taskCatchException(e);
				} catch (Exception e1) {
					throw e1;
				}
			}
		}, calcExecuteTime(executeDateTime), TimeUnit.MILLISECONDS);
	}
	
	public void cancel() {
		if(this.future == null) return;
		this.future.cancel(false);
	}
	
	public void shutdown() {
		this.cancel();
		
		if(this.service == null) return;
		this.service.shutdown();
	}
	
	public void shutdownNow() {
		this.cancel();
		
		if(this.service == null) return;
		this.service.shutdownNow();
	}
	
	private long calcExecuteTime(LocalDateTime executeTime) {
		return executeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();
	}
}
