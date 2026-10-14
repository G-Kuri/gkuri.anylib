package gkuri.anylib.eventbus;

import gkuri.anylib.eventbus.annotation.ListenerClass;

@ListenerClass(EventListener.class)
public interface EventDispatcher<T extends EventListener> {

	public void addEventListener(T listener);
	
}
