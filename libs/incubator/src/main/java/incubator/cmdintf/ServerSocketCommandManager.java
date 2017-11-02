package incubator.cmdintf;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Command manager that will open a server socket and create command interfaces
 * for all connections using the {@link SocketStreamCommandInterface} class.
 */
public class ServerSocketCommandManager extends CommandManager {
	/**
	 * Logger to use.
	 */
	private static final Logger LOG = Logger.getLogger(
			ServerSocketCommandManager.class);
	
	/**
	 * The server socket.
	 */
	private ServerSocket server;
	
	/**
	 * A client counter.
	 */
	private int clientId;
	
	/**
	 * Should the command manager be shut down?
	 */
	private boolean shutdown;
	
	/**
	 * The acceptor thread.
	 */
	private Thread acceptThread;
	
	/**
	 * Creates a new command manager.
	 * 
	 * @param port the port to listen to
	 * 
	 * @throws IOException failed to create the port
	 */
	public ServerSocketCommandManager(int port) throws IOException {
		server = new ServerSocket(port);
		shutdown = false;
		
		acceptThread = new Thread("SSCM-accept") {
			@Override
			public void run() {
				handleRequests();
			}
			
		};
		
		acceptThread.start();
	}
	
	/**
	 * Shuts down the server and all connections.
	 */
	public void shutdown() {
		synchronized (this) {
			if (shutdown) {
				throw new IllegalStateException("Server already shut down.");
			}
			
			shutdown = true;
			
			try {
				server.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
		
		/*
		 * Close all sessions.
		 */
		Set<Session> sessions = listAllSessions();
		for (Session s : sessions) {
			s.getCommandInterface().writeLine("Server is shutting down.");
			s.getCommandInterface().close();
			while (s.getCommandInterface().isAlive()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					/*
					 * We'll cycle faster.
					 */
				}
			}
		}
		
		while (acceptThread.isAlive()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				/*
				 * We'll cycle faster.
				 */
			}
		}
	}
	
	/**
	 * Handles connection requests on the port.
	 */
	private void handleRequests() {
		while (true) {
			synchronized(this) {
				if (shutdown) {
					break;
				}
			}
			
			try {
				@SuppressWarnings("resource")
				final Socket client = server.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							handleCommands(new SocketStreamCommandInterface(
									client));
						} catch (IOException e) {
							LOG.error("Failed to handle command client.", e);
						}
					}
				}, "SSCM-client-" + (clientId++)).start();
			} catch (IOException e) {
				LOG.error("Failed to accept incoming client.", e);
			}
		}
	}
}
