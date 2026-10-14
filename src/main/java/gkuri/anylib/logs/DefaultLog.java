package gkuri.anylib.logs;

public class DefaultLog {
	
	private int code;
	private String title;
	private String message;
	private LogLevel logLevel;
	
	public DefaultLog(int code, String title) {
		this(code, title, LogManager.DEBUG_LEVEL);
	}
	
	public DefaultLog(int code, String title, LogLevel logLevel) {
		this(code, title, null, logLevel);
	}
	
	public DefaultLog(int code, String title, String message, LogLevel logLevel){
		this.code = code;
		this.title = title;
		this.message = message;
		this.logLevel = logLevel;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public LogLevel getLogLevel() {
		return this.logLevel;
	}

}