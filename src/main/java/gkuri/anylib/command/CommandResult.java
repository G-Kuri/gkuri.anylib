package gkuri.anylib.command;

public class CommandResult {
	
	static final String DEFAULT_PROMPT = "";
	
	private String prompt;
	private Object result;
	private boolean interactiveMode;
	private Long interactiveThreadId;
	private CommandResultStatus status;
	private Object interactiveData;
	
	@SuppressWarnings("unused")
	private CommandResult() {};
	
	public CommandResult(Object result) {
		this(DEFAULT_PROMPT, result);
	}
	
	public CommandResult(Object result, CommandResultStatus status) {
		this(DEFAULT_PROMPT, result, status);
	}
	
	private CommandResult(String prompt, Object result) {
		this(false, prompt, result);
	}
	
	private CommandResult(String prompt, Object result, CommandResultStatus status) {
		this(false, null, prompt, result, status, null);
	}

	private CommandResult(boolean interactiveMode, String prompt, Object result) {
		this(interactiveMode, null, prompt, result);
	}
	
	@SuppressWarnings("unused")
	private CommandResult(boolean interactiveMode, String prompt, Object result, CommandResultStatus status) {
		this(interactiveMode, null, prompt, result, status, null);
	}
	
	private CommandResult(boolean interactiveMode, Long interactiveThreadId, String prompt, Object result) {
		this(interactiveMode, interactiveThreadId, prompt, result, interactiveMode ? CommandResultStatus.RUNNING : CommandResultStatus.SUCCESS, null);
	} 

	protected CommandResult(boolean interactiveMode, Long interactiveThreadId, String prompt, Object result, CommandResultStatus status, Object interactiveData) {
		this.interactiveMode = interactiveMode;
		this.prompt = prompt;
		this.result = result;
		this.interactiveThreadId = interactiveThreadId;
		this.status = status;
		this.interactiveData = interactiveData;
	}
	
	/**
	 * インタラクティブモードか否かを返します。
	 * @return
	 */
	public boolean isInteractiveMode() {
		return this.interactiveMode;
	}
	
	/**
	 * インタラクティブIDを返します。
	 * インタラクティブモードではない場合、nullが返ります。
	 * @return
	 */
	public Long getInteractiveThreadId() {
		return this.interactiveThreadId;
	}
	
	/**
	 * インタラクティブIDを手動で設定します。
	 * 通常はコマンド実行後にCommandManagerが
	 * 必要に応じて自動的に設定するため実行する必要はありません。
	 */
	public void setInteractiveThreadId() {
		this.setInteractiveThreadId(CommandManager.createCommandThread());
	}

	void setInteractiveThreadId(long interactiveThreadId) {
		if(this.interactiveThreadId != null || !interactiveMode) return;
		this.interactiveThreadId = interactiveThreadId;
	}
	
	/**
	 * コマンドの入力画面に表示するプロンプトを返します。
	 * @return
	 */
	public String getPrompt() {
		return this.prompt;
	}
	
	/**
	 * コマンドの実行結果を返します。
	 * @return
	 */
	public Object getResult() {
		return this.result;
	}
	
	/**
	 * コマンド実行結果のステータスを返します。
	 * @return
	 */
	public CommandResultStatus getStatus() {
		return this.status;	
	}

	/**
	 * 対話モードへ移行する際にgetNewInteractiveInstance経由でCommandクラスへ渡されるデータを返します。
	 * @return
	 */
	public Object getInteractiveData(){
		return this.interactiveData;
	}
	
	@Override
	public String toString() {
		return String.format("interactive : %5b [%d] || %s > %s", this.interactiveMode, this.interactiveThreadId, this.prompt, this.result);
	}

	public static CommandResultBuilder getBuilder() {
		return new CommandResultBuilder();
	}
}
