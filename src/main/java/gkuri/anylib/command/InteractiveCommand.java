package gkuri.anylib.command;

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
