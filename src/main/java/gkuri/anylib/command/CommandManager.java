package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import gkuri.anylib.command.annotation.ExperimentalCommand;
import gkuri.anylib.command.events.CommandEventListener;
import gkuri.anylib.exception.command.CommandException;
import gkuri.anylib.setting.SettingFile;
import gkuri.anylib.utils.CorrectKeyword;
import gkuri.anylib.utils.multiValue.Duo;

/**
 * コマンドを管理・実行できるクラスです。
 * 初期化(コマンドの登録やイベントリスナの登録)は
 * CommandManagerBuilderから実施してください。
 * 
 * コマンドを実行する際はCommandArgumentクラスの引数を
 * executeCommandメソッドに流すことで実行されます。
 * 非同期処理を行いたい場合はexecuteCommandAsyncメソッドに流してください。
 * 
 * コマンド実行の前後及びリスナ内で例外発生時に発火するイベントがあります。
 * CommandEventListenerクラスを継承したクラスを用意し
 * CommandManagerBuilerから登録することでリスンできます。
 * リスナ登録は
 * 全体/コマンド別
 * を選択して登録できますが、実行順序は
 * 全体リスナ -> コマンド別リスナ -> コマンド実行 -> コマンド別リスナ -> 全体リスナ
 * の順に実行されます。
 * 
 * インスタンス毎の設定可能項目は
 * 保持するコマンド履歴の件数(デフォルト10,000件)
 * 同時実行可能な非同期処理の最大数(デフォルト10スレッド)
 * 
 * 全体の設定可能項目は
 * 全体で同時実行可能な非同期処理の最大数(デフォルト100スレッド)
 */
public class CommandManager {

	private static final AtomicLong SESSION_ID_GENERATOR = new AtomicLong(1);
	private static final AtomicInteger ALL_RUNNING_ASYNC_COMMANDS = new AtomicInteger(0);
	private static final AtomicInteger ALL_MAX_RUNNING_ASYNC_COMMANDS_COUNT = new AtomicInteger(100);

	protected final List<CommandEventListener> comEventListeners = new CopyOnWriteArrayList<>();
	protected final Map<String, Command> comList = new HashMap<>();
	protected final Map<Class<? extends Command>, List<CommandEventListener>> comEventListenerReg = new HashMap<>();
	protected final Map<Long, Command> interactiveCommands = new ConcurrentHashMap<>();
	private final AtomicInteger runningAsyncCommandsCount = new AtomicInteger(0);
	private final AtomicInteger maxRunningAsyncCommandsCount = new AtomicInteger(10);
	protected final boolean showTypoSuggest;
	protected SettingFile langSettingFile;
	protected final Executor executor;

	private HistoryCommand historyCommand;

	/**
	 * builder use only
	 * 
	 * @param executor
	 * @param addHelpCommand
	 * @param addEchoCommand
	 * @param addHistoryCommand
	 * @param lang
	 * @param comsList
	 * @param maxRunningAsyncCommandsCount
	 * @param holedHistoriesCount
	 * @param comEventListeners
	 * @param comEventListenerReg
	 * @param throwException
	 */
	protected CommandManager(Executor executor,
			boolean addHelpCommand,
			boolean addEchoCommand,
			boolean addHistoryCommand,
			String sudoCommandPass,
			boolean showTypoSuggest,
			String lang,
			List<Command> comsList,
			int maxRunningAsyncCommandsCount,
			int holedHistoriesCount,
			List<CommandEventListener> comEventListeners,
			List<Duo<Class<? extends Command>, CommandEventListener>> comEventListenerReg,
			boolean addExperimentalCommand) {
		
		try {
			this.langSettingFile = SettingFile.loadResourcesSetting("lang/" + lang.replace("-", "_") + "/command/CommandManager.properties");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				this.langSettingFile = SettingFile.loadResourcesSetting("lang/ja_jp/command/CommandManager.properties");
			} catch (Exception e1) {
				throw new IllegalArgumentException(e1);
			}
		}
		this.executor = executor;
		this.showTypoSuggest = showTypoSuggest;
		this.maxRunningAsyncCommandsCount.set(maxRunningAsyncCommandsCount);
		
		if (addHelpCommand) this.addHelpCommand();
		if (addEchoCommand) this.addEchoCommand();
		if (addHistoryCommand) this.addHistoryCommand(holedHistoriesCount);
		if (sudoCommandPass != null && !sudoCommandPass.isBlank()) this.addSudoCommand(sudoCommandPass);
		
		for (Command com : comsList) {
			ExperimentalCommand ec;
			if(((ec = com.getClass().getAnnotation(ExperimentalCommand.class)) != null && ec.value()) && !addExperimentalCommand) continue;
			addCommand(com);
		}
		
		int maxLen = 0;
		List<Command> hasAliasCommands = new ArrayList<>();
		for (Command com : comsList) {
			String[] alias;
			if((alias = com.getAlias()) != null) {
				hasAliasCommands.add(com);
				if(maxLen < alias.length) maxLen = alias.length;
			}
		}
		hasAliasCommands.sort((a, b) -> {
			return Integer.compare(a.getAlias().length, b.getAlias().length);
		});
		for(int i = 0; i < maxLen; i++) {
			for(Command com : hasAliasCommands) {
				String[] alias = com.getAlias();
				if(alias.length <= i || alias[i] == null || alias[i].isBlank()) continue;
				String comAlias = alias[i];
				if(!this.comList.containsKey(comAlias)) {
					this.comList.put(comAlias, this.comList.get(com.getName()));
				}
			}
		}
		
		for (CommandEventListener cel : comEventListeners) addCommandEventListener(cel);
		for (Duo<Class<? extends Command>, CommandEventListener> cel : comEventListenerReg) addCommandEventListener(cel.getFirst(), cel.getSecond());
	}

	protected CommandManager(CommandManager commandManager) {
		this(commandManager, null);
	}

	protected CommandManager(CommandManager commandManager, Executor executor) {
		this.langSettingFile = commandManager.langSettingFile;
		this.executor = executor;
		this.showTypoSuggest = commandManager.showTypoSuggest;
		this.maxRunningAsyncCommandsCount.set(commandManager.maxRunningAsyncCommandsCount.get());
		commandManager.comList.forEach(this.comList::put);
		commandManager.comEventListeners.forEach(this.comEventListeners::add);
	}

	protected void addHelpCommand() {
		this.addCommand(new HelpCommand(this, langSettingFile));
	}

	protected void addEchoCommand() {
		this.addCommand(new EchoCommand(langSettingFile));
	}

	protected void addHistoryCommand(int holedHistoriesCount) {
		historyCommand = new HistoryCommand(this.langSettingFile, holedHistoriesCount);
		this.addCommand(historyCommand);
	}

	protected void addSudoCommand(String pass) {
		this.addCommand(new SudoCommand(this, langSettingFile, pass));
	}

	protected void addCommand(Command com) {
		if (com instanceof InteractiveCommand)
			throw new CommandException(14, com);
		if (com.getName() == null || com.getName().isBlank())
			throw new CommandException(2, com);
		if (comList.get(com.getName()) != null)
			throw new CommandException(1, com);
		if (com.getMaxArgsCount() < com.getMinArgsCount() || com.getMinArgsCount() < 0)
			throw new CommandException(13, com);
		comList.put(com.getName(), com);
	}

	protected void addCommandEventListener(CommandEventListener listener) {
		this.comEventListeners.add(listener);
	}

	protected void addCommandEventListener(Class<? extends Command> command, CommandEventListener listener) {
		if (this.comEventListenerReg.get(command) == null) {
			this.comEventListenerReg.put(command, new ArrayList<CommandEventListener>());
		}
		this.comEventListenerReg.get(command).add(listener);
	}

	protected static long createCommandThread() {
		long id = SESSION_ID_GENERATOR.getAndIncrement();
		return id;
	}

	protected void removeCommandThread(long interactiveThreadId) {
		this.interactiveCommands.remove(interactiveThreadId);
	}

	public String[] getAllCommands() {
		List<String> list = new ArrayList<>(comList.keySet());
		list.sort(String::compareTo);
		return list.toArray(new String[0]);
	}

	public Command getCommandClass(String command) {
		if (this.comList.get(command) == null)
			return null;
		return comList.get(command);
	}

	public static int getAllRunningAsyncCount() {
		return ALL_RUNNING_ASYNC_COMMANDS.get();
	}

	public int getRunningAsyncCommandsCount() {
		return runningAsyncCommandsCount.get();
	}

	synchronized public List<CommandArgument> getHistories() {
		if (historyCommand == null)
			return List.of();
		return historyCommand.getHistories();
	}

	public int getMaxRunningAsyncCommandsCount() {
		return this.maxRunningAsyncCommandsCount.get();
	}

	public static int getAllMaxRunningAsyncCommandsCount() {
		return ALL_MAX_RUNNING_ASYNC_COMMANDS_COUNT.get();
	}

	public void setMaxRunningAsyncCommandsCount(int maxRunningAsyncCommandsCount) {
		if (0 < maxRunningAsyncCommandsCount
				&& maxRunningAsyncCommandsCount <= ALL_MAX_RUNNING_ASYNC_COMMANDS_COUNT.intValue())
			this.maxRunningAsyncCommandsCount.set(maxRunningAsyncCommandsCount);
	}

	public static void setAllMaxRunningAsyncCommandsCount(int allMaxRunningAsyncCommandsCount) {
		if (0 < allMaxRunningAsyncCommandsCount)
			ALL_MAX_RUNNING_ASYNC_COMMANDS_COUNT.set(allMaxRunningAsyncCommandsCount);
	}

	public boolean isInstanceBusy() {
		return getMaxRunningAsyncCommandsCount() <= getRunningAsyncCommandsCount();
	}

	public static boolean isAllBusy() {
		return getAllMaxRunningAsyncCommandsCount() <= getAllRunningAsyncCount();
	}

	public CommandSuggestion[] getCommandSuggestions(String args) {
		return this.getCommandSuggestions(new CommandArgument(args), false);
	}

	public CommandSuggestion[] getNextCommandSuggestions(String args) {
		return this.getCommandSuggestions(new CommandArgument(args), true);
	}

	public CommandSuggestion[] getCommandSuggestions(CommandArgument args) {
		return this.getCommandSuggestions(args, false);
	}

	public CommandSuggestion[] getNextCommandSuggestions(CommandArgument args) {
		return this.getCommandSuggestions(args, true);
	}

	protected CommandSuggestion[] getCommandSuggestions(CommandArgument args, boolean hopeNextArg) {

		List<CommandSuggestion> suggest = new ArrayList<>();
		if (comList.get(args.getCommand()) != null) {
			Command c = comList.get(args.getCommand());

			if (!(c instanceof SudoCommand)) {
				while (args.getArgs().size() != 0) {
					Map<String, Command> subs = c.getSubCommands().getSubCommandsMap();
					if (subs == null)
						break;

					Command sub = subs.get(args.getArgs().get(0).toString());
					if (sub == null)
						break;

					c = sub;

					ArrayList<Object> newArgs = new ArrayList<>(args.getArgs());
					newArgs.remove(0);
					boolean isSudo = args.isSudo();
					args = new CommandArgumentBuilder()
							.setExecuteUser(args.getExecuteUser())
							.setCommand(args.getArgs().get(0).toString())
							.setInteractiveThreadId(args.getInteractiveThreadId())
							.setArgs(newArgs)
							.build();
					args.isUsed = true;
					args.setSudo(isSudo);
				}
			}

			if(hopeNextArg){
				for(Command subCom : c.getSubCommands().getSubCommandsList()){
					if(subCom.isHiddenCommand()) continue;
					suggest.add(new CommandSuggestion(subCom.getName(), subCom.getDescription()));
				}
			} else {
				if(args.getArg(args.getArgsCount() - 1) instanceof String a){
					boolean fined = false;
					for(Command subCom : c.getSubCommands().getSubCommandsList()){
						if(subCom.getName().startsWith(a) && !subCom.isHiddenCommand()){
							fined = true;
							suggest.add(new CommandSuggestion(subCom.getName(), subCom.getDescription()));
							continue;
						}

						if(fined) break;
					}
				}
			}

			int[] matchPatterns = args.tryClassCast(c, hopeNextArg);
			int argsPos = Math.max(args.getArgsCount() - (hopeNextArg ? 0 : 1), 0);
			for(CommandSuggestion s : c.getArgSuggestions(matchPatterns, argsPos, args)){
				suggest.add(s);
			}
		} else {
			boolean fined = false;
			List<String> sortedComList = new ArrayList<>(this.comList.keySet());
			sortedComList.sort(String::compareTo);
			for(String comName : sortedComList) {
				if(comName.startsWith(args.getCommand())) {
					Command com = this.comList.get(comName);
					if(com.isHiddenCommand()) continue;
					fined = true;
					suggest.add(new CommandSuggestion(com.getName(), com.getDescription()));
					continue;
				}
				
				if(fined) break;
			}
		}

		return suggest.toArray(new CommandSuggestion[0]);
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	public CommandResult executeCommand(CommandArgument args) {
		if (args == null)
			throw new NullPointerException();
		if (args.isUsed)
			throw new CommandException(11, args);

		args.isUsed = true;

		if (args.getInteractiveThreadId() != null) {
			CommandResult commandResult = null;
			commandResult = doInteractiveCommand(args);
			if (commandResult != null)
				return commandResult;

			args.splitCommandAndArgs();
		}

		if (args.getCommand() == null || args.getCommand().isEmpty())
			return new CommandResult(langSettingFile.getNonNull("commandIsNull").toString(), CommandResultStatus.UNEXECUTED);

		if (this.historyCommand != null && !args.isSudo()) this.historyCommand.addHistory(CommandArgument.copyOf(args));
		
		CommandResult commandResult = doCommandPipeLoop(args);

		if (commandResult != null) return commandResult;

		String maybe = correctKeyword(args.getCommand());
		String notFound = args.getCommand() + " : " + langSettingFile.getNonNull("commandNotFound").toString();
		if (!showTypoSuggest || maybe.equals(args.getCommand())) {
			return new CommandResult(notFound, CommandResultStatus.UNEXECUTED);
		}
		return new CommandResult(notFound + langSettingFile.getNonNull("commandMaybe").toString().formatted(maybe),
				CommandResultStatus.UNEXECUTED);
	}

	private CommandResult doInteractiveCommand(CommandArgument args) {
		Command interactiveCommand;
		if ((interactiveCommand = interactiveCommands.get(args.getInteractiveThreadId())) != null) {
			CommandResult result = null;
			try {
				result = interactiveCommand.execute(args);
			} catch (Exception e) {
				removeCommandThread(args.getInteractiveThreadId());
				try {
					result = interactiveCommand.catchException(e, args);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
				if (result == null)
					result = new CommandResult(langSettingFile.getNonNull("commandCatchException").toString(), CommandResultStatus.ERROR);
			}

			if (result == null)
				throw new CommandException(3, interactiveCommand);
			if (result.isInteractiveMode()) {
				result.setInteractiveThreadId(args.getInteractiveThreadId());
			} else {
				removeCommandThread(args.getInteractiveThreadId());
			}
			return result;
		}
		return null;
	}

	protected CommandResult doCommand(CommandArgument args) {

		if (comList.get(args.getCommand()) != null) {
			Command c = comList.get(args.getCommand());

			if (checkCanEecuteAuth(c, args)) return new CommandResult(langSettingFile.getNonNull("insufficiencyPermission").toString(), CommandResultStatus.UNEXECUTED);

			List<CommandEventListener> listener = addComEventListenerReg(new ArrayList<>(), c);

			if (!(c instanceof SudoCommand)) {
				return executeCommandFromSudo(c, args, listener);
			}

			return runCommandLifeTime(c, args, listener);
		}
		return null;
	}
	
	protected boolean checkCanEecuteAuth(Command command, CommandArgument args) {
		return !args.isSudo() && !command.canExecuteCommand(args.getExecuteUser());
	}
	
	protected List<CommandEventListener> addComEventListenerReg(List<CommandEventListener> baseList, Command c) {
		if(!c.getSubCommands().canListenSubCommandsEvent() || this.comEventListenerReg.get(c.getClass()) == null) {
			baseList.clear();
		}
		
		if(comEventListenerReg.get(c.getClass()) != null) baseList.addAll(comEventListenerReg.get(c.getClass()));
		return baseList;
	}
	
	protected CommandResult doCommandPipeLoop(CommandArgument args) {
		List<CommandArgumentBuilder> downStreams = args.getDownStreams();
		CommandResult commandResult = doCommand(args);

		for (CommandArgumentBuilder ds : downStreams) {
			ds.setBeforeCommandResult(commandResult);
			commandResult = doCommand(ds.build());
		}
		
		return commandResult;
	}
	
	protected String correctKeyword(String command) {
		return CorrectKeyword.correctKeyword(getAllCommands(), command);
	}
	
	protected CommandResult runCommandLifeTime(Command c, CommandArgument args, List<CommandEventListener> listener) {
		doListenersBeforeEvent(listener, args);

		if (args.getArgs().size() < c.getMinArgsCount())
			return new CommandResult(langSettingFile.getNonNull("tooFewArgs").toString(), CommandResultStatus.ERROR);
		if (c.getMaxArgsCount() < args.getArgs().size())
			return new CommandResult(langSettingFile.getNonNull("tooManyArgs").toString(), CommandResultStatus.ERROR);
		
		try {
			args.classCastArgs(c);
			CommandResult result = tryExecuteCommand(c, args);

			if (result == null) throw new CommandException(3, null);
			
			if (result.isInteractiveMode()) {
				if (result.getInteractiveThreadId() == null)
					result.setInteractiveThreadId();
				interactiveCommands.put(result.getInteractiveThreadId(), c.getNewInteractiveInstance(result.getInteractiveData()));
			}

			doListenersAfterEvent(listener, args, result);
			
			return result;
		} catch (CommandException e) {
			if (e.getCode() == 5)
				return new CommandResult(langSettingFile.getNonNull("argsTypeUnmatched").toString(), CommandResultStatus.ERROR);
			throw e;
		}
	}
	
	protected CommandResult tryExecuteCommand(Command com, CommandArgument args) {
		CommandResult result = null;
		try {
			result = com.execute(args);
		} catch (Exception e) {
			// removeCommandThread(args.getInteractiveThreadId());
			try {
				result = com.catchException(e, args);
			} catch (Exception ec) {
				ec.printStackTrace();
			}
			if (result == null) {
				result = new CommandResult(langSettingFile.getNonNull("commandCatchException").toString(),
						CommandResultStatus.ERROR);
			}
		}
		return result;
	}
	
	protected CommandResult executeCommandFromSudo(Command c, CommandArgument args, List<CommandEventListener> sudoEventListener) {
		while (args.getArgs().size() != 0) {
			Map<String, Command> subs = c.getSubCommands().getSubCommandsMap();
			if (subs == null)
				break;

			Command sub = subs.get(args.getArgs().get(0).toString());
			if (sub == null)
				break;

			addComEventListenerReg(sudoEventListener, sub);
			
			c = sub;

			if (checkCanEecuteAuth(c, args)) return new CommandResult(langSettingFile.getNonNull("insufficiencyPermission").toString(), CommandResultStatus.UNEXECUTED);

			ArrayList<Object> newArgs = new ArrayList<>(args.getArgs());
			newArgs.remove(0);
			boolean isSudo = args.isSudo();
			args = new CommandArgumentBuilder()
					.setExecuteUser(args.getExecuteUser())
					.setCommand(args.getArgs().get(0).toString())
					.setInteractiveThreadId(args.getInteractiveThreadId())
					.setArgs(newArgs)
					.build();
			args.isUsed = true;
			args.setSudo(isSudo);
		}
		
		return runCommandLifeTime(c, args, sudoEventListener);
	}
	
	/**
	 * executeCommandと同じ動作。
	 * ただしこちらは非同期。
	 * 
	 * @param args
	 * @return
	 */
	public CompletableFuture<CommandResult> executeCommandAsync(CommandArgument args) {
		if (isInstanceBusy())
			throw new CommandException(7, getRunningAsyncCommandsCount());
		if (isAllBusy())
			throw new CommandException(8, getAllRunningAsyncCount());

		ALL_RUNNING_ASYNC_COMMANDS.incrementAndGet();
		runningAsyncCommandsCount.incrementAndGet();

		CompletableFuture<CommandResult> task;
		try {
			if (executor != null) {
				task = CompletableFuture.supplyAsync(() -> executeCommand(args), executor);
			} else {
				task = CompletableFuture.supplyAsync(() -> executeCommand(args));
			}
		} catch (RuntimeException e) {
			ALL_RUNNING_ASYNC_COMMANDS.decrementAndGet();
			runningAsyncCommandsCount.decrementAndGet();
			throw e;
		}

		return task.whenComplete((r, t) -> {
			ALL_RUNNING_ASYNC_COMMANDS.decrementAndGet();
			runningAsyncCommandsCount.decrementAndGet();
		});
	}

	protected void doListenersBeforeEvent(List<CommandEventListener> comListeners, CommandArgument args) {
		for (CommandEventListener l : this.comEventListeners) {
			try {
				l.doBeforeExecution(args);
			} catch (Exception e) {
				try {
					l.catchExceptionByListener(e, args, null);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
			}
		}
		for (CommandEventListener l : comListeners) {
			try {
				l.doBeforeExecution(args);
			} catch (Exception e) {
				try {
					l.catchExceptionByListener(e, args, null);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
			}
		}
	}
	
	protected void doListenersAfterEvent(List<CommandEventListener> comListeners, CommandArgument args, CommandResult result) {
		for (CommandEventListener l : comListeners) {
			try {
				l.doAfterExecution(args, result);
			} catch (Exception e) {
				try {
					l.catchExceptionByListener(e, args, result);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
			}

			try {
				switch (result.getStatus()) {
					case ERROR:
						l.doResultIsError(args, result);
						break;
					case WARNING:
						l.doResultIsWARN(args, result);
						break;
					case RUNNING:
						l.doResultIsRunning(args, result);
						break;
					case SUCCESS:
						l.doResultIsSuccess(args, result);
						break;
					case UNEXECUTED:
						l.doResultIsUnexecuted(args, result);
				}
			} catch (Exception e) {
				try {
					l.catchExceptionByListener(e, args, result);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
			}
		}

		for (CommandEventListener l : this.comEventListeners) {
			try {
				l.doAfterExecution(args, result);
			} catch (Exception e) {
				try {
					l.catchExceptionByListener(e, args, result);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
			}

			try {
				switch (result.getStatus()) {
					case ERROR:
						l.doResultIsError(args, result);
						break;
					case WARNING:
						l.doResultIsWARN(args, result);
						break;
					case RUNNING:
						l.doResultIsRunning(args, result);
						break;
					case SUCCESS:
						l.doResultIsSuccess(args, result);
						break;
					case UNEXECUTED:
						l.doResultIsUnexecuted(args, result);
				}
			} catch (Exception e) {
				try {
					l.catchExceptionByListener(e, args, result);
				} catch (Exception ec) {
					ec.printStackTrace();
				}
			}
		}
	}
	
	public static CommandManagerBuilder getBuilder() {
		return new CommandManagerBuilder();
	}
}
