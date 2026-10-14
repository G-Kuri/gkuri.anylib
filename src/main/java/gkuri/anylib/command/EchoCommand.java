package gkuri.anylib.command;

import gkuri.anylib.setting.SettingFile;

class EchoCommand implements Command{

	private final SettingFile textContents;
	
	EchoCommand(SettingFile textContents){
		this.textContents = textContents;
	}
	
	@Override
	public String getName() {
		return "echo";
	}

	@Override
	public String getDescription() {
		return textContents.getNonNull("echoDescription").toString();
	}

	@Override
	public CommandResult execute(CommandArgument args) {
		
		StringBuilder result = new StringBuilder("");
		boolean nonSpase = args.hasOption("n") || args.hasOptionIgnoreCase("nonSpase");

		for(Object o : args.getArgs()) {
			String s = o.toString();
			result.append(s);
			if(!nonSpase) result.append(" ");
		}
		
		return new CommandResult(result.toString().trim());
	}
	
	@Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		System.out.println(args.toString());
		exception.printStackTrace();
		return null;
	}
	
}
