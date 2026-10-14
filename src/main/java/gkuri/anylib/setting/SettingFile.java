package gkuri.anylib.setting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gkuri.anylib.file.TextFileOperator;
import gkuri.anylib.setting.fileTypes.FileType;
import gkuri.anylib.setting.fileTypes.SettingJSON;
import gkuri.anylib.setting.fileTypes.SettingProperties;
import gkuri.anylib.setting.fileTypes.SettingXML;
import gkuri.anylib.utils.cast.ClassCastUtil;
import gkuri.anylib.utils.multiValue.Duo;
/**
 * <p>
 * 	複数の設定ファイルフォーマットを同一APIから操作するための基底クラスです。
 * </p>
 * 可能な操作は以下の通りです。
 * <ul>
 * 	<li>設定ファイルの読み込み</li>
 * 	<li>値の取得</li>
 * 	<li>値の更新</li>
 * 	<li>値の削除</li>
 * 	<li>設定ファイルの上書き保存</li>
 * 	<li>ファイル形式の変換</li>
 * </ul>
 * 
 * ファイル形式は以下をサポートします。
 * <ul>
 * 	<li>properties</li>
 * 	<li>JSON</li>
 * 	<li>XML</li>
 * 	<li>YAML (実装予定)</li>
 * 	<li>TOML (実装予定)</li>
 * </ul>
 * 
 * <p>
 * 子要素は"."(ドット)区切りでアクセスします。
 * </p>
 * <p>
 * 配列要素はキーに"[idx]"をつけてアクセスします。
 * </p>
 * 例
 * <ul>
 * 	<li>parent.child</li>
 * 	<li>array[0]</li>
 * 	<li>parent.child.arr[2]</li>
 * 	<li>arrarr[1][3]</li>
 * </ul>
 * 
 */
public abstract class SettingFile {
	
	protected ConcurrentHashMap<String,Object> objectMap = new ConcurrentHashMap<String, Object>();
	protected boolean isChanged = false;
	protected boolean keyChanged = false;
	
	protected boolean isResourcesFile = false;
	
	private static final char OPEN_ARR_IDX = '[';
	private static final char CLOSE_ARR_IDX = ']';
	
	/**
	 * 指定されたファイルパスのファイルを読み込み、SettingFileのインスタンスを返します。
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static SettingFile loadSetting(String filePath) throws IOException {
		return loadSetting(new TextFileOperator(filePath));
	}
	
	/**
	 * 指定されたTextFileOperatorのファイルを読み込み、SettingFileのインスタンスを返します。
	 * @param tfo
	 * @return
	 * @throws IOException
	 */
	public static SettingFile loadSetting(TextFileOperator tfo) throws IOException {
		String fileNameToLow = tfo.getName().toLowerCase();
		for(FileType type : FileType.values()) {
			if(fileNameToLow.endsWith("." + type.getName())) {
				return loadSetting(tfo, type);
			}
		}
		throw new IllegalArgumentException(fileNameToLow + " is not supported");
	}
	
	/**
	 * 指定されたファイルパスのファイルを読み込み、SettingFileのインスタンスを返します。<br>
	 * 実ファイルの拡張子にかかわらずfileTypeで指定されたフォーマットとして読み込まれます。
	 * @param filePath
	 * @param fileType
	 * @return
	 * @throws IOException
	 */
	public static SettingFile loadSetting(String filePath, FileType fileType) throws IOException {
		return loadSetting(new TextFileOperator(filePath), fileType);
	}
	
	/**
	 * 指定されたTextFileOperatorのファイルを読み込み、SettingFileのインスタンスを返します。<br>
	 * 実ファイルの拡張子にかかわらずfileTypeで指定されたフォーマットとして読み込まれます。
	 * @param tfo
	 * @param fileType
	 * @return
	 * @throws IOException
	 */
	public static SettingFile loadSetting(TextFileOperator tfo, FileType fileType) throws IOException {
		SettingFile result = createSettingInstance(tfo, fileType);
		result.reload();
		return result;
	}
	
	private static SettingFile createSettingInstance(TextFileOperator tfo, FileType fileType) throws IOException {
		if(fileType == null) throw new IllegalArgumentException("file type is null");
		
		SettingFile sf = null;
		switch (fileType) {
			case TYPE_PROPERTIES -> sf = new SettingProperties(tfo);
			case TYPE_JSON -> sf = new SettingJSON(tfo);
			case TYPE_XML -> sf = new SettingXML(tfo);
//			case TYPE_YAML -> sf = null;//暫定
//			case TYPE_TOML -> sf = null;//暫定
			default -> throw new IllegalArgumentException(fileType.getName() + " is not supported");
		}
		
		return sf;
	}
	

	public static SettingFile loadResourcesSetting(String resourcesPath) throws Exception {
		return loadResourcesSetting(resourcesPath, StandardCharsets.UTF_8);
	}
	
	public static SettingFile loadResourcesSetting(String resourcesPath, Charset charsets) throws Exception {
		TextFileOperator tfo = loadResourcesSettingTfo(resourcesPath, charsets);
		String fileNameToLow = tfo.getName().toLowerCase();
		for(FileType type : FileType.values()) {
			if(fileNameToLow.endsWith("." + type.getName())) {
				SettingFile result = createSettingInstance(tfo, type);
				result.isResourcesFile = true;
				result.reload();
				return result;
			}
		}
		throw new IllegalArgumentException(fileNameToLow + " is not supported");
	}
	
	public static SettingFile loadResourcesSetting(String resourcesPath, FileType fileType) throws Exception {
		return loadResourcesSetting(resourcesPath, StandardCharsets.UTF_8, fileType);
	}
	
	public static SettingFile loadResourcesSetting(String resourcesPath, Charset charsets, FileType fileType) throws Exception {
		TextFileOperator tfo = loadResourcesSettingTfo(resourcesPath, charsets);
		SettingFile result = createSettingInstance(tfo, fileType);
		result.isResourcesFile = true;
		result.reload();
		return result;
	}
	
	private static TextFileOperator loadResourcesSettingTfo(String resourcesPath, Charset charsets) throws Exception {
		try (InputStream is = SettingFile.class.getClassLoader().getResourceAsStream(resourcesPath)) {
			if (is == null) {
				throw new IllegalArgumentException("resource file not found(" + resourcesPath + ")");
			}
			
			TextFileOperator tfo = new TextFileOperator("temp-resources/" + resourcesPath);
			tfo.addLine(new String(is.readAllBytes(), charsets));
			
			return tfo;
		} catch(Exception e) {
			throw e;
		}
	}

	/**
	 * 設定ファイルを再読み込みします。
	 * @throws IOException
	 */
	abstract public void reload() throws IOException;
	
	/**
	 * 設定ファイルを上書き保存します。
	 * @throws IOException
	 */
	abstract public void save() throws IOException;
	
	/**
	 * 設定ファイルのTextFileOperatorを返します。
	 * @return
	 */
	abstract public TextFileOperator getTextFileOperator();
	
	/**
	 * インスタンスのFileTypeを返します。
	 * @return
	 */
	abstract public FileType getFileType();
	
	/**
	 * キーに対応する設定値を取得します。
	 * @param key
	 * @return
	 */
	public Object get(String keyTree) {
		return this.getOrRaw(keyTree, false);
	}

	public Object getOrDefault(String keyTree, Object defaultValue) {
		Object result = this.get(keyTree);
		return result != null ? result : defaultValue;
	}
	
	public Object getNonNull(String keyTree) {
		return getOrDefault(keyTree, keyTree);
	}
	
	@SuppressWarnings("unchecked")
	private Object getOrRaw(String keyTree, boolean getRaw) {
		String[] tree = this.getTree(keyTree);
		Object result = this.objectMap;
		for(String nativeKey : tree) {
			Duo<String, List<Integer>> keyIdx = parseKey(nativeKey);
			if(result instanceof Map) {
				Map<String, Object> resultMap = (Map<String, Object>) result;
				result = resultMap.get(keyIdx.getFirst());
				if(result == null) return null;
				if(result instanceof List<?>) {
					for(int idx : keyIdx.getSecond()) {
						List<?> resultList = (List<?>) result;
						if(resultList.size() <= idx) return null;
						result = resultList.get(idx);
					}
				}
			} else {
				return null;
			}
		}
		
		if(!getRaw) {
			if(result instanceof Map) {
				return Collections.unmodifiableMap((Map<String, Object>) result);
			} else if(result instanceof List) {
				return Collections.unmodifiableList((List<Object>)result);
			}
		}
		
		return result;
	}
	
	/**
	 * キーに対応する設定値を追加又は上書きします。
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void put(String key, Object value) {
		if(this.isResourcesFile) throw new UnsupportedOperationException("this setting file is resource file.");
		if(Objects.equals(this.get(key),value)) return;
		List<String> keys = new ArrayList<>();
		String[] keyTree = getTree(key);
		if(keyTree.length <= 1) {
			Duo<String, List<Integer>> keyIdx = this.parseKey(key);
			if(keyIdx.getSecond().size() == 0) {
				this.objectMap.put(key, value);
			} else {
				List<Object> list = (List<Object>)this.objectMap.get(keyIdx.getFirst());
				if(list == null) {
					list = new ArrayList<Object>();
					this.objectMap.put(keyIdx.getFirst(), list);
				}
				for(int i = 0; i < keyIdx.getSecond().size(); i++) {
					int idx = keyIdx.getSecond().get(i);
					while(list.size() <= idx) {
						list.add(null);
					}
					if(value != null && (value.getClass().isArray() || value instanceof List) && (list.get(idx) == null || !(list.get(idx) instanceof List))) {
						list.set(idx, new ArrayList<Object>());
						list = (List<Object>)list.get(idx);
					}
					while(list.size() <= idx) {
						list.add(null);
					}
				}
				list.set(keyIdx.getSecond().getLast(), value);
			}
			
			this.isChanged = true;
			return;
		}
		
		for(String k : keyTree) {
			keys.add(k);
		}
		key = keys.removeLast();
		Duo<String, List<Integer>> keyIdx = this.parseKey(key);
		String parentKey = String.join(".", keys);
		
		Object parent = this.getOrRaw(parentKey, true);
		if(parent == null) {
			Duo<String, List<Integer>> paremtKeyIdx = this.parseKey(keys.getLast());
			if(paremtKeyIdx.getSecond().size() != 0) {
				List<Object> rootList = new ArrayList<>();
				List<Object> list = rootList;
				for(int minSize : paremtKeyIdx.getSecond()) {
					list.add(new ArrayList<Object>(minSize));
					list = (ArrayList<Object>)list.getLast();
				}
				parent = rootList;
			} else {
				parent = new ConcurrentHashMap<String, Object>();
			}
			
			this.put(parentKey, parent);
		} else if(keyIdx.getSecond().size() != 0 && !(parent instanceof List)) {
			List<Object> rootList = new ArrayList<>();
			List<Object> list = rootList;
			for(int minSize : keyIdx.getSecond()) {
				List<Object> childList = new ArrayList<Object>();
				while (childList.size() <= minSize) {
					childList.add(null);
				}
				list.add(childList);
				list = (ArrayList<Object>)list.getLast();
			}
			parent = list;
			this.put(parentKey, rootList);
		} else if(!(parent instanceof Map)) {
			parent = new ConcurrentHashMap<String, Object>();
			this.put(parentKey, parent);
		}
		
		if(keyIdx.getSecond().size() == 0) {
			Map<String, Object> parentMap = (Map<String, Object>)parent;
			parentMap.put(key, value);
		} else {
			List<Object> parentList = (List<Object>)parent;
			int idx = keyIdx.getSecond().getLast();
			while (parentList.size() <= idx) {
				parentList.add(null);
			}
			
			parentList.set(idx, value);
		}
		this.isChanged = true;
	}
	
	/**
	 * キーおよび対応する設定値を削除します。
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public void remove(String key) {
		if(this.isResourcesFile) throw new UnsupportedOperationException("this setting file is resource file.");
		if(this.get(key) == null) return;
		
		List<String> keys = new ArrayList<>();
		String[] keyTree = getTree(key);
		if(keyTree.length <= 1) {
			Duo<String, List<Integer>> keyIdx = this.parseKey(key);
			if(keyIdx.getSecond().size() == 0) {
				this.objectMap.remove(key);
			} else {
				List<Object> list = (List<Object>)this.objectMap.get(keyIdx.getFirst());
				for(int i = 0; i < keyIdx.getSecond().size()-1; i++) {
					int idx = keyIdx.getSecond().get(i);
					if(list.get(idx) == null || !(list.get(idx) instanceof List)) {
						return;
					}
					list = (List<Object>)list.get(idx);
				}
				list.remove(keyIdx.getSecond().getLast());
			}
			this.isChanged = true;
			return;
		}
		
		for(String k : keyTree) {
			keys.add(k);
		}
		key = keys.removeLast();
		Duo<String, List<Integer>> keyIdx = this.parseKey(key);
		String parentKey = String.join(".", keys);
		Object parent = this.getOrRaw(parentKey, true);
		
		if(keyIdx.getSecond().size() != 0) {
			if(!(parent instanceof List))return;
			List<Object> parentList = (List<Object>)parent;
			for(int i = 0; i < keyIdx.getSecond().size()-1; i++) {
				int idx = keyIdx.getSecond().get(i);
				if(parentList.size() < idx) return;
				parentList = (List<Object>)parentList.get(idx);
			}
			parentList.remove(keyIdx.getSecond().getLast());
		} else {
			if(!(parent instanceof Map)) return;
			Map<String, Object> parentMap = (Map<String, Object>)parent;
			parentMap.remove(key);
		}
		this.isChanged = true;
	}
	
	/**
	 * キーの一覧を返します。
	 * 基本的に最上位の親キーのみが返ります。
	 * @return
	 */
	public Set<String> keys(){
		return Collections.unmodifiableSet(this.objectMap.keySet());
	}
	
	/**
	 * 設定ファイルを指定したフォーマットに変換し、変換後のSettingFileインスタンスを返します。
	 * @param fileType
	 * @return
	 * @throws FileNotFoundException 
	 */
	public SettingFile cast(FileType fileType) throws FileNotFoundException {
		if(fileType == null) throw new IllegalArgumentException("file type is null");
		String filePath = this.getTextFileOperator().getAbsolutePath();
		List<String> fp = new ArrayList<>(List.of(filePath.split("\\.")));
		fp.set(fp.size()-1, fileType.getName());
		filePath = String.join(".", fp);
		SettingFile result;
		TextFileOperator dummyTfo = new TextFileOperator(filePath);
		switch (fileType) {
			case TYPE_PROPERTIES -> result = new SettingProperties(dummyTfo);
			case TYPE_JSON -> result = new SettingJSON(dummyTfo);
			case TYPE_XML -> result = new SettingXML(dummyTfo);
			default -> throw new IllegalArgumentException(fileType.getName() + " is not supported type.");
		}
		
		for(String key : this.keys()) {
			result.put(key, this.getOrRaw(key, true));
		}
		
		return result;
	}
	
	/**
	 * get(String key)の値をInteger型で返します。
	 * @param key
	 * @return
	 */
	public Integer getInt(String key) {
		Object raw = get(key);
		if(raw == null) return null;
		if(raw instanceof Integer) return (Integer)raw;
		return ClassCastUtil.stringToNumber(raw.toString()).intValue();
	}
	
	/**
	 * get(String key)の値をLong型で返します。
	 * @param key
	 * @return
	 */
	public Long getLong(String key) {
		Object raw = get(key);
		if(raw == null) return null;
		if(raw instanceof Long) return (Long)raw;
		return ClassCastUtil.stringToNumber(raw.toString()).longValue();
	}
	
	/**
	 * get(String key)の値をFloat型で返します。
	 * @param key
	 * @return
	 */
	public Float getFloat(String key) {
		Object raw = get(key);
		if(raw == null) return null;
		if(raw instanceof Float) return (Float)raw;
		return ClassCastUtil.stringToNumber(raw.toString()).floatValue();
	}
	
	/**
	 * get(String key)の値をDouble型で返します。
	 * @param key
	 * @return
	 */
	public Double getDouble(String key) {
		Object raw = get(key);
		if(raw == null) return null;
		if(raw instanceof Double) return (Double)raw;
		return ClassCastUtil.stringToNumber(raw.toString()).doubleValue();
	}
	
	/**
	 * get(String key)の値をBoolean型で返します。
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key) {
		Object raw = get(key);
		if(raw == null) return null;
		if(raw instanceof Boolean) return (Boolean)raw;
		return ClassCastUtil.stringToBoolean(raw.toString());
	}
	
	/**
	 * get(String key)の値をString型で返します。
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		Object obj = get(key);
		return obj == null ? null : obj.toString();
	}
	
	private String[] getTree(String key) {
		return key.split("\\.");
	}
	
	/**
	 * 引数はgetTreeした後のキーを受け取る前提
	 * @param nativeKey
	 * @return
	 */
	private Duo<String, List<Integer>> parseKey(String nativeKey){
		
		StringBuilder key = new StringBuilder();
		List<Integer> idxs = new ArrayList<>();
		boolean idxInside = false;
		StringBuilder idx = null;
		
		for(char c : nativeKey.toCharArray()) {
			if(idxInside) {
				if(c == CLOSE_ARR_IDX) {
					idxInside = false;
					idxs.add(ClassCastUtil.stringToNumber(idx.toString()).intValue());
					continue;
				}
				idx.append(c);
				continue;
			}
			
			if(c == OPEN_ARR_IDX) {
				idxInside = true;
				idx = new StringBuilder();
				continue;
			} 
			key.append(c);
		}
		
		return new Duo<String, List<Integer>>(key.toString(), idxs);
	}
}
