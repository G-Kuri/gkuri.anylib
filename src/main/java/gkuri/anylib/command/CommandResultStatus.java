package gkuri.anylib.command;

public enum CommandResultStatus {
	ERROR(500, "error"),
	WARNING(400, "warning"),
	RUNNING(300, "running"),
	SUCCESS(200, "success"),
	UNEXECUTED(100, "unexecuted");
	
	private final int code;
	private final String label;
	
	CommandResultStatus(int code, String label) {
		this.code = code;
		this.label = label;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public String toString() {
		return "CommandResultStatus[ code : %d || label : %s ]".formatted(this.code, this.label);
	}
}
