package incubator.cmdintf;

import java.util.Date;

/**
 * The session contains all information about the current state of a client
 * connection.
 */
public class Session {
	/**
	 * The client's session ID.
	 */
	private int sessionId;

	/**
	 * The client's command interface.
	 */
	private CommandInterface cmdIntf;

	/**
	 * Currently running command (if any).
	 */
	private Command running;

	/**
	 * When was the command started or the time at which the last command
	 * finished (if <code>running</code> is <code>null</code>)
	 */
	private Date runningStart;

	/**
	 * When was the session started?
	 */
	private Date sessionStart;

	/**
	 * Last command ran.
	 */
	private Command lastCommand;

	/**
	 * Creates a new object representing the client's state.
	 * 
	 * @param sessionId the client's session ID
	 * @param cmdIntf the client's command interface
	 */
	Session(int sessionId, CommandInterface cmdIntf) {
		if (sessionId <= 0) {
			throw new IllegalArgumentException("sessionId <= 0");
		}

		if (cmdIntf == null) {
			throw new IllegalArgumentException("cmdintf == null");
		}

		this.sessionId = sessionId;
		this.cmdIntf = cmdIntf;
		this.running = null;
		this.runningStart = new Date();
		this.sessionStart = new Date();
		this.lastCommand = null;
	}

	/**
	 * Creates a new session which is a copy of an existing one.
	 * 
	 * @param s the session to copy
	 */
	Session(Session s) {
		if (s == null) {
			throw new IllegalArgumentException("s == null");
		}

		this.sessionId = s.sessionId;
		this.cmdIntf = s.cmdIntf;
		this.running = s.running;
		this.runningStart = s.runningStart;
		this.sessionStart = s.sessionStart;
		this.lastCommand = s.lastCommand;
	}

	/**
	 * Obtains the client's session ID.
	 * 
	 * @return the client's session
	 */
	public int getSessionId() {
		return sessionId;
	}

	/**
	 * Obtains the client's command interface.
	 * 
	 * @return the client's command interface
	 */
	CommandInterface getCommandInterface() {
		return cmdIntf;
	}

	/**
	 * Obtains the command currently being run.
	 * 
	 * @return the command or <code>null</code> if no command is currently being
	 * run
	 */
	public Command getRunningCommand() {
		return running;
	}

	/**
	 * Obtains the time at which the currently running command started or the
	 * time the last command was finished, if no command is currently being run.
	 * 
	 * @return the time
	 */
	public Date getRunningStart() {
		return runningStart;
	}

	/**
	 * Sets the session as being currently running a given command.
	 * 
	 * @param cmd the command or <code>null</code> if no command is currently
	 * being run
	 */
	void currentlyRunning(Command cmd) {
		if (cmd == running) {
			return;
		}

		if (running != null) {
			lastCommand = running;
		}

		running = cmd;
		runningStart = new Date();
	}

	/**
	 * Obtains the time when the session started.
	 * 
	 * @return the time when the session started
	 */
	public Date getSessionStart() {
		return sessionStart;
	}

	/**
	 * Obtains the last command executed.
	 * 
	 * @return the last command executed
	 */
	public Command getLastCommand() {
		return lastCommand;
	}
}
