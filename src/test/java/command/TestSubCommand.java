package command;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgsType;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;

public class TestSubCommand implements Command {

	@Override
	public String getName() {
		return "add";
	}

	@Override
	public String getDescription() {
		return "test sub command (add)";
	}
	
	@Override
	public CommandArgsType getArgsType() {
		return new CommandArgsType()
					.addArgsTypePattern(Integer.class, Integer.class);
	}
	
	@Override
	public int getMinArgsCount() {
		return 2;
	}
	
	@Override
	public int getMaxArgsCount() {
		return 2;
	}

	@Override
	public CommandResult execute(CommandArgument args) {
		int a = (int) args.getArg(0);
		int b = (int) args.getArg(1);
		return new CommandResult("add : " + (a + b));
	}

}
