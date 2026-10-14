package setting;

import gkuri.anylib.setting.SettingFile;
import gkuri.anylib.setting.fileTypes.FileType;

public class SettingFileTest {

    public static void main(String[] args) throws Exception {

        test("src/test/resources/setting/test.properties");
        test("src/test/resources/setting/test.json");
        test("src/test/resources/setting/test.xml");
    }

    private static void test(String file) throws Exception {

        System.out.println("==================================================");
        System.out.println(file);
        System.out.println("==================================================");

        SettingFile sf = SettingFile.loadSetting(file);

        //-------------------------
        // 読み込み
        //-------------------------

        System.out.println("keys = " + sf.keys());
        // 期待値：root, array, number, bool, text

        System.out.println("root.child = " + sf.get("root.child"));
        // 期待値：hello

        System.out.println("number = " + sf.getInt("number"));
        // 期待値：123

        System.out.println("number(Long) = " + sf.getLong("number"));
        // 期待値：123

        System.out.println("number(Float) = " + sf.getFloat("number"));
        // 期待値：123.0

        System.out.println("number(Double) = " + sf.getDouble("number"));
        // 期待値：123.0

        System.out.println("bool = " + sf.getBoolean("bool"));
        // 期待値：true

        System.out.println("text = " + sf.getString("text"));
        // 期待値：sample

        System.out.println("array[0] = " + sf.get("array[0]"));
        // 期待値：one

        System.out.println("array[1] = " + sf.get("array[1]"));
        // 期待値：two

        System.out.println("array[2] = " + sf.get("array[2]"));
        // 期待値：three

        System.out.println("notfound = " + sf.get("notfound"));
        // 期待値：null

        //-------------------------
        // put
        //-------------------------

        sf.put("root.child", "changed");
        System.out.println("put root.child = " + sf.get("root.child"));
        // 期待値：changed

        sf.put("newKey", "newValue");
        System.out.println("newKey = " + sf.get("newKey"));
        // 期待値：newValue

        sf.put("newParent.child", "abc");
        System.out.println("newParent.child = " + sf.get("newParent.child"));
        // 期待値：abc

        //-------------------------
        // remove
        //-------------------------

        sf.remove("newKey");

        System.out.println("remove newKey = " + sf.get("newKey"));
        // 期待値：null

        //-------------------------
        // cast
        //-------------------------

        SettingFile prop = sf.cast(FileType.TYPE_PROPERTIES);
        System.out.println("cast(Properties) = " + prop.getFileType());
        // 期待値：TYPE_PROPERTIES

        SettingFile json = sf.cast(FileType.TYPE_JSON);
        System.out.println("cast(JSON) = " + json.getFileType());
        // 期待値：TYPE_JSON

        SettingFile xml = sf.cast(FileType.TYPE_XML);
        System.out.println("cast(XML) = " + xml.getFileType());
        // 期待値：TYPE_XML

        //-------------------------
        // save
        //-------------------------

//        sf.save();
//
//        System.out.println("save() completed.");
//        // 期待値：例外が発生しない
    }

}