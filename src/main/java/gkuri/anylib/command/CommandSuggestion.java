package gkuri.anylib.command;

public class CommandSuggestion {

	private Object suggest;
	private String description;
	
	@SuppressWarnings("unused")
	private CommandSuggestion() {
		
	}
	
	public CommandSuggestion(Object suggest) {
		this(suggest, "");
	}

	public CommandSuggestion(Object suggest, String description) {
		this.suggest = suggest;
		this.description = description;
	}
	
	public Object getSuggest() {
		return this.suggest;
	}
	
	public String getDescription() {
		return this.description;
	}
}
