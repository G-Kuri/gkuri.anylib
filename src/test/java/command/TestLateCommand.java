package command;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;

public class TestLateCommand implements Command {

	@Override
	public String getName() {
		return "late";
	}

	@Override
	public String getDescription() {
		return "late desc";
	}

	@Override
	public CommandResult execute(CommandArgument args) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new CommandResult("sleep 100ms");
	}

}
