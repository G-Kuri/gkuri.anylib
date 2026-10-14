package eventbus;

import gkuri.anylib.eventbus.annotation.ListenEvent;

@ListenEvent(dispatcher = TestEventDis.class, key = "hoge")
public class TestEventLisWithKey implements TestLis {

	@Override
	public void on() {
		System.out.println("onOnWithHogeKey");
	}

}
