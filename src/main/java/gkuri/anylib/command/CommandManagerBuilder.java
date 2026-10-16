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

    /**
     * helpコマンドを追加するか否かを設定します。
     * デフォルトはfalse(追加されない)です。
     * @param addHelp
     * @return
     */
    public CommandManagerBuilder addHelpCommand(boolean addHelp){
    	this.addHelp = addHelp;
        return this;
    }

    /**
     * echoコマンドを追加するか否かを設定します。
     * デフォルトはfalse(追加されない)です。
     * @param addEcho
     * @return
     */
    public CommandManagerBuilder addEchoCommand(boolean addEcho){
    	this.addEcho = addEcho;
        return this;
    }

    /**
     * historyコマンドを追加するか否かを設定します。
     * デフォルトはfalse(追加されない)です。
     * @param addHistory
     * @return
     */
    public CommandManagerBuilder addHistoryCommand(boolean addHistory){
    	this.addHistory = addHistory;
        return this;
    }

    /**
     * sudoコマンドを追加するか否かを設定します。
     * デフォルトはfalse(追加されない)です。
     * また、addSudoがtrueでも、passwordがNULL又はBlankの場合はsudoコマンドが追加されません。
     * @param addSudo
     * @param password
     * @return
     */
    public CommandManagerBuilder addSudoCommand(boolean addSudo, String password){
    	if(addSudo && password != null && !password.isBlank()){
            this.sudoCommandPass = password;
        } else {
            password = null;
        }
        return this;
    }
    
    /**
     * 試験的コマンドとして定義したコマンドを追加するか否かを設定します。
     * @param addExperimentalCommand
     * @return
     */
    public CommandManagerBuilder addExperimentalCommand(boolean addExperimentalCommand) {
    	this.addExperimentalCommand = addExperimentalCommand;
    	return this;
    }

    /**
     * コマンドのスペルが誤っている際に候補をエラー文に含めるか否かを設定します。
     * デフォルトはfalse(含めない)です。
     * @param showTypoSuggest
     * @return
     */
    public CommandManagerBuilder showTypoSuggest(boolean showTypoSuggest){
        this.showTypoSuggest = showTypoSuggest;
        return this;
    }

    /**
     * CommandManagerに登録したいコマンドのインスタンスを設定します。
     * @param com
     * @return
     */
    public CommandManagerBuilder addCommand(Command com){
        comsList.add(com);
        return this;
    }

    /**
     * CommandManagerに登録したいイベントリスナを設定します。
     * @param listener
     * @return
     */
    public CommandManagerBuilder addCommandEventListener(CommandEventListener listener){
        comEventListeners.add(listener);
        return this;
    }

    /**
     * CommandManagerに登録したいコマンド別のイベントリスナを設定します。
     * @param command
     * @param listener
     * @return
     */
    public CommandManagerBuilder addCommandEventListener(Class<? extends Command> command, CommandEventListener listener){
        comEventListenerReg.add(new Duo<>(command, listener));
        return this;
    }

    /**
     * エラー文やhelp等の言語を設定します。
     * @param language
     * @return
     */
    public CommandManagerBuilder setLanguage(String language){
        this.language = language;
        return this;
    }

    /**
     * 非同期実行コマンドを実際に走らせるExecuterを設定します。
     * デフォルトはJava標準のExecuterが使用されます。
     * @param executor
     * @return
     */
    public CommandManagerBuilder setExecutor(Executor executor){
        this.executor = executor;
        return this;
    }

    /**
     * ビルド後のCommandManagerインスタンスは最大何スレッドまで
	 * 非同期実行コマンドを同時に走らせることができるのかを設定します。
     * @param maxRunningAsyncCommandsCount
     * @return
     */
    public CommandManagerBuilder setMaxRunningAsyncCommandsCount(int maxRunningAsyncCommandsCount){
        this.maxRunningAsyncCommandsCount = 0 <= maxRunningAsyncCommandsCount ? maxRunningAsyncCommandsCount : 0;
        return this;
    }

    /**
     * コマンドの履歴を最大何件まで保持しておくかを設定します。
     * @param holdHistoriesCount
     * @return
     */
    public CommandManagerBuilder setHoldHistoriesCount(int holdHistoriesCount){
        this.holdHistoriesCount = 0 <= holdHistoriesCount ? holdHistoriesCount : 0;
        return this;
    }
    
    /**
     * 現在このビルダに登録されているCommandクラスのインスタンスをListで返します。
     * @return
     */
    public List<Command> getCommandsList(){
    	return List.copyOf(this.comsList);
    }
    
    /**
     * 現在このビルダに登録されているイベントリスナのインスタンスをListで返します。
     * @return
     */
    public List<CommandEventListener> getCommandEventListeners(){
    	return List.copyOf(this.comEventListeners);
    }
    
    /**
     * 現在このビルダに登録されているコマンド別イベントリスナのインスタンスをListで返します。
     * @param command
     * @return
     */
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