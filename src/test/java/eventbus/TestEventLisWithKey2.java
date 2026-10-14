package eventbus;

import gkuri.anylib.eventbus.annotation.ListenEvent;

@ListenEvent(dispatcher = TestEventDis.class, key = "fuga")
public class TestEventLisWithKey2 implements TestLis {

	@Override
	public void on() {
		System.out.println("onOnWithFugaKey");
	}

}
