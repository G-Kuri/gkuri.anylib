package command;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import gkuri.anylib.TextContents;
import gkuri.anylib.command.Command;
import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandArgumentBuilder;
import gkuri.anylib.command.CommandManager;
import gkuri.anylib.command.CommandManagerBuilder;
import gkuri.anylib.command.CommandResult;
import gkuri.anylib.command.CommandSuggestion;

public class Test {

	static final CommandManager cm1 = new CommandManagerBuilder()
								.addHelpCommand(true)
								.addEchoCommand(true)
								.addHistoryCommand(true)
								.addSudoCommand(true, "pass")
								.showTypoSuggest(true)
								.setMaxRunningAsyncCommandsCount(2)
								.setHoldHistoriesCount(20)
								.addCommand(new TestCommand())
								.addCommand(new TestAdminCommand())
								.addCommand(new TestContentCommand())
								.build();
	static final CommandManager cm2 = new CommandManagerBuilder()
								.addHelpCommand(false)
								.addEchoCommand(false)
								.addHistoryCommand(false)
								.addSudoCommand(false, null)
								.showTypoSuggest(false)
								.setLanguage(TextContents.EN_US)
								.setMaxRunningAsyncCommandsCount(10)
								.addCommand(new TestCommand())
								.addCommand(new TestAdminCommand())
								.addCommand(new TestExceptionCommand())
								.addCommandEventListener(new TestEventListener("all"))
								.addCommandEventListener(TestCommand.class, new TestEventListener("test"))
								.build();

	public static void main(String[] args) {
		CommandManager.setAllMaxRunningAsyncCommandsCount(3);

		//コマンド登録確認
		printResult1("help");
		printResult1("help sudo");
		printResult1("help test");

		//echo動作確認
		printResult1("echo test test");

		//history動作確認
		printResult1("history");
		printResult1("history 2");
		printResult1("history help");
		printResult1("history history 3");

		//未登録コマンド(候補なし)
		printResult1("aaaaaaaa");

		//未登録コマンド(候補アリ)
		printResult1("tezt");

		//tab補完的な候補
		System.out.println("==suggestions(h)==");
		for(CommandSuggestion s : cm1.getCommandSuggestions("h")) {
			System.out.println(s.getSuggest());
		}
		System.out.println();

		//引数パターン判定(by文字列)
		printResult1("test");
		printResult1("test 123 456");
		printResult1("test true 78.9");
		printResult1("test 100");
		printResult1("test str");
		//オプション付き引数パターン判定
		printResult1("test -ab -c -d --qwer 123 456");
		printResult1("test -a -b -cd false --qwer 78.9");
		printResult1("test -a -- --qwer");

		//引数パターン判定(by Javaオブジェクト)
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs().build());
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs(123, 456).build());
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs(true, 78.9).build());
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs(new TestCommand()).build());
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs("--opt", new TestCommand()).build());
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs(100).build());
		printResult1(new CommandArgumentBuilder().setCommand("test").setArgs("str").build());
		
		//サブコマンド解決
		printResult1("test add 1 2");

		//ダブルクオーテーションを単一引数に
		CommandArgument arg = new CommandArgument("test \"987 654\"");
		printResult1(arg);
		//同じCommandArgument使いまわしエラー
		printResult1(arg);

		//権限チェック
		printResult1("admin");
		printResult1("sudo");
		//sudoパスワード判定チェック
		printResultInteractive1("sudo admin", "unknownPass");
		printResultInteractive1("sudo admin", "pass");
		printResultInteractive1("test i", "1", "2", "3", "throw", "4");

		//非同期の同時実行上限(インスタンス)チェック
		ArrayList<CompletableFuture<CommandResult>> asyncs = new ArrayList<>();
		for(int i = 0; i < 3; i++) {
			try {
				System.out.println("> test late");
				asyncs.add(cm1.executeCommandAsync(new CommandArgument("test late")));
				System.out.println();
			}catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println();
			}
		}
		
		//非同期の同時実行上限(JVM全体)チェック
		for(int i = 0; i < 2; i++) {
			try {
				System.out.println("> test late");
				asyncs.add(cm2.executeCommandAsync(new CommandArgument("test late")));
				System.out.println();
			}catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println();
			}
		}
		
		//別のCommandManagerのコマンドが混入してないかチェック
		printResult2("help");
		for(String c : cm2.getAllCommands()) {
			System.out.println(c);
		}
		System.out.println();

		//イベント発火チェック(結果別)
		printResult2("ex a");
		printResult2("ex 1");
		printResult2("ex 2");
		printResult2("ex 3");
		printResult2("ex 4");
		printResult2("ex 5");
		//イベント発火チェック(コマンド別)
		printResult2("tezt");
		printResult2("test");
		
		//履歴がCommandManagerを横断してないかチェック
		printResult1("history");
		
		//パイプ接続チェック
		printResult1("help");
		printResult1("help | content e");
		printResult1("help | content e | content c");
		printResult1("help | content e | content c | content s");
		printResult1("help | content e | content c | content s | content t");
		
		//stringArgObjectMapper型変換チェック
		printResult1("test @command:test");

		//遅れて非同期処理完了通知
		for(CompletableFuture<CommandResult> rslt : asyncs) {
			try {
				System.out.println(rslt.toCompletableFuture().get().getResult().toString().trim());
				System.out.println();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println();
			}
			
		}

		Scanner scanner = new Scanner(System.in);
		System.out.print("> ");
		String com = scanner.nextLine();
		Long itId = null;
		while (!com.equals("exit")) {
			if(com.endsWith("\t")){
				for(CommandSuggestion s : cm1.getCommandSuggestions(com.trim())){
					System.out.println(s.getSuggest());
				}
				System.out.print("> ");
			} else {
				CommandResult result = cm1.executeCommand(new CommandArgumentBuilder().setCommand(com).setInteractiveThreadId(itId).build());
				if(result.isInteractiveMode()){
					System.out.print(result.getPrompt() + " ");
				} else {
					System.out.println(result.getResult());
					System.out.print("> ");
				}
				itId = result.getInteractiveThreadId();
			}
			com = scanner.nextLine();
		}
	}
	
	private static void printResult1(String command) {
		try {
			System.out.println("> " + command);
			System.out.println(cm1.executeCommand(new CommandArgument(command)).getResult().toString().trim());
			System.out.println();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println();
		}
		
	}
	
	private static void printResult1(CommandArgument command) {
		try {
			System.out.println("> " + command.getNativeCommand());
			System.out.println(cm1.executeCommand(command).getResult().toString().trim());
			System.out.println();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println();
		}
		
	}

	private static void printResultInteractive1(String command, String... interactives){
		try {
			System.out.println("> " + command);
			CommandResult result = cm1.executeCommand(new CommandArgument(command));
			if(!result.isInteractiveMode()){
				System.out.println(result.getResult().toString().trim());
				System.out.println();
				return;
			}
			for(String i : interactives){
				if(result.isInteractiveMode()){
					try {
						System.out.println(result.getPrompt() + " " + i);
						result = cm1.executeCommand(new CommandArgumentBuilder().setArgs(i).setInteractiveThreadId(result.getInteractiveThreadId()).build());
						System.out.println(result.getResult().toString().trim());
					} catch (Exception e) {
						System.out.println(e.getMessage());
						System.out.println();
					}
				}
			}
			System.out.println();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println();
		}
	}
	
	private static void printResult2(String command) {
		try {
			System.out.println("> " + command);
			System.out.println(cm2.executeCommand(new CommandArgument(command)).getResult().toString().trim());
			System.out.println();	
		} catch (Exception e) {
			// System.out.println(e.getMessage());
			// System.out.println();
			e.printStackTrace();
		}
		
	}
	
	public static Command getCommand1(String com) {
		return cm1.getCommandClass(com);
	}
	
}


