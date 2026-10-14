package command;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandResultBuilder;
import gkuri.anylib.command.CommandResultStatus;

public class InteractiveTestCommand implements Command {

    @Override
    public String getName() {
        return "interactive";
    }

    @Override
    public String getDescription() {
        return "対話モードテスト用コマンド";
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
    public CommandResult execute(CommandArgument args) {
        if(args.getCommand() != null) {
            return new CommandResultBuilder()
                .setInteractiveMode(true)
                .setPrompt(">> ")
                .setResult("executed args=" + args)
                .setStatus(CommandResultStatus.SUCCESS)
                .build();
        }
        String arg = args.getArgsCount() > 0 ? args.getArg(0).toString() : "<none>";

        // CommandResultBuilder を使用
        if(args.getArg(0).toString().equals("exit")){
            return new CommandResultBuilder()
                .setInteractiveMode(false)
                .setResult("executed args=" + arg)
                .setStatus(CommandResultStatus.SUCCESS)
                .build();
        }
        CommandResult result = new CommandResultBuilder()
                .setInteractiveMode(args.getInteractiveThreadId() != null)
                .setPrompt(">> ")
                .setResult("executed args=" + arg)
                .setStatus(CommandResultStatus.SUCCESS)
                .build();

        return result;
    }

    @Override
    public Command getNewInteractiveInstance(Object object) {
        return new InteractiveTestCommand();
    }
}