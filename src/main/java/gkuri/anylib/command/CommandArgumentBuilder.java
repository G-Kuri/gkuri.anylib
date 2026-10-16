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

    /**
     * 現在の設定でCommandArgumentのインスタンスを生成します。
     * @return
     */
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

    /**
     * 実行するコマンドを設定します。
     * ここではルートコマンド・親コマンドのみを設定してください。
     * @param command
     * @return
     */
    public CommandArgumentBuilder setCommand(String command){
        if(command == null || command.isBlank()) throw new CommandException(2, command);
        this.command = command;
        return this;
    }
    
    /**
     * コマンドの引数を設定します。
     * サブコマンドを実行する場合はここからStringでコマンド名を追加してください。
     * @param arg
     * @return
     */
    public CommandArgumentBuilder addArg(Object arg) {
    	if(arg == null) return this;
    	this.args.add(arg);
    	return this;
    }

    /**
     * addArgで設定する引数をまとめて設定できます。
     * @param args
     * @return
     */
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

    /**
     * setCommandとsetArgsをまとめて設定できます。
     * @param command
     * @param args
     * @return
     */
    public CommandArgumentBuilder setCommandAndArgs(String command, Object... args){
        return setCommand(command).setArgs(args);
    }

    /**
     * 実行するユーザを設定できます。
     * ここで設定した値はCommandインターフェイスのcanExecuteCommandへ
     * そのまま渡されます。
     * 設定する値に制限はありません。
     * @param executeUser
     * @return
     */
    public CommandArgumentBuilder setExecuteUser(Object executeUser){
        this.executeUser = executeUser;
        return this;
    }

    /**
     * 対話IDを設定できます。
     * 対話コマンドの実行中はこのIDを基に対話中のコマンドを決定するため
     * 対話中は必ず設定してください。
     * @param interactiveThreadId
     * @return
     */
    public CommandArgumentBuilder setInteractiveThreadId(Long interactiveThreadId){
        this.interactiveThreadId = interactiveThreadId;
        return this;
    }

    /**
     * 引数オプションをgetArgsの値から削除するか否かを設定できます。
     * デフォルトはtrueとなっています。
     * @param removeOptionsByArgs
     * @return
     */
    public CommandArgumentBuilder removeOptionsByArgs(boolean removeOptionsByArgs) {
		this.removeOptionsByArgs = removeOptionsByArgs;
        return this;
	}
    
    /**
     * ここからCommandArgumentを追加すると、パイプ"|"で繋いだコマンドと
     * 同じ扱いでコマンドチェーンを作成できます。
     * @param commandArgument
     * @return
     */
    public CommandArgumentBuilder addDownStreamCommand(CommandArgument commandArgument) {
    	if(commandArgument == null) return this;
    	CommandArgumentBuilder cab = new CommandArgumentBuilder();
    	cab.setCommandAndArgs(commandArgument.getCommand(), commandArgument.getArgs());
    	cab.setExecuteUser(commandArgument.getExecuteUser());
    	cab.setInteractiveThreadId(commandArgument.getInteractiveThreadId());
    	this.downStreams.add(cab);
    	return this;
    }
    
    /**
     * ここからCommandArgumentを追加すると、パイプ"|"で繋いだコマンドと
     * 同じ扱いでコマンドチェーンを作成できます。
     * @param commandArgument
     * @return
     */
    public CommandArgumentBuilder addDownStreamCommand(CommandArgumentBuilder commandArgumentBuilder) {
    	if(commandArgumentBuilder == null) return this;
    	this.downStreams.add(commandArgumentBuilder);
    	return this;
    }
}
