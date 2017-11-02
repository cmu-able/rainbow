package incubator.cmdintf;

/**
 * Command that closes the current session.
 */
public class ExitCommand extends AbstractCommand {
	/**
	 * Creates a new command.
	 */
	public ExitCommand() {
		super("exit", "Closes the current session.");
	}

	@Override
	protected void doExecute() throws Exception {
		writeLine("Bye bye! Nice chatting with you!");
		getCommandInterface().close();
	}
}
