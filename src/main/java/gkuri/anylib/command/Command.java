package gkuri.anylib.command;

import java.util.ArrayList;
import java.util.List;

/**
 * CommandManagerに登録するコマンド用のインターフェイスです。
 * CommandManagerBuilderのaddCommandメソッドから
 * CommandManagerに登録してください。
 * 
 * getName,getDescription,executeメソッドは
 * 必須メソッドです。
 */
public interface Command{
	
	/**
	 * コマンド名を返す
	 * @return
	 */
	public String getName();
	
	/**
	 * コマンドの大まかな説明を返す
	 * @return
	 */
	public String getDescription();
	
	/**
	 * 引数の型を限定するためのメソッド。
	 * 考えうるすべてのパターンを登録すること。
	 * 対話モード時は実行されない。
	 * デフォルトでは型チェックされない。
	 * @return
	 */
	public default CommandArgsType getArgsType() {
		return CommandArgsType.NO_TYPE_CHECK;
	}
	
	/**
	 * コマンドの補完・候補を返すメソッド。
	 * サブコマンドはCommandManagerが自動で追加するため、このメソッドで追加する必要はない。
	 * @param argsTypeNumbers 受け取ったCommandArgumentの最後の引数を除いて型が一致したパターン
	 * @param argsPos 呼び出し元が補完を望んでる引数(0なら第一引数、1なら第二引数)
	 * @param args 現状入力されてるコマンド
	 * @return 引数の候補が入った配列
	 */
	default public CommandSuggestion[] getArgSuggestions(int[] argsTypeNumbers, int argsPos, CommandArgument args) {
		return new CommandSuggestion[0];
	}

	/**
	 * 引数の最小数を限定できるメソッド。
	 * Overrideしないと0が返る。
	 * 対話モード時は実行されない。
	 * @return
	 */
	public default int getMinArgsCount(){
		return 0;
	}
	
	/**
	 * 引数型チェック時にString型引数を受け取った場合、任意のObjectとマッピングや変換を行うためのメソッド。
	 * nullを返すと元のString型引数がそのまま使用される。
	 * 尚、真偽値・数値・日時は自動変換されるため、これら専用にOverrideは不要。
	 * @param arg
	 * @return
	 */
	public default Object stringArgObjectMapper(String arg) {
		return null;
	}
	
	/**
	 * コマンドの別名を配列返す。
	 * 同じ別名を別のコマンドが持っていた場合、
	 * 別名の数が少ない方が優先される。
	 * 別名の数が同じ場合はCommandManagerBuilderに早く登録された方が優先される。
	 * サブコマンドでは別名が自動追加されない。
	 * @return
	 */
	public default String[] getAlias() {
		return new String[0];
	}
	
	/**
	 * help等に表示しない隠しコマンドの場合はtrueを返す。
	 * commandManager.getCommandやcommandManager.getAllCommandsからは確認できる。
	 * @return
	 */
	public default boolean isHiddenCommand() {
		return false;
	}
	
	/**
	 * 引数の最大数を限定できるメソッド。
	 * OverrideしないとInteger.MAX_VALUEが返る。
	 * 対話モード時は実行されない。
	 * @return
	 */
	public default int getMaxArgsCount() {
		return Integer.MAX_VALUE;
	}

	/**
	 * サブコマンドを返すメソッド。
	 * サブコマンドの登録はCommandManagerに行わず
	 * このメソッドを使用すること。
	 * また、nullを返してはいけない。
	 * @return 
	 */
	public default SubCommands getSubCommands(){
		return SubCommands.NON_SUBCOMMANDS;
	}
	
	/**
	 * 対話の時用インスタンスを返すメソッド。
	 * 対話機能を実装したコマンドでは必ずOverrideすること。
	 * インスタンスを新規生成し、対話に必要な変数を格納してreturnを推奨。
	 * 必要な情報はCommandResultにinteractiveDataをセットすることで受け渡し可能。
	 * executeメソッドの戻り値が対話モードだった場合のみ
	 * executeメソッドの直後に動作する。
	 * @param interactiveData 対話モードへ移行する際に渡したい任意のデータ
	 * @return
	 */
	public default Command getNewInteractiveInstance(Object interactiveData){
		return this;
	}
	
	/**
	 * コマンドを実行するユーザが実行権限を持っているかを定義できる。
	 * 実行可能な場合はtrueを、不可能な場合はfalseを返す。
	 * Overrideしない場合は権限確認を行わない。
	 * @param executeUser
	 * @return
	 */
	public default boolean canExecuteCommand(Object executeUser) {
		return true;
	}
	
	/**
	 * helpコマンドを受け取ったときの詳細説明を返す
	 * @return
	 */
	public default String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append(" - ").append(getDescription()).append("\n");
		String[] alias = getAlias();
		if(alias != null && 0 < alias.length) {
			sb.append(" (");
			sb.append(alias[0]);
			for(int i = 1; i < alias.length; i++) {
				sb.append(", ");
				sb.append(alias[i]);
			}
			sb.append(")\n");
		}
		
		List<String> subComs = new ArrayList<>(getSubCommands().getSubCommandsMap().keySet());
		subComs.sort(String::compareTo);
		for(String subCom : subComs) {
			Command c = getSubCommands().getSubCommandsMap().get(subCom);
			if(c.isHiddenCommand()) continue;
			sb.append(" ").append(c.getHelp()).append("\n");
		}
		return sb.toString().trim();
	}
	
	/**
	 * executeメソッド内で例外が発生したときに
	 * 後始末をするためのメソッド
	 * @param exception
	 * @param args
	 */
	public default CommandResult catchException(Exception exception, CommandArgument args) {
		return null;
	}
	
	/**
	 * コマンドが実行されたときの処理を書くメソッド
	 * @param args コマンドの引数や対話モード時のスレッドIDを含む。
	 * @return コマンドの実行結果。NULLは即例外。CommandResultコンストラクタ要確認。
	 */
	public CommandResult execute(CommandArgument args);

}
