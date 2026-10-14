package cache;

import gkuri.anylib.cache.CacheStore;
import gkuri.anylib.cache.stores.LRUCacheStore;
import gkuri.anylib.scheduler.loop.LoopScheduleTask;
import gkuri.anylib.scheduler.loop.TaskContext;
import gkuri.anylib.scheduler.loop.delay.DelaySchedulerBuilder;

public class Test {

	public static void main(String[] args) {
		CacheStore<Long> storeR = new LRUCacheStore<>(3);
		CacheStore<Long> storeF = new LRUCacheStore<>(3);
		
		new DelaySchedulerBuilder().setSpan(10).setTask(new LoopScheduleTask() {
			
			@Override
			public void task(TaskContext context) {
				storeR.put(context.getLoopCount() + "", context.getBeforeExecuteCurrentTimeMillis());
				storeR.get("1");
			}
			
			@Override
			public void finished(TaskContext context) {
				for(String key : storeR.keySet()) {
					System.out.println(key + ":" + storeR.get(key));
				}
			}
			
		}).setMaxLoopCount(10).build().start();
		
		new DelaySchedulerBuilder().setSpan(10).setTask(new LoopScheduleTask() {
			
			@Override
			public void task(TaskContext context) {
				storeF.put(context.getLoopCount() + "", context.getBeforeExecuteCurrentTimeMillis());
				storeF.get("1");
			}
			
			@Override
			public void finished(TaskContext context) {
				for(String key : storeF.keySet()) {
					System.out.println(key + ":" + storeF.get(key));
				}
			}
			
		}).setMaxLoopCount(10).build().start();
		
	}
}
