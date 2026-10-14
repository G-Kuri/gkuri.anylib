package gkuri.anylib.scheduler.loop;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class LoopScheduler {
	
	public static final long INFINITY_LOOP = -1;

	protected LoopScheduleTask task;
	protected TimeUnit executeSpan;
	protected int span;
	protected int schedulerThreadPool;
	protected LoopSchedulerMode loopSchedulerMode;
	protected TaskContext defaultTaskContext;
	
	protected boolean isRunning;
	protected long loopCount;
	protected long maxLoopCount;

	protected long loopStartCurrentTimeMillis;
	protected long beforeExecuteCurrentTimeMillis;
	protected long nextExecuteCurrentTimeMillis;
	
	protected ScheduledExecutorService service;
	protected ExecutorService serviceWorker;
	protected Future<?> future;
	
	protected LoopScheduler(LoopScheduleTask task) {
		this.task = task;
		this.executeSpan = TimeUnit.MILLISECONDS;
		this.span = 1;
		this.schedulerThreadPool = 1;
		this.maxLoopCount = -1;
		this.loopSchedulerMode = LoopSchedulerMode.FIXED_DELAY_MODE;
		this.defaultTaskContext = null;
		
		this.service = null;
		this.serviceWorker = null;
		this.future = null;
		this.loopCount = 0;
		this.isRunning = false;
		this.beforeExecuteCurrentTimeMillis = -1;
		this.nextExecuteCurrentTimeMillis = -1;
	}
	
	public LoopScheduler(LoopScheduleTask task, TimeUnit executeSpan, int span, int schedulerThreadPool, LoopSchedulerMode loopSchedulerMode, long maxLoopCount, TaskContext defaultTaskContext) {
		this(task);
		this.executeSpan = executeSpan;
		this.span = span;
		this.schedulerThreadPool = schedulerThreadPool;
		this.maxLoopCount = maxLoopCount;
		this.loopSchedulerMode = loopSchedulerMode;
		this.defaultTaskContext = defaultTaskContext;
	}
	
	/**
	 * スケジューラを起動する。
	 */
	public abstract void start();
	
	/**
	 * startで実行開始までのディレイを計算して呼び出される想定。
	 * delayの単位はミリ秒に統一すること。
	 */
	protected synchronized void loopStart(int startDelay) {
		if(this.isRunning) return;
		
		if(this.maxLoopCount != INFINITY_LOOP && this.maxLoopCount <= this.loopCount) {
			this.shutdown();
			return;
		}
		
		if(this.service == null || this.serviceWorker == null) {
			this.service = Executors.newSingleThreadScheduledExecutor();
			this.serviceWorker = Executors.newFixedThreadPool(this.schedulerThreadPool);
		}
		
		this.nextExecuteCurrentTimeMillis = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(startDelay, TimeUnit.MILLISECONDS);
		
		//ここで例外吐いたらそのまま伝播させる
		try {
			this.task.init(this.createTaskContext());
		} catch (Exception e) {
			this.shutdownNow();
			throw e;
		}
		
		this.isRunning = true;
		this.loopStartCurrentTimeMillis = System.currentTimeMillis();
		switch (this.loopSchedulerMode) {
			case FIXED_DELAY_MODE:
				this.service.scheduleWithFixedDelay(() -> {
					if(this.maxLoopCount != INFINITY_LOOP && this.maxLoopCount <= this.loopCount) return;
					
					this.future = this.serviceWorker.submit(() -> {
						this.loopTask();
						this.nextExecuteCurrentTimeMillis = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(this.span, this.executeSpan);
					});
				}, startDelay, TimeUnit.MILLISECONDS.convert(this.span,this.executeSpan) , TimeUnit.MILLISECONDS);
				break;
			case FIXED_RATE_MODE:
				this.service.scheduleAtFixedRate(() -> {
					if(this.maxLoopCount != INFINITY_LOOP && this.maxLoopCount <= this.loopCount) return;
					
					this.future = this.serviceWorker.submit(() -> {
						this.nextExecuteCurrentTimeMillis = this.nextExecuteCurrentTimeMillis + TimeUnit.MILLISECONDS.convert(this.span, this.executeSpan);
						this.loopTask();
					});
				}, startDelay, TimeUnit.MILLISECONDS.convert(this.span, this.executeSpan) , TimeUnit.MILLISECONDS);
				break;
		}
	}
	
	/**
	 * スケジューラを一時停止する。
	 */
	public abstract void cancel();
	
	/**
	 * loopStartと似てるけど、こっちは明確な用途を想定してない。
	 * ただstopで何か個別処理してからループを止められるようにするためだけに用意した。
	 */
	protected synchronized void loopCancel(boolean mayInterruptIfRunning) {
		if (this.future != null) {
			this.future.cancel(mayInterruptIfRunning);
		}
		this.isRunning = false;
		
		//ここはもうやるべき事終わってるし例外吐いてもそのまま戻す
		this.task.finished(this.createTaskContext());
	}
	
	/**
	 * スケジューラを停止・初期化する。
	 */
	public synchronized void shutdown() {
		try {
			//loopCancel内のtask.finishedで死んだときの対策
			this.loopCancel(true);
		} catch (Exception e) {
			throw e;
		} finally {
			if(this.service != null) this.service.shutdown();
			if(this.serviceWorker != null) this.serviceWorker.shutdown();
			this.service = null;
			this.serviceWorker = null;
			this.future = null;
			this.loopCount = 0;
			this.isRunning = false;
			this.beforeExecuteCurrentTimeMillis = -1;
			this.nextExecuteCurrentTimeMillis = -1;
		}
	}
	
	public synchronized void shutdownNow() {
		if(this.service != null) this.service.shutdownNow();
		if(this.serviceWorker != null) this.serviceWorker.shutdownNow();
		this.shutdown();
	}
	
	protected void loopTask() {
		this.loopCount++;
		TaskContext context = this.createTaskContext();
		this.beforeExecuteCurrentTimeMillis = System.currentTimeMillis();
		try {
			//ここだけはループ中なので例外吐いてもスケジューラが止まらないようにフォローする
			this.task.task(context);
		} catch (Exception e) {
			try {
				//こっちでも例外吐いたら今後が怪しいので止める
				this.task.taskCatchException(context, e);
			}catch (Exception e1) {
				this.shutdownNow();
				throw e1;
			}
		}
		
		if(this.maxLoopCount != INFINITY_LOOP && this.maxLoopCount <= context.getLoopCount()) {
			this.shutdown();
		}
	}
	
	protected TaskContext createTaskContext() {
		TaskContext context;
		if(this.defaultTaskContext != null) {
			context = this.defaultTaskContext.copy();
		} else {
			context = new TaskContext();
		}
		
		context.setBeforeExecuteCurrentTimeMillis(this.beforeExecuteCurrentTimeMillis);
		context.setBeforeExecuteTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(this.beforeExecuteCurrentTimeMillis), ZoneId.systemDefault()));
		context.setLoopCount(this.loopCount);
		context.setLoopStartCurrentTimeMillis(this.loopStartCurrentTimeMillis);
		context.setLoopStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(this.loopStartCurrentTimeMillis), ZoneId.systemDefault()));
		context.setMaxLoopCount(this.maxLoopCount);
		return context;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public LocalDateTime getNextExecuteTime() {
		return this.ctm2ldt(this.nextExecuteCurrentTimeMillis);
	}
	
	public long getNextExecuteCurrentTimeMillis() {
		return this.nextExecuteCurrentTimeMillis;
	}
	
	public long getLoopCount() {
		return this.loopCount;
	}
	
	public long getMaxLoopCount() {
		return this.maxLoopCount;
	}
	
	public void setMaxLoopCount(long maxLoopCount) {
		this.maxLoopCount = maxLoopCount;
	}
	
	protected long ldt2ctm(LocalDateTime ldt) {
		return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
	
	protected LocalDateTime ctm2ldt(long ctm) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(ctm), ZoneId.systemDefault());
	}
}
