package gkuri.anylib.exception.command;

import gkuri.anylib.command.CommandManager;

public class CommandException extends RuntimeException{
	
	private int code;
	private String msg;
	private Object error;
	
	public CommandException(int code, String msg, Object error) {
		this(code, error);
		this.msg = msg;
	}
	
	public CommandException(int code, Object error) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for(StackTraceElement s : stackTrace) {
			if(s.getClass().getName().equals(CommandManager.class.getName())) {
				
			}
		}
		
		this.code = code;
		switch (code) {
			case -1:
				this.msg = error.toString();
				break;
			case 1:
				this.msg = "The command is already registered.";
				break;
			case 2:
				this.msg = "Name can't return null or blank.";
				break;
			case 3:
				this.msg = "CommandResult is null.";
				break;
			case 4:
				this.msg = "this command can only use String or Number or Boolean.";
				break;	
			case 5:
				this.msg = "argument type does not match any of the expected patterns.";
				break;
			case 6:
				this.msg = "create interactive thread failed.";
				break;
			case 7:
				this.msg = "the maximum number of concurrent executions allowed on this instance has been exceeded.";
				break;
			case 8:
				this.msg = "the total number of concurrent executions has been exceeded.";
				break;
			case 9:
				this.msg = "an exception was caught during execution.";
				break;
			case 10:
				this.msg = "command is not found.";
				break;
			case 11:
				this.msg = "this CommandArgument has already been used.";
				break;
			case 12:
				this.msg = "this builder has already been used.";
				break;
			case 13:
				this.msg = "maxArgsCount or minArgsCount is invalid.";
				break;
			case 14:
				this.msg = "InteractiveCommand cannot be registered.";
				break;
			default:
				throw new IllegalArgumentException("unknown exception code.[code : " + code + "]");
		}

		this.error = error;
	}

	public int getCode() {
		return this.code;
	}
	
	public Object getError() {
		return this.error;
	}
	
	@Override
	public String getMessage() {
		return this.msg;
	}
	
}
