package gkuri.anylib.setting.fileTypes;

import gkuri.anylib.setting.SettingFile;

public enum FileType {
	
	TYPE_PROPERTIES(0, "properties", SettingProperties.class),
	TYPE_JSON(1, "json", SettingJSON.class),
	TYPE_XML(2, "xml", SettingXML.class),
//	TYPE_YAML(3, "yaml"),
//	TYPE_TOML(4, "toml")
	;
	
	public final int code;
	public final String name;
	public final Class<? extends SettingFile> setting;
	
	FileType(int code, String name, Class<? extends SettingFile> setting) {
		this.code = code;
		this.name = name;
		this.setting = setting;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Class<? extends SettingFile> getSettingFileClass(){
		return this.setting;
	}
}
