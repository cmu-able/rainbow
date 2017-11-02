package incubator.cmdintf;

import java.util.Set;

/**
 * Command which lists all currently open sessions and their activity.
 */
public class ListSessionsCommand extends SessionsCommand {
	/**
	 * Creates a new list sessions command.
	 */
	public ListSessionsCommand() {
		super("list-sessions", "Lists all currently open sessions.");
	}

	@Override
	protected void doExecute() throws Exception {
		CommandManager cmgr = getCommandManager();
		Set<Session> unsortedSessions = cmgr.listAllSessions();
		writeSessions(unsortedSessions);
	}
}
