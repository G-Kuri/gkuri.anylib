package eventbus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import gkuri.anylib.eventbus.EventDispatcher;
import gkuri.anylib.eventbus.annotation.ListenerClass;

@ListenerClass(TestLis.class)
public class TestEventDis implements EventDispatcher<TestLis> {

	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	
	private final String key;
	public TestEventDis(String key) {
		this.key = key;
	}
	
	@Override
	public void addEventListener(TestLis listener) {
		this.service.schedule(() -> {
			System.out.print(key + " ");
			listener.on();
			this.service.shutdown();
		}, 1, TimeUnit.SECONDS);
	}

}
