package command;

import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.events.CommandEventListener;

public class PrintEventListener implements CommandEventListener {

    private void log(String msg) {
        System.out.println("[Listener] " + msg);
    }

    @Override
    public void doBeforeExecution(CommandArgument args) {
        log("before : " + args);
    }

    @Override
    public void doAfterExecution(CommandArgument args, CommandResult result) {
        log("after  : " + result.getStatus());
    }

    @Override
    public void doResultIsSuccess(CommandArgument args, CommandResult result) {
        log("SUCCESS");
    }

    @Override
    public void doResultIsError(CommandArgument args, CommandResult result) {
        log("ERROR");
    }

    @Override
    public void doResultIsWARN(CommandArgument args, CommandResult result) {
        log("WARN");
    }

    @Override
    public void doResultIsRunning(CommandArgument args, CommandResult result) {
        log("RUNNING");
    }

    @Override
    public void doResultIsUnexecuted(CommandArgument args, CommandResult result) {
        log("UNEXECUTED");
    }

    @Override
    public void catchExceptionByListener(Exception e, CommandArgument args, CommandResult result) {
        log("EXCEPTION : " + e);
    }
}