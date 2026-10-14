package gkuri.anylib.communication;

import java.time.ZoneId;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CommunicationData<T> {
	
	public static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).addModule(new Jdk8Module()).build();

	private boolean locked = false;
	
	private long versionId = 1L;
	private MetaData metaData = null;
	private String token = "";
	private T data = null;
	private Class<?> dataClass = null;
	
	public CommunicationData() {
		this.metaData = MetaData.getDefault();
	}
	
	public CommunicationData(T data) {
		this();
		this.setData(data);
	}
	
	public long getVersionId() {
		return this.versionId;
	}
	
	public MetaData getMetaData() {
		return this.metaData;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public T getData() {
		return this.data;
	}
	
	public Class<?> getDataClass(){
		return this.dataClass;
	}
	
	public boolean isLocked() {
		return this.locked;
	}
	
	public CommunicationData<T> setToken(String token) {
		if(this.locked) return this;
		this.token = token;
		return this;
	}
	
	public CommunicationData<T> setData(T data) {
		if(this.locked) return this;
		this.dataClass = data.getClass();
		this.data = data;
		return this;
	}
	
	@SuppressWarnings("unused")
	private void setMetaData(MetaData metaData) {
		if(this.locked) return;
		this.metaData = metaData;
	}
	
	@Override
	public String toString() {
		return "CommunicationData<%s>[ version:%d || locked:%s || token:%s || data:%s ]".formatted(this.dataClass != null ? this.dataClass.getSimpleName() : "?", this.versionId, this.locked, this.token, this.data != null ? this.data : "null");
	}
	
	public static CommunicationData<?> parseJson(JsonNode rowJson, String clazzName) throws ClassNotFoundException, JsonProcessingException, IllegalArgumentException{
		Class<?> clazz = Class.forName(clazzName);
		JavaType type = mapper.getTypeFactory().constructParametricType(CommunicationData.class, clazz);
		CommunicationData<?> data = mapper.treeToValue(rowJson, type);
		if(!CommunicationNoLockData.class.isAssignableFrom(data.getDataClass())) {
			data.locked = true;
		}
		
		return data;
	}
	
	public static class MetaData {
		
		private static final String JAVA_VERSION = System.getProperty("java.version");
		private static final String OS_NAME = System.getProperty("os.name");
		private static final String OS_VERSION = System.getProperty("os.version");
		private static final String CPU_ARCH = System.getProperty("os.arch");
		private static final String TIMEZONE = ZoneId.systemDefault().getId();
		private static final Locale LOCALE = Locale.getDefault();
		
		private String javaVersion;
		private String osName;
		private String osVersion;
		private String cpuArch;
		private String timezone;
		private Locale locale;
		
		private MetaData() {
			
		}
		
		public String getJavaVersion() {
			return this.javaVersion;
		}
		
		public String getOsName() {
			return this.osName;
		}
		
		public String getOsVersion() {
			return this.osVersion;
		}
		
		public String getCpuArch() {
			return this.cpuArch;
		}
		
		public String getTimezone() {
			return this.timezone;
		}
		
		public Locale getLocale() {
			return this.locale;
		}
		
//		@SuppressWarnings("unused")
//		private void setJaveVersion(String javaVersion) {
//			this.javaVersion = javaVersion;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setOsName(String osName) {
//			this.osName = osName;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setOsVersion(String osVersion) {
//			this.osVersion = osVersion;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setCpuArch(String cpuArch) {
//			this.cpuArch = cpuArch;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setHostName(String hostName) {
//			this.hostName = hostName;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setLocalIp(String localIp) {
//			this.localIp = localIp;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setTimezone(String timezone) {
//			this.timezone = timezone;
//		}
//		
//		@SuppressWarnings("unused")
//		private void setLocale(Locale locale) {
//			this.locale = locale;
//		}
		
		private static MetaData getDefault() {
			MetaData meta = new MetaData();
			meta.javaVersion = JAVA_VERSION;
			meta.osName = OS_NAME;
			meta.osVersion = OS_VERSION;
			meta.cpuArch = CPU_ARCH;
			meta.timezone = TIMEZONE;
			meta.locale = LOCALE;
			return meta;
		}
		
	}
}
