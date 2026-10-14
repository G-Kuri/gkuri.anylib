package gkuri.anylib;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 言語プロファイル用クラス。
 * このライブラリ内の言語設定にしか使えないようにするんで
 * 実装が若干雑。
 * 
 * 今はもう過去の遺物なのでどこからも呼ばれない。
 * 誰もお前を愛さない。
 */
public class TextContents {
	
	public static final String EN_US = "en-us";
	public static final String JA_JP = "ja-jp";
	
	private String langType;
	private Properties setting;
	private boolean fileFind = false;
	
	@SuppressWarnings("unused")
	private TextContents() {
		this(JA_JP);
	}
	
	public TextContents(String langType) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String coller = stackTrace[2].getClassName();
		
		if(coller.indexOf("gkuri.anylib.") == -1) throw new IllegalCallerException();
		
		this.langType = langType;

		coller = coller.substring(coller.indexOf("gkuri.anylib.") + "gkuri.anylib.".length());
//		System.out.println(coller);

		setting = new Properties();

		String stream = "lang/" + this.langType.replace("-", "_") + "/" + coller.replace(".", "/") + ".properties";
//		System.out.println(stream);
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(stream)) {
			if (is != null) {
			    this.setting.load(new InputStreamReader(is, StandardCharsets.UTF_8));
			    this.fileFind = true;
			} else {
				throw new RuntimeException("resource file is null(" + stream + ")");
			}
		} catch (Exception e) {
			if(e instanceof RuntimeException || e instanceof IOException) {
				System.err.println(e.getMessage());
				try (InputStream is = getClass().getClassLoader().getResourceAsStream("lang/ja_jp/" + coller.replace(".", "/") + ".properties")) {
					if (is != null) {
						this.setting.load(new InputStreamReader(is, StandardCharsets.UTF_8));
					    this.fileFind = true;
					}
					throw new RuntimeException("resource file is null");
				} catch (Exception e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			}
		}
	}
	
	public String get(String key) {
		if(!this.fileFind || setting == null) return key;
		String value = setting.getProperty(key);
		if(value == null) return key;
		return value;
	}
	
	public String getLangType() {
		return this.langType;
	}
	
	public boolean fileFind() {
		return this.fileFind;
	}
}
