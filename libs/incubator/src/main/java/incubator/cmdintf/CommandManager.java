package incubator.cmdintf;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class that contains commands and executes them on client interfaces. This
 * class does not contain any way of creating the client interfaces. It relies
 * on the {@link #handleCommands(CommandInterface)} being called with the client
 * interfaces. Commands can be added with the {@link #addCommand(Command)}
 * method. The {@link HelpCommand} and {@link ExitCommand} and created
 * automatically.
 */
public class CommandManager {
	/**
	 * Maximum number of session history history to keep.
	 */
	private static final int MAX_SESSION_HISTORY = 1000;

	/**
	 * All known commands.
	 */
	private Set<Command> commands;

	/**
	 * Current sessions.
	 */
	private Set<Session> sessions;

	/**
	 * Closed sessions.
	 */
	private List<Session> sessionHistory;

	/**
	 * The ID of the next session.
	 */
	private int nextSid;

	/**
	 * Creates a new command manager and initializes the default commands.
	 */
	public CommandManager() {
		commands = new TreeSet<>(new Comparator<Command>() {
			@Override
			public int compare(Command o1, Command o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		commands = Collections.synchronizedSet(commands);

		commands.add(new HelpCommand());
		commands.add(new ExitCommand());
		sessions = new HashSet<>();
		sessionHistory = new ArrayList<>();
		nextSid = 1;
	}

	/**
	 * Adds a new command to the command manager. This command will be available
	 * immediately.
	 * 
	 * @param cmd the command to add
	 */
	public void addCommand(Command cmd) {
		if (cmd == null) {
			throw new IllegalArgumentException("cmd == null");
		}

		commands.add(cmd);
	}

	/**
	 * Obtains a list with all available commands.
	 * 
	 * @return all available commands
	 */
	public List<Command> getCommands() {
		return Collections.unmodifiableList(new ArrayList<>(commands));
	}

	/**
	 * Finds the command that has a given name.
	 * 
	 * @param name the command name
	 * 
	 * @return the command found or <code>null</code> if none
	 */
	public Command findCommand(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		for (Command cmd : commands) {
			if (name.equals(cmd.getName())) {
				return cmd;
			}
		}

		return null;
	}

	/**
	 * Opens a new session and keeps handling commands from the given command
	 * interface until it is no longer alive.
	 * 
	 * @param cmdIntf the command interface
	 */
	public void handleCommands(CommandInterface cmdIntf) {
		if (cmdIntf == null) {
			throw new IllegalArgumentException("cmdIntf == null");
		}

		int sid = nextSid;
		nextSid++;
		Session cstate = new Session(sid, cmdIntf);

		synchronized (this) {
			sessions.add(cstate);
		}

		try {
			String input;
			while (cmdIntf.isAlive()) {
				cmdIntf.write("> ");
				input = cmdIntf.readLine();

				input = input.trim();
				if (input.length() == 0) {
					continue;
				}

				String[] stuff = input.split("\\s+", 2);
				if (stuff.length > 1) {
					cmdIntf.writeLine("Command parameters are not supported.");
					continue;
				}

				Command cmd = findCommand(stuff[0]);
				if (cmd == null) {
					cmdIntf.writeLine("Unknown command '" + stuff[0] + "'. "
							+ "Use 'help' for help.");
					continue;
				}

				try {
					cstate.currentlyRunning(cmd);
					cmd.execute(sid, this, cmdIntf);
				} catch (Exception e) {
					if (cmdIntf.isAlive()) {
						cmdIntf.writeLine("Error: " + e.getMessage());
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						cmdIntf.writeLine(sw.toString());
					}
				}

				cstate.currentlyRunning(null);
			}
		} finally {
			sessions.remove(cstate);
			sessionHistory.add(cstate);
			while (sessionHistory.size() > MAX_SESSION_HISTORY) {
				sessionHistory.remove(0);
			}
		}

		if (cmdIntf.isAlive()) {
			cmdIntf.close();
		}
	}

	/**
	 * Obtains a copy of all existing sessions.
	 * 
	 * @return the sessions currently open
	 */
	public synchronized Set<Session> listAllSessions() {
		Set<Session> sessions = new HashSet<>();
		for (Session s : this.sessions) {
			sessions.add(new Session(s));
		}

		return sessions;
	}

	/**
	 * Obtains several sessions from the history.
	 * 
	 * @param n the maximum number of sessions to obtain
	 * 
	 * @return the list with sessions
	 */
	public synchronized List<Session> getSessionHistory(int n) {
		List<Session> sessions = new ArrayList<>();
		if (n > sessionHistory.size()) {
			n = sessionHistory.size();
		}

		sessions.addAll(sessionHistory.subList(0, n));
		return sessions;
	}
}
