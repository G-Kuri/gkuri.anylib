package gkuri.anylib.logs.events;

import gkuri.anylib.logs.Log;

public interface LogEvent {
	
	default public void logAddEvent(Log log) {
		
	}
	
	default public void logRemoveEvent(Log log) {
		
	}
	
	default public void logPrintEvent(Log log) {
		
	}
}
