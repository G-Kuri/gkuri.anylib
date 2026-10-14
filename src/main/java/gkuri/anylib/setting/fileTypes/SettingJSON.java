package gkuri.anylib.setting.fileTypes;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import gkuri.anylib.file.TextFileOperator;
import gkuri.anylib.setting.SettingFile;

public class SettingJSON extends SettingFile {

	private TextFileOperator tfo;

	
	public SettingJSON(TextFileOperator tfo) {
		this.tfo = tfo;
		if(!this.isResourcesFile && !this.tfo.canRead()) {
			throw new IllegalAccessError(this.tfo.getAbsolutePath() + " is cannot read.");
		}
		this.tfo.loadFile();
	}
	
	@Override
	public void reload() {
		objectMap = new ConcurrentHashMap<>(new JSONObject(this.tfo.getAllContent()).toMap());
		isChanged = false;
	}

	@Override
	public void save() throws IOException {
		if(!isChanged) return;
		this.tfo.removeAllLine();
		this.tfo.addLine(new JSONObject(objectMap).toString());
		this.tfo.writeFile();
		isChanged = false;
	}
	
	@Override
	public TextFileOperator getTextFileOperator() {
		return this.tfo;
	}
	
	@Override
	public FileType getFileType() {
		return FileType.TYPE_JSON;
	}

//	@Override
//	public Object get(String key) {
//		String[] tree = getTree(key);
//		Object obj = getJsonParentObj(tree);
//		try {
//			if(obj instanceof JSONArray) {
//				JSONArray ja = (JSONArray)obj;
//				try {
//					Number num = ClassCastUtil.stringToNumber(tree[tree.length-1]);
//					if(num != null) {
//						int idx = num.intValue();
//						return ja.get(idx);
//					}
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//				}
//				
//				return null;
//			} else {
//				JSONObject jo = (JSONObject)obj;
//				Object result = jo.get(tree[tree.length-1]);
//				if(result instanceof JSONObject) {
//					return createKey(key, (JSONObject)result);
//				} else if(result instanceof JSONArray) {
//					List<Object> list = new ArrayList<>();
//					for(Object v : (JSONArray)result) {
//						list.add(v);
//					}
//					return list;
//				}
//				return result != null ? result : null;
//			}
//			
//		} catch (Exception e) {
//			return null;
//		}
//	}
//
//	@Override
//	public void put(String key, Object value) {
//		if(this.get(key) == null) keyChanged = true;
//		String[] tree = getTree(key);
//		Object obj = getJsonParentObjOrAdd(tree);
//		if(obj instanceof JSONArray) {
//			JSONArray ja = (JSONArray)obj;
//			ja.put(ClassCastUtil.stringToNumber(tree[tree.length - 1]).intValue(), value);
//			isChanged = true;
//		} else {
//			JSONObject jo = (JSONObject)obj;
//			if(value != null && (value.getClass().isArray())) {
//				jo.put(tree[tree.length - 1], new JSONArray(value));
//			} else if(value instanceof Collection<?>) {
//				jo.put(tree[tree.length - 1], new JSONArray((Collection<?>)value));
//			} else {
//				jo.put(tree[tree.length - 1], value);
//			}
//			isChanged = true;
//		}
//	}
//
//	@Override
//	public void remove(String key) {
//		String[] tree = getTree(key);
//		Object obj = getJsonParentObj(tree);
//		if(obj == null) return;
//		if(obj instanceof JSONArray) {
//			JSONArray ja = (JSONArray) obj;
//			ja.remove(ClassCastUtil.stringToNumber(tree[tree.length - 1]).intValue());
//		} else {
//			JSONObject jo = (JSONObject) obj;
//			jo.remove(tree[tree.length - 1]);
//			keyChanged = true;
//		}
//		isChanged = true;
//	}
//	
//	@Override
//	public Set<String> keys() {
//		if(!keyChanged) return this.keys;
//		Set<String> keys = new HashSet<>();
//		for(Object key : this.jsonObject.keySet()) {
//			String strKey = key.toString();
//			Object value = this.jsonObject.get(strKey);
//			if(value instanceof JSONObject) {
//				keys.addAll(createKey(strKey, (JSONObject)value));
//			} else if(value instanceof JSONArray) {
//				JSONArray ja = (JSONArray)value;
//				String k = strKey + ".";
//				for(int i = 0; i < ja.length(); i++) {
//					keys.add(k + i);
//				}
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
//	
//	private Set<String> createKey(String baseKey, JSONObject baseObj){
//		Set<String> keys = new HashSet<>();
//		baseKey += ".";
//		for(Object key : baseObj.keySet()) {
//			String strKey = key.toString();
//			Object value = baseObj.get(strKey);
//			if(value instanceof JSONObject) {
//				keys.addAll(createKey(baseKey + strKey, (JSONObject)value));
//			} else {
//				keys.add(baseKey + strKey);
//			}
//		}
//		return keys;
//	}
//
//	private Object getJsonParentObj(String[] tree) {
//		try {
//			if(tree.length == 1) {
//				return this.jsonObject;
//			}
//			Object nodeObj = this.jsonObject;
//			for(int i = 0; i < tree.length - 1; i++) {
//				if(nodeObj == null) return null;
//				if(nodeObj instanceof JSONArray) {
//					JSONArray ja = (JSONArray) nodeObj;
//					try {
//						Number num = ClassCastUtil.stringToNumber(tree[i]);
//						if(num != null) {
//							int idx = num.intValue();
//							return ja.get(idx);
//						}
//					} catch (NumberFormatException e) {
//					}
//					
//					return null;
//				} else {
//					JSONObject jo = (JSONObject)nodeObj;
//					nodeObj = jo.get(tree[i]);
//				}
//			}
//			
//			return nodeObj;
//		} catch (JSONException e) {
//			return null;
//		}
//	}
//	
//	private Object getJsonParentObjOrAdd(String[] tree) {
//		if(tree.length == 1) return this.jsonObject;
//		
//		Object obj = this.jsonObject.optJSONObject(tree[0]);
//		if(obj == null) {
//			this.jsonObject.put(tree[0], new JSONObject());
//			obj = this.jsonObject.getJSONObject(tree[0]);
//		}
//		
//		for(int i = 1; i < tree.length - 1; i++) {
//			if(obj instanceof JSONArray) {
//				JSONArray ja = (JSONArray)obj;
//				Number num = ClassCastUtil.stringToNumber(tree[i]);
//				if(num != null) {
//					int idx = num.intValue();
//					if(ja.length() <= idx) {
//						while (ja.length() <= idx) {
//						    ja.put("");
//						}
//					}
//					obj = ja.get(idx);
//				}
//			} else {
//				JSONObject jo = (JSONObject)obj;
//				Object chiledObj = jo.opt(tree[i]);
//				if(chiledObj == null) {
//					jo.put(tree[i], new JSONObject());
//					chiledObj = jo.get(tree[i]);
//				}
//				obj = chiledObj;
//			}
//			
//		}
//		return obj;
//	}
//	
//	
}
