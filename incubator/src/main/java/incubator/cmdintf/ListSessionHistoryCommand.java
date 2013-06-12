package incubator.cmdintf;

import java.util.List;

/**
 * Command which lists the last 100 sessions.
 */
public class ListSessionHistoryCommand extends SessionsCommand {
	/**
	 * Maximum number of sessions to retrieve.
	 */
	private static final int SESSION_COUNT = 100;

	/**
	 * Creates a new list session history command.
	 */
	public ListSessionHistoryCommand() {
		super("list-session-history", "Lists closed sessions.");
	}

	@Override
	protected void doExecute() throws Exception {
		CommandManager cmgr = getCommandManager();
		List<Session> sessions = cmgr.getSessionHistory(SESSION_COUNT);
		writeSessions(sessions);
	}
}
