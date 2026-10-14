package eventbus;

import gkuri.anylib.eventbus.EventListener;
import gkuri.anylib.eventbus.annotation.ListenEvent;

@ListenEvent(dispatcher = TestEventDis.class, key = "")
public interface TestLis extends EventListener {

	public void on();
	
}
