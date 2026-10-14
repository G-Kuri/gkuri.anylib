package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.exception.command.CommandException;
import gkuri.anylib.user.User;

public class CommandArgumentBuilder {
    
    private String command = null;
	private List<Object> args = new ArrayList<>();
	private Object executeUser = null;
	private Long interactiveThreadId = null;
    private boolean removeOptionsByArgs = true;
    private List<CommandArgumentBuilder> downStreams = new ArrayList<>();
    private CommandResult beforeCommandResult;

    public CommandArgument build(){
        if(this.executeUser == null) this.executeUser = User.DEFAULT_USER;
        CommandArgument ca = new CommandArgument(this.executeUser, this.command, this.interactiveThreadId, this.args, this.removeOptionsByArgs, this.downStreams, this.beforeCommandResult);
        if(this.args.isEmpty()){
            if(this.interactiveThreadId != null){
                ca.splitAndParseStringArgs();
            } else {
                ca.splitCommandAndArgs();
            }
        }
        return ca;
    }

    public CommandArgumentBuilder(){

    }

    public CommandArgumentBuilder setCommand(String command){
        if(command == null || command.isBlank()) throw new CommandException(2, command);
        this.command = command;
        return this;
    }
    
    public CommandArgumentBuilder addArg(Object arg) {
    	if(arg == null) return this;
    	this.args.add(arg);
    	return this;
    }

    public CommandArgumentBuilder setArgs(Object... args){
    	for(Object a : args){
            if(a instanceof List<?> list){
                this.args.addAll(list);
            } else {
                this.args.add(a);
            }
        }
        return this;
    }
    
    protected void setBeforeCommandResult(CommandResult commandResult) {
		this.beforeCommandResult = commandResult;
	}

    public CommandArgumentBuilder setCommandAndArgs(String command, Object... args){
        return setCommand(command).setArgs(args);
    }

    public CommandArgumentBuilder setExecuteUser(Object executeUser){
        this.executeUser = executeUser;
        return this;
    }

    public CommandArgumentBuilder setInteractiveThreadId(Long interactiveThreadId){
        this.interactiveThreadId = interactiveThreadId;
        return this;
    }

    public CommandArgumentBuilder removeOptionsByArgs(boolean removeOptionsByArgs) {
		this.removeOptionsByArgs = removeOptionsByArgs;
        return this;
	}
    
    public CommandArgumentBuilder addDownStreamCommand(CommandArgument commandArgument) {
    	if(commandArgument == null) return this;
    	CommandArgumentBuilder cab = new CommandArgumentBuilder();
    	cab.setCommandAndArgs(commandArgument.getCommand(), commandArgument.getArgs());
    	cab.setExecuteUser(commandArgument.getExecuteUser());
    	cab.setInteractiveThreadId(commandArgument.getInteractiveThreadId());
    	this.downStreams.add(cab);
    	return this;
    }
    
    public CommandArgumentBuilder addDownStreamCommand(CommandArgumentBuilder commandArgumentBuilder) {
    	if(commandArgumentBuilder == null) return this;
    	this.downStreams.add(commandArgumentBuilder);
    	return this;
    }
}
