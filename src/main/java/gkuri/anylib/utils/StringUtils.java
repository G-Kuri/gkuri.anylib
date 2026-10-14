package gkuri.anylib.utils;

public class StringUtils{
	
	private String text;
	
	public StringUtils() {
		
	}
	
	public StringUtils(String text) {
		this.text = text;
	}
	
	public String get() {
		return this.text;
	}
	
	public void set(String text) {
		this.text = text;
	}

//-----------------------------------------------------------------------------------------
//un-static methods
	public void trimComments() {
        this.text = this.text.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "");
	}
	
//-----------------------------------------------------------------------------------------
//static methods
	public static String trimComments(String text) {
        return text.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "");
	}
	
}