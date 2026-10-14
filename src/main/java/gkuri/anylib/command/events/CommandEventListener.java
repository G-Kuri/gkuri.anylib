package gkuri.anylib.command.events;

import gkuri.anylib.command.CommandArgument;
import gkuri.anylib.command.CommandResult;

/**
 * コマンド実行前後のイベントをリスンするためのクラスです。
 */
public interface CommandEventListener {
	
	/**
	 * コマンド実行前に発火するメソッドです。
	 * @param args
	 */
	default public void doBeforeExecution(CommandArgument args) {
		
	}

	/**
	 * コマンド実行後に発火するメソッドです。
	 * ステータス別メソッドの前に発火します。
	 * @param args
	 * @param result
	 */
	default public void doAfterExecution(CommandArgument args, CommandResult result) {
		
	}
	
	/**
	 * コマンド実行後のステータスがERRORだった際に発火するメソッドです。
	 * doAfterExecutionの後に発火します。
	 * @param args
	 * @param result
	 */
	default public void doResultIsError(CommandArgument args, CommandResult result) {
		
	}
	
	/**
	 * コマンド実行後のステータスがWARNだった際に発火するメソッドです。
	 * doAfterExecutionの後に発火します。
	 * @param args
	 * @param result
	 */
	default public void doResultIsWARN(CommandArgument args, CommandResult result) {
		
	}
	
	/**
	 * コマンド実行後のステータスがRUNNINGだった際に発火するメソッドです。
	 * doAfterExecutionの後に発火します。
	 * @param args
	 * @param result
	 */
	default public void doResultIsRunning(CommandArgument args, CommandResult result) {
		
	}
	
	/**
	 * コマンド実行後のステータスがSUCCESSだった際に発火するメソッドです。
	 * doAfterExecutionの後に発火します。
	 * @param args
	 * @param result
	 */
	default public void doResultIsSuccess(CommandArgument args, CommandResult result) {
		
	}
	
	/**
	 * コマンド実行後のステータスがUNEXECUTEDだった際に発火するメソッドです。
	 * doAfterExecutionの後に発火します。
	 * @param args
	 * @param result
	 */
	default public void doResultIsUnexecuted(CommandArgument args, CommandResult result) {
		
	}
	
	/**
	 * 各メソッドで例外が発生した際に後処理を行うためのメソッドです。
	 * doBeforeExecutionで例外が発生した場合はCommandResultがnullです。
	 * @param exception
	 * @param args
	 * @param result
	 */
	default public void catchExceptionByListener(Exception exception, CommandArgument args, CommandResult result) {
		
	}
	
}
