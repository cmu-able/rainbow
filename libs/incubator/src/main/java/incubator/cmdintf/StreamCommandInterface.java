package incubator.cmdintf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.lang.SystemUtils;

/**
 * Command interface that reads from an input stream and writes to an output
 * stream.
 */
public class StreamCommandInterface implements CommandInterface {
	/**
	 * The buffered reader to read from.
	 */
	private BufferedReader inr;
	
	/**
	 * The writer to write to.
	 */
	private Writer outw;
	
	/**
	 * Creates a new interface.
	 * 
	 * @param in where to input data from
	 * @param out where to write data to
	 */
	public StreamCommandInterface(InputStream in, OutputStream out) {
		if (in == null) {
			throw new IllegalArgumentException("in == null");
		}
		
		if (out == null) {
			throw new IllegalArgumentException("out == null");
		}
		
		inr = new BufferedReader(new InputStreamReader(in));
		outw = new OutputStreamWriter(out);
	}

	@Override
	public String readLine() {
		try {
			return inr.readLine();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void write(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text == null");
		}
		
		try {
			outw.write(text);
			outw.flush();
		} catch (IOException e) {
			/*
			 * Ignore it.
			 */
		}
	}

	@Override
	public void writeLine(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text == null");
		}
		
		write(text + SystemUtils.LINE_SEPARATOR);
	}

	@Override
	public void writeLine() {
		writeLine("");
	}

	@Override
	public void close() {
		if (inr == null) {
			throw new IllegalStateException("Command interface already "
					+ "closed.");
		}
		
		try {
			inr.close();
			outw.close();
		} catch (IOException e) {
			/*
			 * We'll ignore.
			 */
		}
		
		inr = null;
		outw = null;
	}

	@Override
	public boolean isAlive() {
		return inr != null;
	}
}
