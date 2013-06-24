package incubator.rcli;

import java.io.IOException;

public interface Command {
	/**
	 * Executes this command which was invoked with the given command line.
	 * @param line the command line
	 * @param s the session the command is executing in
	 * @throws CommandSyntaxException failed to understand the command
	 * @throws IOException failed to read/write to the session
	 */
	void process_cmd(CommandLine line, Session s)
			throws CommandSyntaxException, IOException;
}
