package incubator.rcli;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Reads and writes information between the client and the server.
 */
@SuppressWarnings("javadoc")
public class InformationReadAndWriteTest extends DefaultTCase {
	/**
	 * The loopback session to use.
	 */
	private LoopbackSession m_loopback_session;
	
	/**
	 * Prepares the text fixture.
	 */
	@Before
	public void set_up() {
		m_loopback_session = new LoopbackSession();
	}
	
	/**
	 * Cleans up after the test case.
	 */
	@After
	public void clean_up() {
		m_loopback_session.done();
	}
	
	@Test
	public void read_write_number() throws Exception {
		final String[][] configs = new String[][] {
			{ null, null, null, null },
			{ "", "", "", "" },
			{ "foo", "foo", null, null },
			{ null, null, "bar", "bar" },
			{ "aaaaarg: ", "a+rg: ", null, null },
			{ null, null, " (muuu)", " \\(mu*\\)"}
		};
		
		for (int i = 0; i < configs.length; i++) {
			class Cmd extends AbstractCommand {
				public String p, pre, s, sre;
				
				public Cmd(String p, String pre, String s, String sre) {
					super("xx");
					this.p = p;
					this.pre = pre;
					this.s = s;
					this.sre = sre;
				}
				
				@Override
				public void process_cmd(CommandLine line, Session s)
						throws CommandSyntaxException, IOException {
					int v = line.argi(0);
					write_number(this.p, this.s, v, s);
				}
				
				public int run(int i, Session s, Runnable wait_hook)
						throws Exception {
					CommandLine cl = new CommandLine(name(), "" + i);
					s.output(cl.to_single_line());
					wait_hook.run();
					return read_number(pre, sre, s);
				}
			};
			
			for (int j = 0; j < 10; j++) {
				int v = RandomUtils.nextInt();
				final int ii = i;
				int r = new Cmd(configs[i][0], configs[i][1], configs[i][2],
						configs[i][3]).run(v,
						m_loopback_session.client_session(), new Runnable() {
					@Override
					public void run() {
						try {
							new Cmd(configs[ii][0], configs[ii][1],
									configs[ii][2], configs[ii][3]).process_cmd(
									CommandLine.parse(
									m_loopback_session.server_session().input()),
									m_loopback_session.server_session());
						} catch (Exception e) {
							fail();
						}
					}
				});
				
				assertEquals(v, r);
			}
		}
	}
}
