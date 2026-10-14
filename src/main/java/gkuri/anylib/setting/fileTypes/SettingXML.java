package gkuri.anylib.setting.fileTypes;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import gkuri.anylib.file.TextFileOperator;
import gkuri.anylib.setting.SettingFile;

public class SettingXML extends SettingFile {

	private TextFileOperator tfo;
	
	private static final XmlMapper mapper = new XmlMapper();
	private String rootName = "root";
	
	public SettingXML(TextFileOperator tfo) {
		this.tfo = tfo;
		if(!this.isResourcesFile && !this.tfo.canRead()) {
			throw new IllegalAccessError(this.tfo.getAbsolutePath() + " is cannot read.");
		}
		this.tfo.loadFile();
	}
	
	@Override
	public void reload() throws IOException {
		objectMap = mapper.readValue(this.tfo.getReader(), new TypeReference<ConcurrentHashMap<String, Object>>() {});
		isChanged = false;
	}

	@Override
	public void save() throws IOException {
		if(!isChanged) return;
		String xml = mapper.writer().withRootName(this.rootName).writeValueAsString(objectMap);
		this.tfo.removeAllLine();
		this.tfo.addLine(xml);
		this.tfo.writeFile();
		isChanged = false;
	}
	
	@Override
	public FileType getFileType() {
		return FileType.TYPE_XML;
	}

	@Override
	public TextFileOperator getTextFileOperator() {
		return this.tfo;
	}
	
	public String getRootName() {
		return this.rootName;
	}
	
	public void setRootName(String rootName) {
		if(rootName != null && !rootName.isBlank()) {
			this.rootName = rootName;
		}
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public Object get(String key) {
//		String[] tree = getTree(key);
//		Map<String, Object> parent = getParentNode(tree);
//		if(parent == null) return null;
//		if(parent.get(tree[tree.length -1]) instanceof Map) return createKey(key, (Map<String, Object>)parent.get(tree[tree.length -1]));
//		return parent.get(tree[tree.length -1]);
//	}

//	@Override
//	public void put(String key, Object value) {
//		if(this.get(key) == null) keyChanged = true;
//		String[] tree = getTree(key);
//		Map<String, Object> obj = getParentNodeOrAdd(tree);
//		obj.put(tree[tree.length - 1], value);
//		isChanged = true;
//	}

//	@Override
//	public void remove(String key) {
//		if(this.get(key) == null) keyChanged = true;
//		String[] tree = getTree(key);
//		Map<String, Object> obj = getParentNodeOrAdd(tree);
//		obj.remove(tree[tree.length -1]);
//		isChanged = true;
//	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public Set<String> keys() {
//		if(!keyChanged) return this.keys;
//		Set<String> keys = new HashSet<>();
//		for(Object key : this.xmlMap.keySet()) {
//			String strKey = key.toString();
//			Object value = this.xmlMap.get(strKey);
//			if(value instanceof Map) {
//				keys.addAll(createKey(strKey, (Map<String, Object>)value));
//			} else {
//				keys.add(strKey);
//			}
//		}
//		this.keys = keys;
//		this.keyChanged = false;
//		return keys;
//	}
//
//	
//	@SuppressWarnings("unchecked")
//	private Set<String> createKey(String baseKey, Map<String, Object> baseMap){
//		Set<String> keys = new HashSet<>();
//		baseKey += ".";
//		for(Object key : baseMap.keySet()) {
//			String strKey = key.toString();
//			Object value = baseMap.get(strKey);
//			if(value instanceof Map) {
//				keys.addAll(createKey(baseKey + strKey, (Map<String, Object>)value));
//			} else {
//				keys.add(baseKey + strKey);
//			}
//		}
//		return keys;
//	}
//	
//	@SuppressWarnings("unchecked")
//	private Map<String, Object> getParentNode(String[] tree) {
//		try {
//			if(tree.length == 1) {
//				return this.xmlMap;
//			}
//			Map<String, Object> parent = this.xmlMap;
//			for(int i = 0; i < tree.length - 1; i++) {
//				Object node = parent.get(tree[i]);
//				if(node == null || !(node instanceof Map)) return null;
//				parent = (Map<String, Object>)node;
//			}
//			return parent;
//		} catch (JSONException e) {
//			return null;
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	private Map<String, Object> getParentNodeOrAdd(String[] tree) {
//		if(tree.length == 1) return this.xmlMap;
//		
//		Object obj = this.xmlMap.get(tree[0]);
//		if(obj == null || !(obj instanceof Map)) {
//			this.xmlMap.put(tree[0], new LinkedHashMap<String, Object>());
//			obj = this.xmlMap.get(tree[0]);
//		}
//		
//		Map<String, Object> objMap = (LinkedHashMap<String, Object>)obj;
//		for(int i = 1; i < tree.length - 1; i++) {
//			Object chiledObj = objMap.get(tree[i]);
//			if(chiledObj == null || !(chiledObj instanceof Map)) {
//				objMap.put(tree[i], new LinkedHashMap<String, Object>());
//				chiledObj = objMap.get(tree[i]);
//			}
//			
//			objMap = (LinkedHashMap<String, Object>)chiledObj;
//		}
//		return objMap;
//	}
//	
//	private String[] getTree(String key) {
//		return key.split("\\.");
//	}

}
