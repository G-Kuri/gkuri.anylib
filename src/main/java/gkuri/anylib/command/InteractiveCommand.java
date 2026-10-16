package gkuri.anylib.command;

/**
 * 対話モード中の処理を実装するCommandクラスで
 * 実装する必要がない必須メソッドをdefault実装したインターフェイスです。
 * このインターフェイスのクラスはCommandManager及びサブコマンドに登録することはできません。
 */
public interface InteractiveCommand extends Command{
    
    @Override
	default public String getName(){
        return "InteractiveCommand : " + this.getClass().getName();
    }
	
    @Override
	default public String getDescription(){
        return getName();
    }
	
	public default String getHelp() {
		return getName();
	}
}
