package gkuri.anylib.command;

import java.util.ArrayList;

import org.mindrot.jbcrypt.BCrypt;

import gkuri.anylib.setting.SettingFile;
import gkuri.anylib.utils.CorrectKeyword;

public class SudoCommand implements Command {
    
    private String hashedPassword;

    private final SettingFile textContents;
    private final CommandManager commandManager;

    public SudoCommand(CommandManager commandManager, SettingFile textContents, String password) {
        this.commandManager = commandManager;
        this.textContents = textContents;
        this.setPassword(password);
    }

    public void setPassword(String pass) {
		if(pass == null) throw new NullPointerException("password is null.");
		this.hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
		// this.hashedPassword = pass;
	}

    @Override
	public String getName(){
        return "sudo";
    }

    @Override
	public String getDescription(){
        return textContents.getNonNull("sudoDescription").toString();
    }
	
    @Override
	public CommandArgsType getArgsType() {
		return CommandArgsType.NO_TYPE_CHECK;
	}

    @Override
	public int getMinArgsCount(){
		return 1;
	}

    @Override
	public SubCommands getSubCommands(){
		SubCommands sub = new SubCommands();
        for(String com : this.commandManager.getAllCommands()){
            sub.addSubCommands(this.commandManager.getCommandClass(com));
        }
        return sub;
	}

    @Override
	public Command getNewInteractiveInstance(Object interactiveData){
		return (InteractiveCommand) interactiveData;
	}
	
    @Override
	public boolean canExecuteCommand(Object executeUser) {
		return true;
	}
	
    @Override
	public String getHelp() {
		return this.textContents.getNonNull("sudoHelp").toString();
	}

    @Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		exception.printStackTrace();
		return null;
	}

    @Override
	public CommandResult execute(CommandArgument args){
        
        if(this.commandManager.getCommandClass(args.getArgs().get(0).toString()) == null) {
			String maybe = CorrectKeyword.correctKeyword(commandManager.getAllCommands(), args.getArg(0).toString());
			String notFound = args.getArg(0) + " : " + textContents.get("commandNotFound");
			if(maybe.equals(args.getArg(0))) {
				return new CommandResultBuilder()
								.setResult(notFound)
								.setStatus(CommandResultStatus.ERROR)
								.build();
			} else {
				return new CommandResultBuilder()
								.setResult(notFound + textContents.getNonNull("commandMaybe").toString().formatted(maybe))
								.setStatus(CommandResultStatus.ERROR)
								.build();
			}
		}

		ArrayList<Object> newArgs = new ArrayList<>(args.getArgs());
		String command = args.getArg(0).toString();
		newArgs.remove(0);
		CommandArgument commandArgument = 
							new CommandArgumentBuilder()
							.setExecuteUser(args.getExecuteUser())
							.setCommand(command)
							.setArgs(newArgs)
							.build();
		commandArgument.setSudo(true);

        return new CommandResultBuilder()
                    .setInteractiveMode(true)
                    .setPrompt(textContents.get("sudoEnterPass") + " >")
                    .setResult("")
                    .setStatus(CommandResultStatus.RUNNING)
					.setInteractiveData(new CheckPassword(this.commandManager, this.textContents, this.hashedPassword, commandArgument))
                    .build();
    }

	private class CheckPassword implements InteractiveCommand {

		private CommandManager manager;
		private SettingFile textContents;
		private String hashedPassword;
		private CommandArgument afterArgs;

		public CheckPassword(CommandManager manager, SettingFile textContents, String hashedPassword, CommandArgument afterArgs){
			this.manager = manager;
			this.textContents = textContents;
			this.hashedPassword = hashedPassword;
			this.afterArgs = afterArgs;
		}

		@Override
		public CommandResult execute(CommandArgument args){
			if(passwordMatch(args.getArgs().get(0).toString())) {
                this.manager.removeCommandThread(args.getInteractiveThreadId());
				
                return this.manager.doCommand(this.afterArgs);
            }
            return new CommandResult(textContents.getNonNull("sudoUnmatchPass").toString(), CommandResultStatus.WARNING);
		}

		private boolean passwordMatch(String plainPassword) {
			if(this.hashedPassword == null) return false;
			try {
				return BCrypt.checkpw(plainPassword, this.hashedPassword);
				// return plainPassword.equals(this.hashedPassword);
			}catch (Exception e) {
				return false;
			}
		}
	}
}

