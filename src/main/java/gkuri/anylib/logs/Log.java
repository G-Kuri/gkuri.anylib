package gkuri.anylib.logs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.utils.multiValue.Duo;

public class Log {
	
	public static DateTimeFormatter LOG_DATE_TIME_FORMATT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS");
	
	public static final String LOG_LEVEL_PARSER = "%l";
	public static final String INDEX_PARSER_10 = "%i";
	public static final String INDEX_PARSER_16 = "%xi";
	public static final String LOG_CODE_PARSER_10 = "%c";
	public static final String LOG_CODE_PARSER_16 = "%xc";
	public static final String TITLE_PARSER = "%t";
	public static final String MESSAGE_PARSER = "%m";
	public static final String OBJECT_PARSER = "%o";
	public static final String TIME_PARSER = "%T";
	public static final String COLLER_PARSER = "%C";

	private LogLevel logLevel;
	private long index = 0;
	private int code;
	private String title;
	private String message;
	private Object object;
	private LocalDateTime time;
	private String coller;
	
	private String format = null;
	private String parsedFormat = null;
	private List<Object> formatList;
	
	@SuppressWarnings("unused")
	private Log() {}

	public Log(int code){
		this(code, null);
	}

	public Log(String title){
		this(0, title);
	}

	public Log(DefaultLog defaultLog){
		this(defaultLog.getCode(), defaultLog.getTitle(), defaultLog.getMessage(), null, LocalDateTime.now(), defaultLog.getLogLevel());
	}

	public Log(int code, String title){
		this(code, title, null, null);
	}
	
	public Log(int code, String title, LogLevel logLevel){
		this(code, title, null, logLevel);
	}

	public Log(String title, String message){
		this(0, title, message);
	}
	
	public Log(String title, String message, LogLevel logLevel){
		this(0, title, message, logLevel);
	}
	
	public Log(int code, String title, String message){
		this(code, title, message, null, LocalDateTime.now(), null);
	}
	
	public Log(int code, String title, String message, LogLevel logLevel){
		this(code, title, message, null, LocalDateTime.now(), logLevel);
	}
	
	public Log(DefaultLog defaultLog, String message) {
		this(defaultLog.getCode(), defaultLog.getTitle(), message, null, LocalDateTime.now(), defaultLog.getLogLevel());
	}
	
	public Log(DefaultLog defaultLog, String message, Object object) {
		this(defaultLog.getCode(), defaultLog.getTitle(), message, object, LocalDateTime.now(), defaultLog.getLogLevel());
	}

	protected Log(int code, String title, String message, Object object, LocalDateTime time, LogLevel logLevel) {
		this.index = -1L;
		this.code = code;
		this.title = title;
		this.message = message;
		this.object = object;
		this.time = time;
		this.logLevel = logLevel;
		setColler();
	}
	
	public long getIndex() {
		return this.index;
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
	
	public Object getObject() {
		return this.object;
	}
	
	public LocalDateTime getTime() {
		return this.time;
	}
	
	public LogLevel getLogLevel() {
		return this.logLevel;
	}
	
	public String getColler() {
		return this.coller;
	}
	
	protected void setIndex(long index){
		this.index = index;
	}
	
	protected void setLogLevel(LogLevel loglevel) {
		this.logLevel = loglevel;
	}

	private void setColler() {
		this.coller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
		        .walk(stream ->
		            stream
		                .filter(f -> !f.getClassName().startsWith(this.getClass().getPackageName()))
		                .findFirst()
		                .map(f -> f.getClassName() + "." + f.getMethodName())
		                .orElse("unknown")
		        );
	}

	public Log setFromat(String format) {
		
		Duo<String, List<Object>> parsedFormatt = parseFormatt(format);
		if(parsedFormatt == null) {
			this.format = null;
		} else {
			this.parsedFormat = parsedFormatt.getFirst();
			this.formatList = parsedFormatt.getSecond();
		}
		return this;
	}
	
	public String formatt(String formatt) {
		Duo<String, List<Object>> parsedFormatt = parseFormatt(formatt);
		if(parsedFormatt == null) {
			return toString();
		} else {
			return String.format(parsedFormatt.getFirst(), parsedFormatt.getSecond().toArray());
		}
	}
	
	private Duo<String, List<Object>> parseFormatt(String format){
		if(format == null || format.isBlank()) {
			return null;
		}

		if(format.equals(LogManager.DEFAULT_LOG_FORMAT)){
			format = "index : %d || code : 0x%08x || level : [ %s ] || time : %s || coller : %s || %s : %s || object : [ %s ]";
			List<Object> list = List.of(
					this.index, 
					this.code, 
					this.logLevel != null ? this.logLevel.getLavel() : LogManager.INFO_LEVEL.getLavel(), 
					this.time != null ? this.time.format(LOG_DATE_TIME_FORMATT) : LocalDateTime.now().format(LOG_DATE_TIME_FORMATT), 
					this.coller, 
					this.title != null ? this.title : "null", 
					this.message != null ? this.message : "null", 
					this.object != null ? this.object : "null");
			return new Duo<>(format, list);
		}
		
		StringBuilder resultFormat = new StringBuilder();
		
		ArrayList<Integer> escList = new ArrayList<>();
		int escIdx = format.indexOf('%');
		while(escIdx != -1) {
			escList.add(escIdx);
			escIdx = format.indexOf('%', escIdx + 1);
		}
		
		ArrayList<Object> objList = new ArrayList<>();
		int beforeIdx = 0;
		int nextIdx = 3;
		for(int e : escList) {
			resultFormat.append(format.substring(beforeIdx, e));
			
			int endIdx = Math.min(e + 3, format.length());
			String p = format.substring(e, endIdx);
			String f = "%";
			nextIdx = 1;
			
			if(p.startsWith(INDEX_PARSER_16)) {
				objList.add(this.index);
				f = "%x";
				nextIdx = INDEX_PARSER_16.length();
			} else if(p.startsWith(LOG_CODE_PARSER_16)) {
				objList.add(this.code);
				f = "%08x";
				nextIdx = LOG_CODE_PARSER_16.length();
			} else if(p.startsWith(LOG_LEVEL_PARSER)) {
				objList.add(this.logLevel.getLavel());
				f = "%s";
				nextIdx = LOG_LEVEL_PARSER.length();
			} else if(p.startsWith(INDEX_PARSER_10)) {
				objList.add(this.index);
				f = "%d";
				nextIdx = INDEX_PARSER_10.length();
			} else if(p.startsWith(LOG_CODE_PARSER_10)) {
				objList.add(this.code);
				f = "%d";
				nextIdx = LOG_CODE_PARSER_10.length();
			} else if(p.startsWith(TITLE_PARSER)) {
				objList.add(this.title);
				f = "%s";
				nextIdx = TITLE_PARSER.length();
			} else if(p.startsWith(MESSAGE_PARSER)) {
				objList.add(this.message);
				f = "%s";
				nextIdx = MESSAGE_PARSER.length();
			} else if(p.startsWith(OBJECT_PARSER)) {
				objList.add(this.object);
				f = "%s";
				nextIdx = OBJECT_PARSER.length();
			} else if(p.startsWith(TIME_PARSER)) {
				objList.add(this.time.format(LOG_DATE_TIME_FORMATT));
				f = "%s";
				nextIdx = TIME_PARSER.length();
			} else if(p.startsWith(COLLER_PARSER)) {
				objList.add(this.coller);
				f = "%s";
				nextIdx = COLLER_PARSER.length();
			}
			
			resultFormat.append(f);
			beforeIdx = e + nextIdx;
		}
		
		if(beforeIdx < format.length()) resultFormat.append(format.substring(beforeIdx));
		
		return new Duo<>(resultFormat.toString(), objList);
	}
	
	@Override
	public String toString() {
		if(this.format == null) setFromat(LogManager.DEFAULT_LOG_FORMAT);
		return String.format(parsedFormat, formatList.toArray());
	}
	
}
