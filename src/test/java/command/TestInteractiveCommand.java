package command;

import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandResultBuilder;
import gkuri.anylib.command.InteractiveCommand;

public class TestInteractiveCommand implements InteractiveCommand{

    @Override
    public CommandResult execute(CommandArgument args) {
        if(args.getArg(0).equals("throw")) throw new IllegalArgumentException();
        return new CommandResultBuilder().setPrompt("next >").setResult(args.getArg(0).toString()).setInteractiveMode(true).build();
    }

    @Override
    public CommandResult catchException(Exception exception, CommandArgument args) {
        return new CommandResult("swaped default exception msg");
    }
    

}
