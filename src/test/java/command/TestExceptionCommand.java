package command;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgsType;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandResultStatus;

public class TestExceptionCommand implements Command{

	@Override
	public String getName() {
		return "ex";
	}

	@Override
	public String getDescription() {
		return "ex desc";
	}
	
	@Override
	public int getMinArgsCount() {
		return 1;
	}
	
	@Override
	public int getMaxArgsCount() {
		return 1;
	}
	
	@Override
	public CommandArgsType getArgsType() {
		return new CommandArgsType().addArgsTypePattern(Integer.class);
	}

	@Override
	public CommandResult execute(CommandArgument args) {
		if(args.getArgsTypePatternNumber() == 0) {
			int number = (int)args.getArg(0);
			switch (number) {
				case 0:
					return new CommandResult("arg0", CommandResultStatus.ERROR);
				case 1:
					return new CommandResult("arg1", CommandResultStatus.RUNNING);
				case 2:
					return new CommandResult("arg2", CommandResultStatus.SUCCESS);
				case 3:
					return new CommandResult("arg3", CommandResultStatus.UNEXECUTED);
				case 4:
					return new CommandResult("arg4", CommandResultStatus.WARNING);
				default :
					throw new IllegalArgumentException("unknown arg");
			}
		}
		throw new IllegalArgumentException("args type pattern is -1");
	}
	
	@Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		System.out.println(exception.getMessage());
		return null;
	}

}
