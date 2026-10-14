package gkuri.anylib.logs;

public class LogLevel implements Comparable<LogLevel>{

	private int logLevel;
	private String lavel;
	private boolean isError;
	
	private boolean locked = false;
	
	@SuppressWarnings("unused")
	private LogLevel() {
		
	}
	
	public LogLevel(int logLevel, String lavel) {
		this(logLevel, lavel, false);
	}
	
	public LogLevel(int logLevel, String lavel, boolean isError) {
		this.logLevel = logLevel;
		this.lavel = lavel;
		this.isError = isError;
	}
	
	public int getLogLevel() {
		return this.logLevel;
	}
	
	public String getLavel() {
		return this.lavel;
	}
	
	public boolean isError() {
		return this.isError;
	}
	
	public void setLogLevel(int logLevel) {
		if(!locked) this.logLevel = logLevel;
	}
	
	public void setLavel(String lavel) {
		if(!locked) this.lavel = lavel;
	}
	
	public void setError(boolean isError) {
		if(!locked) this.isError = isError;
	}
	
	public void lock() {
		this.locked = true;
	}
	
	@Override
	public int compareTo(LogLevel l) {
		return Integer.compare(logLevel, l.getLogLevel());
	}
	
	@Override
	public String toString() {
		return "LogLevel [ logLevel : " + logLevel + " || lavel : " + lavel + " || isError : " + isError + " ]";
	}
}
