package scheduler.loop.delay;

import java.util.concurrent.TimeUnit;

import gkuri.anylib.scheduler.loop.LoopScheduler;
import gkuri.anylib.scheduler.loop.LoopSchedulerMode;
import gkuri.anylib.scheduler.loop.delay.DelaySchedulerBuilder;
import gkuri.anylib.scheduler.loop.time.SchedulerTime;
import gkuri.anylib.scheduler.loop.time.TimeSchedulerBuilder;
import gkuri.anylib.scheduler.loop.time.TimeSchedulerMode;

public class Test {

	public static void main(String[] args) {
		//DelayTaskの処理時間は400ms over
		DelaySchedulerBuilder dBuilder = new DelaySchedulerBuilder()
				.setSpan(500)
				.setTask(new DelayTask("defoMode"))
				.setMaxLoopCount(15);
		
		dBuilder.build().start();
		
		dBuilder.setLoopSchedulerMode(LoopSchedulerMode.FIXED_RATE_MODE).setTask(new DelayTask("rateMode")).build().start();
		dBuilder.setTask(new DelayTask("rateOver")).setSpan(300).build().start();
		dBuilder.setTask(new DelayTask("rateOv3T")).setSpan(300).setSchedulerThreadPool(3).build().start();
		
		LoopScheduler secondsScheduler = dBuilder.setMaxLoopCount(3)
			.setLoopSchedulerMode(LoopSchedulerMode.FIXED_DELAY_MODE)
			.setSpan(5)
			.setSpanTimeUnit(TimeUnit.SECONDS)
			.setTask(new DelayTask("seconds"))
			.build();
		
		secondsScheduler.start();
		System.out.println("secondes next execute : " + secondsScheduler.getNextExecuteCurrentTimeMillis());
		
		SchedulerTime st = new SchedulerTime();
		st.setMillSec(250);
		TimeSchedulerBuilder tBuilder = new TimeSchedulerBuilder()
				.setTimeSchedulerMode(TimeSchedulerMode.EVERY_SEC)
				.setMaxLoopCount(15)
				.setSchedulerTime(st)
				.setTask(new DelayTask("time.25s"));
		
		LoopScheduler time250MSScheduler = tBuilder.build();
		time250MSScheduler.start();
		System.out.println("time250MSScheduler next execute : " + time250MSScheduler.getNextExecuteCurrentTimeMillis() + "(" + time250MSScheduler.getNextExecuteTime() + ")");
	
		LoopScheduler time4525SScheduler = tBuilder.setTimeSchedulerMode(TimeSchedulerMode.EVERY_MIN)
			.setMaxLoopCount(2)
			.setSchedulerTime(st.setSec(45))
			.setTask(new DelayTask("time4525"))
			.build();
		
		time4525SScheduler.start();
		System.out.println("time4525SScheduler next execute : " + time4525SScheduler.getNextExecuteCurrentTimeMillis() + "(" + time4525SScheduler.getNextExecuteTime() + ")");
	
		TimeSchedulerMode.valueOf("every year");
	}
}
