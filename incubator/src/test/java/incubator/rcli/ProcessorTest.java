package incubator.rcli;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Test case for the command processor.
 */
@SuppressWarnings("javadoc")
public class ProcessorTest extends DefaultTCase {
	@Test
	public void handle_command() throws Exception {
		try (CommandProcessor cp = new CommandProcessor()) {
			cp.add("hello", new Command() {
				@Override
				public void process_cmd(CommandLine line, Session s)
						throws CommandSyntaxException, IOException {
					s.output("Hello world");
				}
			});
			
			try (PipedInputStream pis = new PipedInputStream();
					Reader pr = new InputStreamReader(pis);
					PipedOutputStream pos = new PipedOutputStream();
					Writer pw = new OutputStreamWriter(pos);
					StringWriter sw = new StringWriter()) {
				
				cp.start_session(pr, sw);
				Thread.sleep(100);
				
				/*
				 * We should get some hello message.
				 */
				assertTrue(sw.getBuffer().length() > 10);
				sw.getBuffer().delete(0, sw.getBuffer().length());
				assertEquals(0, sw.getBuffer().length());
				
				/*
				 * Send the command. Wait a little bit and check that we
				 * have an answer.
				 */
				pw.write("hello\n");
				Thread.sleep(250);
				assertEquals("Hello world\n", sw.toString());
				sw.getBuffer().delete(0, sw.getBuffer().length());
				assertEquals(0, sw.getBuffer().length());
				
				/*
				 * Send two more commands. Wait a little bit and check that we
				 * have an answer.
				 */
				pw.write("hello\nhello\n");
				assertEquals("Hello world\nHello world\n", sw.toString());
				sw.getBuffer().delete(0, sw.getBuffer().length());
			}
		}
	}
}
