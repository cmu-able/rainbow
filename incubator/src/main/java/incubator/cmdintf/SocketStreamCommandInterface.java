package incubator.cmdintf;

import java.io.IOException;
import java.net.Socket;

/**
 * Command interface that reads and writes from a network socket.
 */
public class SocketStreamCommandInterface extends StreamCommandInterface {
	/**
	 * Creates a new command interface.
	 * 
	 * @param socket the socket to read and write data to.
	 * 
	 * @throws IOException failed to obtain the socket's streams
	 */
	public SocketStreamCommandInterface(Socket socket) throws IOException {
		super(socket.getInputStream(), socket.getOutputStream());
	}
}
