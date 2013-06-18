package incubator.rcli;

public class CommandSyntaxException extends Exception {
	public CommandSyntaxException(String description) {
		super(description);
	}
}
