package incubator.cmdintf;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract implementation of a command. This implementation will keep the
 * programmer from having to cope with multi-threading issues (as long as no
 * instance variables are used). It also provides quick access to methods in the
 * command interface by providing methods that will delegate in the interface.
 */
public abstract class AbstractCommand implements Command {
	/**
	 * Name of the command.
	 */
	private String name;

	/**
	 * Description of the command.
	 */
	private String description;

	/**
	 * Command interface being handled (one per thread).
	 */
	private ThreadLocal<CommandInterface> cmdIntf;

	/**
	 * Command manager being handled (one per thread).
	 */
	private ThreadLocal<CommandManager> commandManager;

	/**
	 * Session identifier (one per thread).
	 */
	private ThreadLocal<Integer> sid;

	/**
	 * Creates a new command.
	 * 
	 * @param name the name of the command
	 * @param description a description of the command
	 */
	public AbstractCommand(String name, String description) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (description == null) {
			throw new IllegalArgumentException("description == null");
		}

		this.name = name;
		this.description = description;

		cmdIntf = new ThreadLocal<>();
		commandManager = new ThreadLocal<>();
		sid = new ThreadLocal<>();
	}

	/**
	 * Executes the command. This method should be multi-thread safe. Input /
	 * output can be done through the various <code>readXXX</code> and
	 * <code>writeXXX</code> methods in the command.
	 * 
	 * @throws Exception execution failed
	 */
	protected abstract void doExecute() throws Exception;

	@Override
	public final void execute(int sid, CommandManager manager,
			CommandInterface cmdInterface) throws Exception {
		if (cmdInterface == null) {
			throw new IllegalArgumentException("cmdInterface == null");
		}

		if (manager == null) {
			throw new IllegalArgumentException("manager == null");
		}

		if (sid <= 0) {
			throw new IllegalArgumentException("sid <= 0");
		}

		if (cmdIntf.get() != null) {
			throw new IllegalStateException("Reentrant invocation of "
					+ "'execute'.");
		}

		this.cmdIntf.set(cmdInterface);
		this.commandManager.set(manager);
		this.sid.set(sid);

		try {
			doExecute();
		} finally {
			this.cmdIntf.set(null);
			;
			this.commandManager.set(null);
			this.sid.set(0);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Writes text to the command interface.
	 * 
	 * @param text the text to write
	 */
	protected void write(String text) {
		if (cmdIntf.get() == null) {
			throw new IllegalArgumentException("Can only use 'write' during "
					+ "a command execution.");
		}

		cmdIntf.get().write(text);
	}

	/**
	 * Writes text and a line terminator to the command interface.
	 * 
	 * @param text the text to write
	 */
	protected void writeLine(String text) {
		if (cmdIntf.get() == null) {
			throw new IllegalArgumentException("Can only use 'writeLine' "
					+ "during a command execution.");
		}

		cmdIntf.get().writeLine(text);
	}

	/**
	 * Writes a line terminator to the command interface.
	 */
	protected void writeLine() {
		if (cmdIntf == null) {
			throw new IllegalArgumentException("Can only use 'writeLine' "
					+ "during a command execution.");
		}

		cmdIntf.get().writeLine();
	}

	/**
	 * Reads a line of text from the command interface.
	 * 
	 * @return the text read
	 */
	protected String readLine() {
		if (cmdIntf.get() == null) {
			throw new IllegalArgumentException("Can only use 'readLine' "
					+ "during a command execution.");
		}

		return cmdIntf.get().readLine();
	}

	/**
	 * Obtains a reference to the command interface.
	 * 
	 * @return the command interface
	 */
	protected CommandInterface getCommandInterface() {
		if (cmdIntf.get() == null) {
			throw new IllegalArgumentException("Can only use "
					+ "'getCommandInterface' during a command execution.");
		}

		return cmdIntf.get();
	}

	/**
	 * Obtains a reference to the command manager.
	 * 
	 * @return the command manager
	 */
	protected CommandManager getCommandManager() {
		if (commandManager.get() == null) {
			throw new IllegalArgumentException("Can only use "
					+ "'getCommandManager' during a command execution.");
		}

		return commandManager.get();
	}

	/**
	 * Obtains the current session ID.
	 * 
	 * @return the current session ID
	 */
	protected int getSid() {
		if (sid.get() == 0) {
			throw new IllegalArgumentException("Can only use "
					+ "'getSid' during a command execution.");
		}

		return sid.get();
	}

	/**
	 * Writes a table of data. The first line is printed separately as it is
	 * assumed to be the header.
	 * 
	 * @param data the data to write; each array element corresponds to a line
	 * and each array within the array to the list of columns.
	 */
	protected void writeTable(String[][] data) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		if (data.length == 0) {
			throw new IllegalArgumentException("data.length == 0");
		}

		int rows = data.length;
		int cols = data[0].length;

		/*
		 * Check that all rows have the same number of columns and compute the
		 * maximum length of each column.
		 */
		int[] max = new int[cols];
		for (int r = 0; r < rows; r++) {
			if (data[r].length != cols) {
				throw new IllegalArgumentException("data[" + r + "].length "
						+ "!= data[0].length");
			}

			for (int c = 0; c < cols; c++) {
				if (r == 0) {
					max[c] = StringUtils.length(data[r][c]);
				} else {
					max[c] = Math.max(max[c], StringUtils.length(data[r][c]));
				}
			}
		}

		/*
		 * Compute the separator line.
		 */
		StringBuffer sepLineBuffer = new StringBuffer();
		sepLineBuffer.append('+');
		for (int c = 0; c < cols; c++) {
			sepLineBuffer.append(StringUtils.repeat("-", max[c]));
			sepLineBuffer.append('+');
		}
		String sepLine = sepLineBuffer.toString();

		/*
		 * Write the table down.
		 */
		writeLine(sepLine);
		for (int r = 0; r < rows; r++) {
			StringBuffer lineBuffer = new StringBuffer();
			lineBuffer.append('|');
			for (int c = 0; c < cols; c++) {
				String add = StringUtils.defaultString(data[r][c]);
				add = StringUtils.leftPad(add, max[c]);
				lineBuffer.append(add);
				lineBuffer.append("|");
			}

			writeLine(lineBuffer.toString());

			if (r == 0 || r == rows - 1) {
				writeLine(sepLine);
			}
		}
	}
}
