package command;

import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.events.CommandEventListener;

public class TestEventListener implements CommandEventListener {
	
	private final String range;
	
	public TestEventListener(String range) {
		this.range = range;
	}
	
	public void doBeforeExecution(CommandArgument args) {
		System.out.println("[" + range + "] doBeforeExecution");
	}

	public void doAfterExecution(CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] doAfterExecution");
	}
	
	public void doResultIsError(CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] doResultIsError");
	}
	
	public void doResultIsWARN(CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] doResultIsWARN");
	}
	
	public void doResultIsRunning(CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] doResultIsRunning");
	}
	
	public void doResultIsSuccess(CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] doResultIsSuccess");
	}
	
	public void doResultIsUnexecuted(CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] doResultIsUnexecuted");
	}
	
	public void catchExceptionByListener(Exception exception, CommandArgument args, CommandResult result) {
		System.out.println("[" + range + "] catchExceptionByListener");
	}

}
