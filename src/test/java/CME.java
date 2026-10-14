
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandManager;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.exception.command.CommandException;
import gkuri.anylib.utils.multiValue.Duo;

public class CME extends CommandManager{

    private ConcurrentLinkedQueue<Duo<CommandArgument, CompletableFuture<CommandResult>>> asyncQ = new ConcurrentLinkedQueue<>();
    private Executor executor;

    public CME(CommandManager commandManager, Executor executor){
        super(commandManager, executor);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<CommandResult> executeCommandAsync(CommandArgument args) {
        try {
            CompletableFuture<CommandResult> result = super.executeCommandAsync(args);
            result.whenComplete((r, e) -> {
                Duo<CommandArgument, CompletableFuture<CommandResult>> q = asyncQ.poll();
                executor.execute(() -> {
                    CompletableFuture<CommandResult> asyncResult = super.executeCommandAsync(q.getFirst());
                    try {
						q.getSecond().complete(asyncResult.get());
					} catch (InterruptedException | ExecutionException e1) {
						// TODO 自動生成された catch ブロック
						e1.printStackTrace();
					}
                });
            });
            return result;
        } catch (Exception e) {
            if(e instanceof CommandException ce && (ce.getCode() == 7 || ce.getCode() == 8)) {
                CompletableFuture<CommandResult> q = new CompletableFuture<>();
                asyncQ.add(new Duo<>(args, q));
                return q;
            }
            throw e;
        }
    } 
    
}
