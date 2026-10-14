package gkuri.anylib.scheduler.loop.time;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import gkuri.anylib.scheduler.loop.LoopScheduleTask;
import gkuri.anylib.scheduler.loop.LoopScheduler;
import gkuri.anylib.scheduler.loop.TaskContext;

/**
 * 
 */
//LoopSchedulerの大半のメソッドをオーバーライドして、次回実行時間になったら実行のロジックにする予定
public class TimeLoopScheduler extends LoopScheduler {

	protected TimeSchedulerMode timeSchedulerMode;
	protected SchedulerTime schedulerTime;
	
	public TimeLoopScheduler(LoopScheduleTask task, int schedulerThreadPool, long maxLoopCount, TaskContext defaultTaskContext,
			TimeSchedulerMode timeSchedulerMode, SchedulerTime schedulerTime) {
		super(task);
		
		this.schedulerThreadPool = schedulerThreadPool;
		this.maxLoopCount = maxLoopCount;
		this.defaultTaskContext = defaultTaskContext;
		this.timeSchedulerMode = timeSchedulerMode;
		this.schedulerTime = schedulerTime;
	}
	

	@Override
	public void start() {
		this.loopStart(0);
	}

	@Override
	public void cancel() {
		this.loopCancel(false);
	}
	
	/**
	 * "@Override"の都合で引数あるけど使ってない
	 */
	@Override
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
		
		//ここで例外吐いたらそのまま伝播させる
		try {
			this.task.init(this.createTaskContext());
		} catch (Exception e) {
			this.shutdownNow();
			throw e;
		}
		
		this.isRunning = true;
		this.loopStartCurrentTimeMillis = System.currentTimeMillis();
		
		this.nextExecuteCurrentTimeMillis = this.calcNextExecuteTime(this.loopStartCurrentTimeMillis);
		new Thread(new Runnable() {
			@Override
			public void run() {
				loopTask();
			}
		}).start();
	}
	
	@Override
	protected void loopTask() {
		this.future = this.service.schedule(() -> {
			this.nextExecuteCurrentTimeMillis = this.calcNextExecuteTime(this.nextExecuteCurrentTimeMillis);
			this.loopCount++;
			TaskContext context = this.createTaskContext();
			this.beforeExecuteCurrentTimeMillis = System.currentTimeMillis();
			
			this.future = this.serviceWorker.submit(() -> {
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
			});
			
			if(this.isRunning && (this.maxLoopCount == INFINITY_LOOP || this.loopCount < this.maxLoopCount)) {
				this.future = this.service.schedule(() -> {
					loopTask();
				}, Math.max(0, this.nextExecuteCurrentTimeMillis - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
			}
		}, Math.max(0, this.nextExecuteCurrentTimeMillis - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
	}
	
	private long calcNextExecuteTime(long baseCurrentTimeMillis) {
		
		LocalDateTime baseTime = ctm2ldt(baseCurrentTimeMillis);
		//ミリ秒
		baseTime = baseTime.plus(calcPlusTime(baseTime, TimeUnit.MILLISECONDS) , ChronoUnit.MILLIS);
		if(this.timeSchedulerMode == TimeSchedulerMode.EVERY_SEC) return ldt2ctm(baseTime);
		
		//秒
		baseTime = baseTime.plusSeconds(calcPlusTime(baseTime, TimeUnit.SECONDS));
		if(this.timeSchedulerMode == TimeSchedulerMode.EVERY_MIN) return ldt2ctm(baseTime);
		
		//分
		baseTime = baseTime.plusMinutes(calcPlusTime(baseTime, TimeUnit.MINUTES));
		if(this.timeSchedulerMode == TimeSchedulerMode.EVERY_HOUR) return ldt2ctm(baseTime);
		
		//時
		baseTime = baseTime.plusHours(calcPlusTime(baseTime, TimeUnit.HOURS));
		if(this.timeSchedulerMode == TimeSchedulerMode.EVERY_DAY) return ldt2ctm(baseTime);

		// 曜日
	    if (this.timeSchedulerMode == TimeSchedulerMode.EVERY_WEEK) {

	        int targetDOF = this.schedulerTime.getDayOfWeekNum();
	        if (this.schedulerTime.getDayOfWeek() != null) targetDOF = this.schedulerTime.getDayOfWeek().getValue();
	        
	        int currentDOF = baseTime.getDayOfWeek().getValue();
	        if (currentDOF < targetDOF) {
	            baseTime = baseTime.plusDays(targetDOF - currentDOF);
	        } else if (targetDOF < currentDOF) {
	            baseTime = baseTime.plusDays(7 - currentDOF + targetDOF);
	        } else {
	            LocalDateTime targetTime = baseTime
	                .withHour(this.schedulerTime.getHour())
	                .withMinute(this.schedulerTime.getMin())
	                .withSecond(this.schedulerTime.getSec())
	                .withNano(this.schedulerTime.getMillSec() * 1_000_000);

	            if (!targetTime.isAfter(baseTime)) baseTime = baseTime.plusDays(7);
	        }
	        return ldt2ctm(baseTime);
	    }
	    
		//日
		baseTime = baseTime.plusDays(calcPlusTime(baseTime, TimeUnit.DAYS));
		if(this.timeSchedulerMode == TimeSchedulerMode.EVERY_MONTH) return ldt2ctm(baseTime);
		
		//月
		if(ctm2ldt(baseCurrentTimeMillis).isEqual(baseTime)) {
			baseTime = baseTime.plusMonths(12);
		} else {
			baseTime = baseTime.plusMonths(baseTime.getMonthValue() < this.schedulerTime.getMonth() ? this.schedulerTime.getMonth() - baseTime.getMonthValue() : 12 - baseTime.getMonthValue() + this.schedulerTime.getMonth());
		}
		
		return ldt2ctm(baseTime);
	}
	
	private long calcPlusTime(LocalDateTime baseTime, TimeUnit timeUnit) {
		
		switch (timeUnit) {
			case MILLISECONDS:
				return (baseTime.getNano() / 1_000_000) < this.schedulerTime.getMillSec() ? this.schedulerTime.getMillSec() - (baseTime.getNano() / 1_000_000) : 1000 - (baseTime.getNano() / 1_000_000) + this.schedulerTime.getMillSec();
			case SECONDS:
				return baseTime.getSecond() < this.schedulerTime.getSec() ? this.schedulerTime.getSec() - baseTime.getSecond() : 60 - baseTime.getSecond() + this.schedulerTime.getSec();
			case MINUTES:
				return baseTime.getMinute() < this.schedulerTime.getMin() ? this.schedulerTime.getMin() - baseTime.getMinute() : 60 - baseTime.getMinute() + this.schedulerTime.getMin();
			case HOURS:
				return baseTime.getHour() < this.schedulerTime.getHour() ? this.schedulerTime.getHour() - baseTime.getHour() : 24 - baseTime.getHour() + this.schedulerTime.getHour();
			case DAYS:
				LocalDateTime daysBaseTime = baseTime;
				int lengthOfMonth = daysBaseTime.toLocalDate().lengthOfMonth();
				while(lengthOfMonth < this.schedulerTime.getDay()) {
					daysBaseTime = daysBaseTime.plusMonths(1);
					lengthOfMonth = daysBaseTime.toLocalDate().lengthOfMonth();
				}
				return daysBaseTime.getDayOfMonth() < this.schedulerTime.getDay() ? this.schedulerTime.getDay() - daysBaseTime.getDayOfMonth() : lengthOfMonth - daysBaseTime.getDayOfMonth() + this.schedulerTime.getDay();
			default:
				throw new IllegalArgumentException(timeUnit.name() + " is unsupported");
		}
	}

}
