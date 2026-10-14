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

    public CommandResultBuilder setPrompt(String prompt){
        if(prompt != null) this.prompt = prompt;
        return this;
    }

    public CommandResultBuilder setResult(Object result){
        if(result != null) this.result = result;
        return this;
    }

    public CommandResultBuilder setInteractiveMode(boolean interactiveMode){
        this.interactiveMode = interactiveMode;
        return this;
    }

    public CommandResultBuilder setInteractiveThreadId(Long interactiveThreadId){
        if(interactiveThreadId != null) this.interactiveThreadId = interactiveThreadId;
        return this;
    }

    public CommandResultBuilder setInteractiveData(Object interactiveData){
        this.interactiveData = interactiveData;
        return this;
    }

    public CommandResultBuilder setStatus(CommandResultStatus status){
        if(status != null) this.status = status;
        return this;
    }
}
