package command;

import java.util.List;

import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgsType;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandResultBuilder;
import gkuri.anylib.command.CommandResultStatus;

public class TypeCommand implements Command {

    @Override
    public String getName() {
        return "typeTest";
    }

    @Override
    public String getDescription() {
        return "型チェックテスト用コマンド";
    }

    @Override
    public CommandArgsType getArgsType() {
        // addArgsTypePattern で複数パターン登録可能
        return new CommandArgsType()
                .addArgsTypePattern(Integer.class, Boolean.class)
                .addArgsTypePattern(String.class, String.class);
    }

    @Override
    public int getMinArgsCount() {
        return 2;
    }

    @Override
    public int getMaxArgsCount() {
        return 2;
    }

    @Override
    public CommandResult execute(CommandArgument args) {
        List<Object> castedArgs = args.getArgs(); // CommandManager 内で classCastArgs 済み
        String resultStr = "executed args: ";
        for (Object o : castedArgs) {
            resultStr += o + " ";
        }

        return new CommandResultBuilder()
                .setResult(resultStr.trim())
                .setStatus(CommandResultStatus.SUCCESS)
                .build();
    }
}