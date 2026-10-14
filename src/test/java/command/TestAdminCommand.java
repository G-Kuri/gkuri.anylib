package command;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;

public class TestAdminCommand implements Command {

	@Override
	public String getName() {
		return "admin";
	}

	@Override
	public String getDescription() {
		return "admin desc";
	}
	
	@Override
	public boolean canExecuteCommand(Object user) {
		return false;
	}

	@Override
	public CommandResult execute(CommandArgument args) {
		return new CommandResult("execute admin");
	}
	
	

}
