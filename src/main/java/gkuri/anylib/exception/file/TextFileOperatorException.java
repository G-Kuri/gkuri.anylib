package gkuri.anylib.exception.file;

public class TextFileOperatorException extends RuntimeException{
	
	private int code;
	
	public TextFileOperatorException(int code){
		this.code = code;
	}
	
	public int getErrorCode() {
		return this.code;
	}
	
	/**
	 * このスロー可能オブジェクトの詳細メッセージ文字列を返します。
	 * @return このThrowableインスタンスの詳細メッセージ文字列(nullも可)。
	 */
	@Override
	public String getMessage() {
		String message = null;
		
		switch(this.code) {
			case 0x02 : 
				message = "No rows have been loaded";
				break;
			case 0xff :
				message = "Too many rows(limit is 2.1 billion rows)";
				break;
		}
		
		return message;
	}
}