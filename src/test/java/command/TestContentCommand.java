package command;

import java.util.ArrayList;
import java.util.List;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;

public class TestContentCommand implements Command {

	@Override
	public String getName() {
		return "content";
	}

	@Override
	public String getDescription() {
		return "content desc";
	}

	@Override
	public int getMinArgsCount() {
		return 1;
	}
	
	@Override
	public CommandResult execute(CommandArgument args) {
		List<String> result = new ArrayList<>();
		for(Object a : args.getArgs()) {
			result.add(a.toString());
		}
		String target = result.remove(0);
		
		if(args.getBeforeCommandResult() != null) {
			for(String s : args.getBeforeCommandResult().getResult().toString().split("\n")) {
				result.add(s);
			}
		}
		
		for(int i = result.size()-1; 0 <= i; i--) {
			if(!result.get(i).contains(target)) result.remove(i);
		}
		
		StringBuilder sb = new StringBuilder();
		result.forEach((r) -> {
			sb.append(r).append("\n");
		});
		
		return new CommandResult(sb.toString());
	}
	
	@Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		return new CommandResult(exception.getMessage());
	}

}
