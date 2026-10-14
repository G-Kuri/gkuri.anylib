package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.List;

public class CommandArgsType {
	
	public static final CommandArgsType NO_TYPE_CHECK = new CommandArgsType(false);
    public static final CommandArgsType STRING_ARGS = new CommandArgsType(true);
	
	private final List<Class<?>[]> argsType;
	
	public CommandArgsType() {
		this.argsType = new ArrayList<>();
	}
	
	private CommandArgsType(boolean isCheck) {
		if(isCheck) {
			this.argsType = new ArrayList<>();
		} else {
			this.argsType = null;
		}
	}
	
	public CommandArgsType addArgsTypePattern(Class<?>... argsType) {
		this.argsType.add(argsType);
		return this;
	}
	
	List<Class<?>[]> getArgsType(){
		return this.argsType;
	}
	
}
