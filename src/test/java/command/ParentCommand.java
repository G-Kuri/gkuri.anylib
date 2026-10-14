package command;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandResultStatus;
import gkuri.anylib.command.SubCommands;

public class ParentCommand implements Command {

    private final SubCommands subs = new SubCommands()
        .addSubCommands(new ChildCommand());

    @Override
    public String getName() {
        return "parent";
    }

    @Override
    public String getDescription() {
        return "parent command";
    }

    @Override
    public SubCommands getSubCommands() {
        return subs;
    }

    @Override
    public CommandResult execute(CommandArgument args) {
        return new CommandResult("parent executed", CommandResultStatus.SUCCESS);
    }
}

class ChildCommand implements Command {

    private final SubCommands subs = new SubCommands()
        .addSubCommands(new GrandChildCommand());

    @Override
    public String getName() {
        return "child";
    }

    @Override
    public String getDescription() {
        return "child command";
    }

    @Override
    public SubCommands getSubCommands() {
        return subs;
    }

    @Override
    public CommandResult execute(CommandArgument args) {
        return new CommandResult("child executed", CommandResultStatus.SUCCESS);
    }
}

class GrandChildCommand implements Command {

    @Override
    public String getName() {
        return "grand";
    }

    @Override
    public String getDescription() {
        return "grand child command";
    }

    @Override
    public int getMinArgsCount() {
        return 1;
    }

    @Override
    public CommandResult execute(CommandArgument args) {
        return new CommandResult(
            "grand arg=" + args.getArg(0),
            CommandResultStatus.SUCCESS
        );
    }
}
