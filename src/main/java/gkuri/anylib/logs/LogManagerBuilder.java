package gkuri.anylib.logs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.logs.events.LogEvent;

public class LogManagerBuilder {
    
    private String logFormat = "index : %i || code : 0x%xc || level : [ %l ] || time : %T || coller : %C || %t : %m || object : [ %o ]";
    private int holdLogsCount = 1;
    private boolean consoleOutputWhenAdding = true;
    private LogLevel printLevel = LogManager.INFO_LEVEL;
    private final List<LogEvent> logEventListeners = new ArrayList<>();
    private File logFile; 

    private boolean isUsed = false;

    public LogManagerBuilder(){

    }

    public LogManager build() throws IOException{
        if(isUsed){
        	throw new IllegalStateException("this builder is used.");
        }

        LogManager logManager = new LogManager(logFile, holdLogsCount);
        logManager.setLogFormat(logFormat);
        if(logManager.getConsoleOutputWhenAdding() != consoleOutputWhenAdding) logManager.toggleConsoleOutputWhenAdding();
        logManager.setPrintLogLevel(printLevel);
        for(LogEvent e : logEventListeners) {
        	logManager.addLogEventListener(e);
        }
        
        return logManager;
    }

    
}
