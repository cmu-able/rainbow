package incubator.cmdintf;

import java.util.List;

/**
 * Command that displays all available commands and their descriptions.
 */
public class HelpCommand extends AbstractCommand {
	/**
	 * Creates a new command.
	 */
	public HelpCommand() {
		super("help", "Lists all available commands and their "
				+ "descriptions.");
	}

	@Override
	protected void doExecute() throws Exception {
		List<Command> cmds = getCommandManager().getCommands();
		
		writeLine();
		for (Command cmd : cmds) {
			writeLine(cmd.getName());
			writeLine(cmd.getDescription());
			writeLine();
		}
	}
}