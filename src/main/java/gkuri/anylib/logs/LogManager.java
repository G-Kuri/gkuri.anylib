package gkuri.anylib.logs;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import gkuri.anylib.logs.events.LogEvent;

public class LogManager implements Closeable {
	
	public static final LogLevel TRACE_LEVEL = new LogLevel(0x10000000, "TRACE");
	public static final LogLevel DEBUG_LEVEL = new LogLevel(0x20000000, "DEBUG");
	public static final LogLevel INFO_LEVEL = new LogLevel(0x30000000, "INFO");
	public static final LogLevel WARNING_LEVEL = new LogLevel(0x40000000, "WARNING");
	public static final LogLevel ERROR_LEVEL = new LogLevel(0x50000000, "ERROR", true);
	
	public static final String DEFAULT_LOG_FORMAT = "index : %i || code : 0x%xc || level : [ %l ] || time : %T || coller : %C || %t : %m || object : [ %o ]";
	
	private long logIndex = 0;
	private Map<Long, Log> logsMap = new ConcurrentHashMap<>();
	private int maxLogCount = 1;
	private boolean consoleOutputWhenAdding = true;
	private int printLogLevel = 0x30000000;
	private List<LogEvent> logEventListeners = new CopyOnWriteArrayList<LogEvent>();
	
	private LogPrinter logPrinter;
	
	private String logFormat = DEFAULT_LOG_FORMAT;

	public LogLevel defaultLogLevel = INFO_LEVEL;
	
	public LogManager() {
		this(1);
	}
	
	public LogManager(int maxLogCount) {
		setMaxLogCount(maxLogCount);
	}
	
	public LogManager(File logFile) throws IOException {
		this(logFile, 1);
	}
	
	public LogManager(File logFile, int maxLogCount) throws IOException {
		this(maxLogCount);
		this.logPrinter = new LogPrinter(logFile);
	}
	
	public synchronized void addLog(Log log) {
		removeLog();
		
		log.setIndex(this.logIndex++);
		if(log.getLogLevel() == null) log.setLogLevel(defaultLogLevel);
		
		logsMap.put(log.getIndex(), log);
		
		if(consoleOutputWhenAdding && isPrintLevel(log)) printLog(log);
		if(logPrinter != null) logPrinter.write(log);
	}
	
	public synchronized Log getLog(long targetIndex) {
	    return this.logsMap.get(targetIndex);
	}

	public synchronized Log getLastLog(){
		List<Log> logs = getAllLogs();
		if(logs.isEmpty()) return null;
		logs.sort((a, b) -> Long.compare(a.getIndex(), b.getIndex()));
		return logs.getLast();
	}
	
	public synchronized List<Log> getAllLogs() {
	    return new ArrayList<>(logsMap.values());
	}
	
	public synchronized void printLog() {
		printLog(this.logIndex - 1);
	}
	
	public synchronized void printLog(long index) {
		printLog(getLog(index));
	}
	
	public void printLog(Log log) {
		if(log == null) return;
		if(log.getLogLevel().isError()) {
			System.err.println(log);
		} else {
			System.out.println(log);
		}
		for(LogEvent l : logEventListeners) {
			try {
				l.logPrintEvent(log);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getLogFormat() {
		return this.logFormat;
	}
	
	public synchronized void setLogFormat(String format) {
		this.logFormat = format;
	}
	
	public synchronized void setMaxLogCount(int max) {
	    if(0 < max && max < 2_100_000_000) maxLogCount = max;
	}
	
	public synchronized void setPrintLogLevel(int level) {
		printLogLevel = level;
	}
	
	public synchronized void setPrintLogLevel(LogLevel logLevel) {
		this.printLogLevel = logLevel.getLogLevel();
	}
	
	public boolean getConsoleOutputWhenAdding() {
		return consoleOutputWhenAdding;
	}
	
	public void toggleConsoleOutputWhenAdding() {
	    consoleOutputWhenAdding = !consoleOutputWhenAdding;
	}
	
	private void removeLog() {
		if(logsMap.size() <= maxLogCount) return;
		
		List<Log> logs = getAllLogs();
		logs.sort((a, b) -> Long.compare(a.getIndex(), b.getIndex()));
	    while (maxLogCount < logs.size()) {
	    	Log target = logs.removeFirst();
	    	for(LogEvent l : logEventListeners) {
	    		l.logRemoveEvent(target);
	    	}
	        removeLog(target.getIndex());
	    }
	}
	
	public synchronized void removeLog(long index) {
		this.logsMap.remove(index);
	}
	
	private boolean isPrintLevel(Log log) {
		return printLogLevel <= log.getLogLevel().getLogLevel();
	}
	
	public void setLogfile(File logFile) throws IOException {
		if(logPrinter != null) { 
			logPrinter.setLogFile(logFile);
		} else {
			this.logPrinter = new LogPrinter(logFile);
		}
	}
	
//	public File getLogFile() {
//		return this.logFile;
//	}
	
	public void addLogEventListener(LogEvent listener) {
		logEventListeners.add(listener);
	}
	
	public void removeLogEventListener(LogEvent listener) {
		logEventListeners.remove(listener);
	}
	

	@Override
	public void close() throws IOException {
		if(this.logPrinter == null) return;
		this.logPrinter.close();
	}
	
	private class LogPrinter implements Closeable {
		
		private File logFile;
		private BufferedWriter writer;
		private List<Log> writeQueue = Collections.synchronizedList(new ArrayList<>());
		private boolean writeProsessing = false;
		private boolean isClosed = false;
		CountDownLatch latch;
		
		protected LogPrinter(File logFile) throws IOException {
			setLogFile(logFile);
		}
		
		protected synchronized void write(Log log) {
			if(this.logFile == null || this.isClosed) return;
			writeQueue.add(log);
			writeLog();
		}
		
		private synchronized List<Log> clearQueue(){
			List<Log> list = List.copyOf(this.writeQueue);
			this.writeQueue.clear();
			return list;
		}
		
		private void writeLog() {
			if(this.writeProsessing) return;
			
			latch = new CountDownLatch(1);
			this.writeProsessing = true;
			new Thread(() -> {
				
				while (!this.writeQueue.isEmpty()) {
					List<Log> list = clearQueue();
					
					for(Log log : list) {
						try {
							writer.write(log.toString());
							writer.newLine();
							writer.flush();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				latch.countDown();
				this.writeProsessing = false;
			}).start();
		}
		
		protected synchronized void setLogFile(File logFile) throws IOException {
			if(logFile != null) {
				if(!logFile.exists()) {
					//ファイルがない時
					try {
						if(logFile.createNewFile()) {
							System.out.println("create new log file.");
						} else {
							throw new IllegalArgumentException("file not found and can't create.");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if(!logFile.isFile()){
					throw new IllegalArgumentException(logFile.getName() + " is dir.");
				} else if(!logFile.canWrite()) {
					throw new IllegalArgumentException(logFile.getName() + " can't write.");
				}
			}
			this.logFile = logFile;
			this.close();
			this.writer = new BufferedWriter(new FileWriter(this.logFile, true));
			this.isClosed = false;
		}
		
		@Override
		public void close() throws IOException {
			this.isClosed = true;
			try {
				while (!this.writeQueue.isEmpty()) {
					this.writeLog();
					this.latch.await(10_000, TimeUnit.MILLISECONDS);
				}
				this.writer.close();
			} catch (InterruptedException | IOException e) {
				this.writer.close();
				throw new RuntimeException(e);
			}
		}
	}

}
