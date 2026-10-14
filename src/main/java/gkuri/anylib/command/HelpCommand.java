package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.setting.SettingFile;
import gkuri.anylib.utils.CorrectKeyword;

class HelpCommand implements Command{
	
	private final CommandManager commandManager;
	private final SettingFile langSettingFile;
	
	HelpCommand(CommandManager commandManager, SettingFile langSettingFile){
		this.commandManager = commandManager;
		this.langSettingFile = langSettingFile;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getDescription() {
		return langSettingFile.getNonNull("helpDescription").toString();
	}
	
	@Override
	public String getHelp() {
		return langSettingFile.getNonNull("helpHelp").toString();
	}
	
	@Override
	public CommandSuggestion[] getArgSuggestions(int[] argsTypeNumbers, int argsPos, CommandArgument args) {
//		System.out.println("num:%s pos:%d arg:%s".formatted(Arrays.toString(argsTypeNumbers), argsPos, args));
		
		if(args.getArgsCount() == 0) {
			String[] allComs = commandManager.getAllCommands();
			CommandSuggestion[] result = new CommandSuggestion[allComs.length];
			for(int i = 0; i < allComs.length; i++) {
				Command com = commandManager.getCommandClass(allComs[i]);
				result[i] = new CommandSuggestion(com.getName(), com.getDescription());
			}
			return result;
		}
		
		Command com = commandManager.getCommandClass(args.getArg(0).toString());
		if(com != null) {
			for(int i = 1; i < argsPos; i++) {
				if(com == null) return new CommandSuggestion[0];
				com = com.getSubCommands().getSubCommandsMap().get(args.getArg(i).toString());
			}
			
			List<Command> subComs = com.getSubCommands().getSubCommandsList();
			CommandSuggestion[] result = new CommandSuggestion[subComs.size()];
			for(int i = 0; i < subComs.size(); i++) {
				result[i] = new CommandSuggestion(subComs.get(i).getName(), subComs.get(i).getDescription());
			}
			return result;
		} else {
			String[] allComs = commandManager.getAllCommands();
			List<CommandSuggestion> result = new ArrayList<>();
			String target = args.getArg(0).toString();
			boolean fined = false;
			for(String c : allComs) {
				if(c.startsWith(target)) {
					fined = true;
					com = commandManager.getCommandClass(c);
					result.add(new CommandSuggestion(com.getName(), com.getDescription()));
					continue;
				}
				
				if(fined) break;
			}
			
			return result.toArray(new CommandSuggestion[0]);
		}
	}

	@Override
	public CommandResult execute(CommandArgument args) {
		
		if(!args.argsCountIs(0)) {
			Command c = commandManager.getCommandClass(args.getArg(0).toString());
			SubCommands sub = null;
			if(c == null) return new CommandResult(args.getArg(0) + " : " + langSettingFile.getNonNull("commandNotFound"));

			String targetCommand = c.getName();
			for(int i = 1; i < args.getArgsCount(); i++){
				targetCommand = args.getArg(i).toString();
				sub = c.getSubCommands();
				c = sub.getSubCommandsMap().get(targetCommand);
				if(c == null){
					return new CommandResult(targetCommand + " : " + langSettingFile.getNonNull("commandNotFound"));
				}
			}
			String help = c.getHelp();

			if(help != null) {
				return new CommandResult(help);
			} else if(sub != null) {
				String maybe = CorrectKeyword.correctKeyword(sub.getSubCommandsMap().keySet().toArray(new String[0]), args.getArgs().get(0).toString());
				String notFound = targetCommand + " : " + langSettingFile.getNonNull("commandNotFound");
				if(maybe.equals(targetCommand)) {
					return new CommandResultBuilder()
									.setResult(notFound)
									.setStatus(CommandResultStatus.ERROR)
									.build();
				} else {
					return new CommandResultBuilder()
							.setResult(notFound + langSettingFile.getNonNull("commandMaybe").toString().formatted(maybe))
							.setStatus(CommandResultStatus.ERROR)
							.build();
				}
			} else {
				String notFound = targetCommand + " : " + langSettingFile.getNonNull("commandNotFound");
				return new CommandResultBuilder()
					.setResult(notFound)
					.setStatus(CommandResultStatus.ERROR)
					.build();
			}
		} else {
			
			StringBuilder result = new StringBuilder("");
			
			for(String comName : commandManager.getAllCommands()) {
				Command c = commandManager.getCommandClass(comName);
				if(c.getName().equals(comName) && !c.isHiddenCommand()) {
					result.append(c.getName());
					result.append(" - ");
					result.append(c.getDescription());
					result.append("\n");
				} 
			}
			
			return new CommandResult(result.toString());
			
		}
	}
	
	@Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		exception.printStackTrace();
		return null;
	}
	
}