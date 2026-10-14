package eventbus;

import gkuri.anylib.eventbus.annotation.ListenEvent;

@ListenEvent(dispatcher = TestEventDis.class, key = "")
public class TestEventLis implements TestLis {

	@Override
	public void on() {
		System.out.println("onOn");
	}

}
