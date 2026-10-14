package logs;

import java.io.File;
import java.io.IOException;

import gkuri.anylib.logs.DefaultLog;
import gkuri.anylib.logs.Log;
import gkuri.anylib.logs.LogManager;

public class Test {
	
	public static final DefaultLog ERROR = new DefaultLog(0, "error log", LogManager.ERROR_LEVEL);
	public static final DefaultLog WERN = new DefaultLog(1, "warn log", LogManager.WARNING_LEVEL);
	public static final DefaultLog INFO = new DefaultLog(2, "info log", LogManager.INFO_LEVEL);
	public static final DefaultLog DEBUG = new DefaultLog(3, "debug log", LogManager.DEBUG_LEVEL);
	public static final DefaultLog TRACE = new DefaultLog(4, "trace log", LogManager.TRACE_LEVEL);
	
	public static void main(String[] args) throws IOException {
		
//		LogManagerBuilder logManagerBuilder = new LogManagerBuilder();
		
		LogManager logManager = new LogManager();
		LogManager historyLogManager = new LogManager(10);
		LogManager outputLogManager = new LogManager(new File("D:\\VsCode\\test\\logs.log"));
		
		logManager.addLog(new Log("asdf"));
		logManager.addLog(new Log("qwer"));
		
		for(Log l : logManager.getAllLogs()) {
			System.out.println(l);
		}
		
		for(int i = 0; i < 12; i++) {
			historyLogManager.addLog(new Log(INFO, "his %d".formatted(i)));
		}
		
		for(int i = 0; i < 12; i++) {
			outputLogManager.addLog(new Log(INFO, "out %d".formatted(i)));
		}
	}
}
