package gkuri.anylib.command;

public class CommandSuggestion {

	private Object suggest;
	private String description;
	
	@SuppressWarnings("unused")
	private CommandSuggestion() {
		
	}
	
	/**
	 * 候補のObjectを格納してインスタンスを生成します。
	 * 説明は空文字です。
	 * @param suggest
	 */
	public CommandSuggestion(Object suggest) {
		this(suggest, "");
	}

	/**
	 * 候補のObjectとその説明を格納してインスタンスを生成します。
	 * @param suggest
	 * @param description
	 */
	public CommandSuggestion(Object suggest, String description) {
		this.suggest = suggest;
		this.description = description;
	}
	
	/**
	 * 設定された候補を返します。
	 * @return
	 */
	public Object getSuggest() {
		return this.suggest;
	}
	
	/**
	 * 設定された候補の説明を返します。
	 * デフォルトは空文字です。
	 * @return
	 */
	public String getDescription() {
		return this.description;
	}
}
