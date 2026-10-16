package gkuri.anylib.command;

public class CommandResultBuilder {
    
    private String prompt = CommandResult.DEFAULT_PROMPT;
	private Object result = "<undefined>";
	private boolean interactiveMode = false;
	private Long interactiveThreadId = null;
	private CommandResultStatus status = CommandResultStatus.SUCCESS;
    private Object interactiveData = null;

    public CommandResult build(){
        return new CommandResult(this.interactiveMode, this.interactiveThreadId, this.prompt, this.result, this.status, this.interactiveData);
    }

    public CommandResultBuilder(){

    }

    /**
     * UIに表示する想定のプロンプトを設定します。
     * デフォルトは空文字です。
     * @param prompt
     * @return
     */
    public CommandResultBuilder setPrompt(String prompt){
        if(prompt != null) this.prompt = prompt;
        return this;
    }

    /**
     * コマンドの実行結果を設定します。
     * @param result
     * @return
     */
    public CommandResultBuilder setResult(Object result){
        if(result != null) this.result = result;
        return this;
    }

    /**
     * 対話モードを設定します。
     * デフォルトはfalse(非対話モード)です。
     * @param interactiveMode
     * @return
     */
    public CommandResultBuilder setInteractiveMode(boolean interactiveMode){
        this.interactiveMode = interactiveMode;
        return this;
    }

    /**
     * 対話IDを設定します。
     * 対話中は必ず同じIDを引き継いで設定してください。
     * @param interactiveThreadId
     * @return
     */
    public CommandResultBuilder setInteractiveThreadId(Long interactiveThreadId){
        if(interactiveThreadId != null) this.interactiveThreadId = interactiveThreadId;
        return this;
    }

    /**
     * 対話モード移行時にgetNewInteractiveInstanceへ渡される
     * 対話用インスタンスへ渡すデータを設定します。
     * @param interactiveData
     * @return
     */
    public CommandResultBuilder setInteractiveData(Object interactiveData){
        this.interactiveData = interactiveData;
        return this;
    }

    /**
     * 実行ステータスを設定します。
     * デフォルトは通常時SUCCESS、対話モード時RUNNINGです。
     * @param status
     * @return
     */
    public CommandResultBuilder setStatus(CommandResultStatus status){
        if(status != null) this.status = status;
        return this;
    }
}
