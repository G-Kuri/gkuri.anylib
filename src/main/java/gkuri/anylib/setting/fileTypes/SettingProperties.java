package gkuri.anylib.setting.fileTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import gkuri.anylib.file.TextFileOperator;
import gkuri.anylib.setting.SettingFile;

public class SettingProperties extends SettingFile {

	private TextFileOperator tfo;
	
	public SettingProperties(TextFileOperator tfo) {
		this.tfo = tfo;
		this.tfo.loadFile();
	}
	
	@Override
	public void reload() throws IOException {
		System.out.println(this.isResourcesFile);
		if(!this.isResourcesFile && !this.tfo.canRead()) {
			throw new IllegalAccessError(this.tfo.getAbsolutePath() + " is cannot read.");
		}
		Properties properties = new Properties();
		properties.load(tfo.getReader());
		
		List<String> keys = new ArrayList<>(properties.stringPropertyNames());
		keys.sort(String::compareTo);
		
		boolean isResourcesFile = this.isResourcesFile;
		this.isResourcesFile = false;
		for (String key : keys) {
		    this.put(key, properties.getProperty(key));
		}
		this.isResourcesFile = isResourcesFile;
	}

	@Override
	public void save() throws IOException {
		if(this.isResourcesFile || !isChanged) return;
		StringBuilder sb = new StringBuilder();
		for(String key : keys()) {
			sb.append(this.createKeyValue(key));
		}
		this.tfo.removeAllLine();
		this.tfo.addLine(sb.toString());
		this.tfo.writeFile();
		isChanged = false;
	}
	
	@Override
	public TextFileOperator getTextFileOperator() {
		return this.tfo;
	}
	
	@Override
	public FileType getFileType() {
		return FileType.TYPE_PROPERTIES;
	}
	
	@SuppressWarnings("unchecked")
	private String createKeyValue(String key) {
		Object value = get(key);
		if(value instanceof Map) {
			StringBuilder sb = new StringBuilder();
			Map<String, Object> valueMap = (Map<String, Object>) value;
			for(String k : valueMap.keySet()) {
				String keyValue = createKeyValue(k);
				if(keyValue != null) {
					sb.append(keyValue).append("\n");
				}
			}
			return sb.toString();
		} else if(value instanceof List) {
			StringBuilder sb = new StringBuilder();
			List<Object> valueList = (List<Object>) value;
			for(int i = 0; i < valueList.size(); i++) {
				String keyValue = createKeyValue(key + "[%d]".formatted(i));
				if(keyValue != null) {
					sb.append(keyValue).append("\n");
				}
			}
			return sb.toString();
		} else {
			return "%s=%s\n".formatted(key, value);
		}
	}

//	@Override
//	public Object get(String key) {
//		return this.properties.get(key);
//	}
//
//	@Override
//	public void put(String key, Object value) {
//		if(this.properties.get(key) == null) this.keyChanged = true;
//		this.properties.put(key, String.valueOf(value));
//		this.isChanged = true;
//	}
//
//	@Override
//	public void remove(String key) {
//		this.properties.remove(key);
//		this.isChanged = true;
//		this.keyChanged = true;
//	}
//	
//	@Override
//	public Set<String> keys() {
//		if(!keyChanged) return this.keys;
//		Set<String> result = new HashSet<>();
//		for(Object key : this.properties.keySet()) {
//			result.add(key.toString());
//		}
//		this.keys = result;
//		this.keyChanged = false;
//		return result;
//	}
	
}
