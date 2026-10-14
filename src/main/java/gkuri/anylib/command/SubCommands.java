package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gkuri.anylib.exception.command.CommandException;

public class SubCommands {
	
	public static final SubCommands NON_SUBCOMMANDS = new SubCommands();

	private final Map<String, Command> subCommands;
	private boolean listenSubCommandsEvent = false;
	
	public SubCommands() {
		subCommands = new HashMap<>();
	}
	
	/**
	 * サブコマンドを登録します。
	 * @param command
	 * @return
	 */
	public SubCommands addSubCommands(Command command) {
		if(command == null) throw new NullPointerException();
		if(command.getName() == null || command.getName().isBlank()) throw new CommandException(2, command);
		Command subcom = subCommands.get(command.getName());
		if(subcom != null && subcom.getName().equals(command.getName())) throw new CommandException(1, command);
		subCommands.put(command.getName(), command);
		String[] aliass = command.getAlias();
		if(aliass != null) {
			for(String alias : aliass) {
				if(alias == null || alias.isBlank()) continue;
				subcom = subCommands.get(alias);
				if(subcom == null) {
					subCommands.put(alias, command);
				} else {
					if(command.getAlias().length < subcom.getAlias().length) {
						subCommands.put(alias, command);
					}
				}
			}
		}
		
		return this;
	}
	
	/**
	 * 自分のサブコマンドが実行される際のイベントを拾うか否かを設定します。
	 * デフォルトではイベントを拾いません。
	 * また、"> base sub1 sub2"
	 * のようなコマンド構成の場合、baseでsub2のイベントを拾うには
	 * 前提としてsub1がsub2のイベントを拾う必要があります。
	 * @param listenSubCommandsEvent
	 * @return
	 */
	public SubCommands setListenSubCommandsEvent(boolean listenSubCommandsEvent) {
		this.listenSubCommandsEvent = listenSubCommandsEvent;
		return this;
	}
	
	protected Map<String, Command> getSubCommandsMap(){
		return this.subCommands;
	}
	
	protected boolean canListenSubCommandsEvent() {
		return this.listenSubCommandsEvent;
	}
	
	public List<Command> getSubCommandsList(){
		List<Command> list = new ArrayList<Command>();
		for(String key : this.subCommands.keySet()) {
			list.add(this.subCommands.get(key));
		}
		list.sort(Comparator.comparing(Command::getName));
		return list;
	}
}
