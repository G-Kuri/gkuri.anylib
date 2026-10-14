package command;

import java.util.stream.Collectors;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgsType;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandResultBuilder;
import gkuri.anylib.command.SubCommands;

public class TestCommand implements Command {

	@Override
	public String getName() {
		return "test";
	}

	@Override
	public String getDescription() {
		return "test desc";
	}
	
	@Override
	public CommandArgsType getArgsType() {
		return new CommandArgsType()
				.addArgsTypePattern(Integer.class, Integer.class)
				.addArgsTypePattern(Boolean.class, Double.class)
				.addArgsTypePattern(Command.class)
				.addArgsTypePattern(Integer.class)
				.addArgsTypePattern(String.class);
	}
	
	@Override
	public Object stringArgObjectMapper(String arg) {
		
		String[] split = arg.split(":");
		if(split[0].equals("@command")) {
			return Test.getCommand1(split[1]);
		}
		
		return null;
	}
	
	@Override
	public SubCommands getSubCommands() {
		return new SubCommands()
					.addSubCommands(new TestSubCommand())
					.addSubCommands(new TestLateCommand());
	}

    @Override
    public Command getNewInteractiveInstance(Object obj) {
        return new TestInteractiveCommand();
    }

	@Override
	public CommandResult execute(CommandArgument args) {
		String[] options = args.getOptions();
		if(args.getArgsTypePatternNumber() == 4 && args.getArg(0).equals("i")){
			return new CommandResultBuilder().setInteractiveMode(true)
											.setPrompt("next >")
											.setResult("interactive mode")
											.build();
		}

		if(options.length != 0){
			StringBuilder sb = new StringBuilder();
			for(String o : options){
				sb.append(o).append(" ");
			}
			return new CommandResult("typeNum :" + args.getArgsTypePatternNumber() + " type : " + args.getArgs().stream().map(obj -> obj.getClass().getName()).collect(Collectors.joining(", ")) + " (opt : " + String.join(", ", options) + ")");
		}
		return new CommandResult("typeNum :" + args.getArgsTypePatternNumber() + " type : " + args.getArgs().stream().map(obj -> obj.getClass().getName()).collect(Collectors.joining(", ")));
	}
	
	@Override
	public CommandResult catchException(Exception exception, CommandArgument args) {
		exception.printStackTrace();
		return null;
	}
	

}
