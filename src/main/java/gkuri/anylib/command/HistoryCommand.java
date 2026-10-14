package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import gkuri.anylib.setting.SettingFile;
import gkuri.anylib.utils.cast.ClassCastUtil;

class HistoryCommand implements Command{
	
	private final SettingFile textContents;
	final List<CommandArgument> argsHistory = new LinkedList<>();
	private final AtomicInteger holedHistoriesCount;
	
	HistoryCommand(SettingFile textContents, int holedHistoriesCount) {
		this.holedHistoriesCount = new AtomicInteger(holedHistoriesCount);
		this.textContents = textContents;
	}

	@Override
	public String getName() {
		return "history";
	}

	@Override
	public String getDescription() {
		return textContents.getNonNull("historyDescription").toString();
	}
	
	@Override
	public String getHelp() {
		return textContents.getNonNull("historyHelp").toString();
	}
	
	@Override
	public CommandArgsType getArgsType(){
		return new CommandArgsType()
			.addArgsTypePattern(Integer.class)
			.addArgsTypePattern(String.class)
			.addArgsTypePattern(String.class, Integer.class);
	}
	
	synchronized void addHistory(CommandArgument args){
		argsHistory.add(args);
	    while (argsHistory.size() > holedHistoriesCount.get()) {
	    	argsHistory.remove(0);
	    }
	}
	
	synchronized List<CommandArgument> getHistories(){
		return new ArrayList<>(this.argsHistory);
	}

	@Override
	synchronized public CommandResult execute(CommandArgument args) {
		String result;
//		System.out.println("ptn : " + args.getArgsTypePatternNumber());
//		if(!args.argsIsEmpty())System.out.println(args.getArg(0).getClass());
		switch (args.getArgsTypePatternNumber()) {
			case 0:
			    result = intPattern((int) args.getArg(0));
			    break;
			case 1:
				result = stringPattern(args.getArgs().get(0).toString());
				break;
			case 2:
				result = stringIntPattern(args.getArgs().get(0).toString(), ClassCastUtil.cast(args.getArgs().get(1), Integer.class));
				break;
			default:
				result = intPattern(this.argsHistory.size());
		}
		
		return new CommandResult(result);
	}
	
	private String intPattern(int size) {
		StringBuilder sb = new StringBuilder();
		
		size = Math.max(0, argsHistory.size() - size);
		for(int i = size; i < this.argsHistory.size(); i++) {
			sb.append(this.argsHistory.get(i).getNativeCommand()).append("\n");
		}
		
		return sb.toString();
	}
	
	private String stringPattern(String command) {
		StringBuilder sb = new StringBuilder();
		
		for(CommandArgument arg : this.argsHistory) {
			String stringArg = arg.getNativeCommand();
			if(stringArg.startsWith(command)) sb.append(stringArg).append("\n");
		}
		
		return sb.toString();
	}
	
	private String stringIntPattern(String command, int size) {
		ArrayList<String> argsList = new ArrayList<>();
		
		for(CommandArgument arg : this.argsHistory) {
			String stringArg = arg.getNativeCommand();
			if(stringArg.startsWith(command)) argsList.add(stringArg);
		}
		
		StringBuilder sb = new StringBuilder();
		
		int start = argsList.size() - size;
		if(start < 0) start = 0;
		
		for(int i = start; i < argsList.size(); i++) {
			sb.append(argsList.get(i)).append("\n");
		}
		
		return sb.toString();
	}

	@Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		exception.printStackTrace();
		return null;
	}
}
