package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import gkuri.anylib.TextContents;
import gkuri.anylib.command.events.CommandEventListener;
import gkuri.anylib.exception.command.CommandException;
import gkuri.anylib.utils.multiValue.Duo;

public class CommandManagerBuilder{

    private boolean addHelp = false;
    private boolean addEcho = false;
    private boolean addHistory = false;
    private String sudoCommandPass = null;
    private boolean showTypoSuggest = false;
    private final List<Command> comsList = new LinkedList<>();
    private Executor executor = null;
    private String language = TextContents.JA_JP;
    private int maxRunningAsyncCommandsCount = 10;
    private int holdHistoriesCount = 10_000;
    private final List<CommandEventListener> comEventListeners = new LinkedList<>();
    private final List<Duo<Class<? extends Command>, CommandEventListener>> comEventListenerReg = new LinkedList<>();
    private boolean addExperimentalCommand = false;
    
    private boolean isUsed = false;

    public CommandManager build(){
    	if(isUsed) throw new CommandException(12, null);
    	isUsed = true;
        return new CommandManager(executor, 
                                    addHelp, 
                                    addEcho, 
                                    addHistory, 
                                    sudoCommandPass,
                                    showTypoSuggest,
                                    language, 
                                    comsList, 
                                    maxRunningAsyncCommandsCount, 
                                    holdHistoriesCount, 
                                    comEventListeners, 
                                    comEventListenerReg,
                                    addExperimentalCommand);
    }

    public CommandManagerBuilder addHelpCommand(boolean addHelp){
    	this.addHelp = addHelp;
        return this;
    }

    public CommandManagerBuilder addEchoCommand(boolean addEcho){
    	this.addEcho = addEcho;
        return this;
    }

    public CommandManagerBuilder addHistoryCommand(boolean addHistory){
    	this.addHistory = addHistory;
        return this;
    }

    public CommandManagerBuilder addSudoCommand(boolean addSudo, String password){
    	if(addSudo && password != null && !password.isBlank()){
            this.sudoCommandPass = password;
        } else {
            password = null;
        }
        return this;
    }
    
    public CommandManagerBuilder addExperimentalCommand(boolean addExperimentalCommand) {
    	this.addExperimentalCommand = addExperimentalCommand;
    	return this;
    }

    public CommandManagerBuilder showTypoSuggest(boolean showTypoSuggest){
        this.showTypoSuggest = showTypoSuggest;
        return this;
    }

    public CommandManagerBuilder addCommand(Command com){
        comsList.add(com);
        return this;
    }

    public CommandManagerBuilder addCommandEventListener(CommandEventListener listener){
        comEventListeners.add(listener);
        return this;
    }

    public CommandManagerBuilder addCommandEventListener(Class<? extends Command> command, CommandEventListener listener){
        comEventListenerReg.add(new Duo<>(command, listener));
        return this;
    }

    public CommandManagerBuilder setLanguage(String language){
        this.language = language;
        return this;
    }

    public CommandManagerBuilder setExecutor(Executor executor){
        this.executor = executor;
        return this;
    }

    public CommandManagerBuilder setMaxRunningAsyncCommandsCount(int maxRunningAsyncCommandsCount){
        this.maxRunningAsyncCommandsCount = 0 <= maxRunningAsyncCommandsCount ? maxRunningAsyncCommandsCount : 0;
        return this;
    }

    public CommandManagerBuilder setHoldHistoriesCount(int holdHistoriesCount){
        this.holdHistoriesCount = 0 <= holdHistoriesCount ? holdHistoriesCount : 0;
        return this;
    }
    
    public List<Command> getCommandsList(){
    	return List.copyOf(this.comsList);
    }
    
    public List<CommandEventListener> getCommandEventListeners(){
    	return List.copyOf(this.comEventListeners);
    }
    
    public List<CommandEventListener> getCommandEventListeners(Command command){
    	List<CommandEventListener> result = new ArrayList<>();
    	for(Duo<Class<? extends Command>, CommandEventListener> listener : this.comEventListenerReg) {
    		if(listener.getFirst() == command.getClass()) {
    			result.add(listener.getSecond());
    		}
    	}
    	return result;
    }
    
}