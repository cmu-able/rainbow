package incubator.cmdintf;

/**
 * General interface of a command.
 */
public interface Command {
	/**
	 * Obtains the command name.
	 * 
	 * @return the command name
	 */
	String getName();
	
	/**
	 * Obtains the command description.
	 * 
	 * @return the command description
	 */
	String getDescription();
	
	/**
	 * Executes the command in a specific session (each time an interface is
	 * handled by the command manager, a new session ID is attributed). Note
	 * that this method may be called by several threads is several clients are
	 * being handled simultaneously. It must be, therefore, thread safe.
	 * 
	 * @param sid the session identified
	 * @param manager the command manager
	 * @param cmdInterface the command interface
	 * 
	 * @throws Exception the command failed to execute
	 */
	void execute(int sid, CommandManager manager,
			CommandInterface cmdInterface) throws Exception;
}
