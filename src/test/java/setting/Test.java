package setting;

import java.io.IOException;

import gkuri.anylib.file.TextFileOperator;
import gkuri.anylib.setting.SettingFile;
import gkuri.anylib.setting.fileTypes.FileType;

public class Test {

	public static void main(String[] args) throws IOException {
		TextFileOperator propertiesTfo = new TextFileOperator(System.getProperty("user.dir"), "temp.properties");
		propertiesTfo.addLine(fakeProperties);
		TextFileOperator jsonTfo = new TextFileOperator(System.getProperty("user.dir"), "temp.json");
		jsonTfo.addLine(fakeJson);
		TextFileOperator xmlTfo = new TextFileOperator(System.getProperty("user.dir"), "temp.xml");
		xmlTfo.addLine(fakeXml);
		
		SettingFile propertiesSetting = SettingFile.loadSetting(propertiesTfo);
		SettingFile jsonSetting = SettingFile.loadSetting(jsonTfo);
		SettingFile xmlSetting = SettingFile.loadSetting(xmlTfo);
		
		System.out.println("properties key : " + propertiesSetting.keys());
		System.out.println("json key : " + jsonSetting.keys());
		System.out.println("xml key : " + xmlSetting.keys());
		
		System.out.println("pro : intVal = " + propertiesSetting.get("intVal"));
		System.out.println("pro : parent = " + propertiesSetting.get("parent"));
		System.out.println("pro : parent.chiled = " + propertiesSetting.get("parent.chiled"));
		
		System.out.println("json : boolVal = " + jsonSetting.get("boolVal"));
		System.out.println("json : parent = " + jsonSetting.get("parent"));
		System.out.println("json : parent.chiled = " + jsonSetting.get("parent.chiled"));
		
		System.out.println("xml : boolVal = " + xmlSetting.get("boolVal"));
		System.out.println("xml : parent = " + xmlSetting.get("parent"));
		System.out.println("xml : parent.chiled = " + xmlSetting.get("parent.chiled"));
		
		propertiesSetting.put("newKey", "newVal");
		propertiesSetting.put("boolVal", false);
		propertiesSetting.put("parent.chiled.grandchild1", "grandchild1");
		propertiesSetting.put("parent.chiled.grandchild2", "grandchild2");
		
		System.out.println("pro : newKey = " + propertiesSetting.get("newKey"));
		System.out.println("pro : boolVal = " + propertiesSetting.get("boolVal"));
		
		jsonSetting.put("newKey", "newVal");
		jsonSetting.put("parent.chiled.grandchild1", "grandchild1");
		jsonSetting.put("parent.chiled.grandchild2", "grandchild2");
		
		System.out.println("json : newKey = " + jsonSetting.get("newKey"));
		System.out.println("json : parent.chiled = " + jsonSetting.get("parent.chiled"));
		System.out.println("json : parent.chiled.grandchild1 = " + jsonSetting.get("parent.chiled.grandchild1"));
		
		xmlSetting.put("newKey", "newVal");
		xmlSetting.put("parent.chiled.grandchild1", "grandchild1");
		xmlSetting.put("parent.chiled.grandchild2", "grandchild2");
		
		System.out.println("xml : newKey = " + xmlSetting.get("newKey"));
		System.out.println("xml : parent.chiled = " + xmlSetting.get("parent.chiled"));
		System.out.println("xml : parent.chiled.grandchild1 = " + xmlSetting.get("parent.chiled.grandchild1"));
		
		propertiesSetting.remove("strVal");
		jsonSetting.remove("strVal");
		xmlSetting.remove("strVal");
		
		System.out.println("json : arr = " + jsonSetting.get("arr"));
		System.out.println("xml : arr = " + xmlSetting.get("arr"));
		
		System.out.println("properties key : " + propertiesSetting.keys());
		System.out.println("json key : " + jsonSetting.keys());
		System.out.println("xml key : " + xmlSetting.keys());
//		
		System.out.println("properties to json key : " + jsonSetting.cast(FileType.TYPE_JSON).keys());
		System.out.println("json to xml : " + propertiesSetting.cast(FileType.TYPE_XML).keys());
		System.out.println("xml to properties key : " + xmlSetting.cast(FileType.TYPE_PROPERTIES).keys());
	}
	
	private static final String fakeProperties = 
			"""
			intVal=123
			boolVal=true
			strVal=abcdefg
			parent.chiled=pc
			""";
	
	private static final String fakeJson = 
			"""
			{
				"intVal":123,
				"boolVal":true,
				"strVal":"abcdefg",
				"parent":{
					"chiled":"pc"
				},
				"arr":["arr1", "arr2"]
			}
			""";
	
	private static final String fakeXml = 
			"""
			<root>
				<intVal>123</intVal>
				<boolVal>true</boolVal>
				<strVal>abcdefg</strVal>
				<parent>
					<chiled>pc</chiled>
				</parent>
				<arr>arr1</arr>
				<arr>arr2</arr>
			</root>
			""";
	
}
