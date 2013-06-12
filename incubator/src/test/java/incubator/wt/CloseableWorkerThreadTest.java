package incubator.wt;

import incubator.dispatch.DispatchHelper;

import java.io.EOFException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.RandomGenerator;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Class that checks that the closeable worker thread works.
 */
public class CloseableWorkerThreadTest extends DefaultTCase {
	@SuppressWarnings("javadoc")
	@TestHelper
	private DispatchHelper m_dispatcher_helper;
	
	@SuppressWarnings("javadoc")
	@TestHelper
	private ThreadCountTestHelper m_thread_count_helper;
	
	@SuppressWarnings("javadoc")
	@Test
	public void reading_input_stream_until_eof() throws Exception {
		final boolean cls[] = new boolean[1];
		PipedInputStream pis = new PipedInputStream(15) {
			@Override
			public void close() throws IOException {
				cls[0] = true;
				super.close();
			}
		};
		byte[] send_data = RandomGenerator.randBytes(500);
		final byte[] rec_data = new byte[send_data.length + 1];
		
		/*
		 * This is a weird try-with-resources statement...		
		 */
		try (PipedOutputStream pos = new PipedOutputStream(pis);
				CloseableWorkerThread<PipedInputStream> cwt =
				new CloseableWorkerThread<PipedInputStream>("x", pis, false) {
			public int rr = 0;
			
			@Override
			protected void do_cycle_operation(PipedInputStream closeable)
					throws Exception {
				int r = closeable.read(rec_data, rr, rec_data.length - rr);
				if (r == -1) {
					throw new EOFException();
				}
				
				if (rr == 0) {
					Thread.sleep(10);
				}
				
				rr += r;
			}}) {
		
			final int notf[] = new int[1];
			cwt.add_listener(new CloseableListener() {
				@Override
				public void closed(IOException e) {
					notf[0]++;
				}
			});
			
			cwt.start();
			
			for (int i = 0; i < send_data.length; ) {
				int to_send = RandomGenerator.randInt(11, 27);
				if (to_send > (send_data.length - i)) {
					to_send = send_data.length - i;
				}
				
				pos.write(send_data, i, to_send);
				i += to_send;
				Thread.sleep(10);
			}
			
			pos.close();
			Thread.sleep(50);
			
			assertEquals(WtState.ABORTED, cwt.state());
			assertEquals(true, cwt.closed());
			assertEquals(true, cls[0]);
			assertEquals(1, notf[0]);
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void closing_stream_while_reading() throws Exception {
		final boolean cls[] = new boolean[1];
		PipedInputStream pis = new PipedInputStream(15) {
			@Override
			public void close() throws IOException {
				cls[0] = true;
				super.close();
			}
		};
		
		try (PipedOutputStream pos = new PipedOutputStream(pis);
			CloseableWorkerThread<PipedInputStream> cwt =
					new CloseableWorkerThread<PipedInputStream>("y", pis,
					false) {
				@Override
				protected void do_cycle_operation(PipedInputStream closeable)
						throws Exception {
					if (closeable.read() == -1) {
						throw new EOFException();
					}
				}
			}) {
			final int notf[] = new int[1];
			cwt.add_listener(new CloseableListener() {
				@Override
				public void closed(IOException e) {
					notf[0]++;
				}
			});
			
			cwt.start();
			Thread.sleep(50);
			
			assertEquals(false, cwt.closed());
			cwt.close();
			Thread.sleep(50);
			
			assertEquals(WtState.ABORTED, cwt.state());
			assertEquals(true, cwt.closed());
			assertEquals(true, cls[0]);
			assertEquals(1, notf[0]);
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void removed_listeners_not_notified() throws Exception {
		final boolean cls[] = new boolean[1];
		PipedInputStream pis = new PipedInputStream(15) {
			@Override
			public void close() throws IOException {
				cls[0] = true;
				super.close();
			}
		};
		
		try (PipedOutputStream pos = new PipedOutputStream(pis);
			CloseableWorkerThread<PipedInputStream> cwt =
					new CloseableWorkerThread<PipedInputStream>("y", pis,
					false) {
				@Override
				protected void do_cycle_operation(PipedInputStream closeable)
						throws Exception {
					if (closeable.read() == -1) {
						throw new EOFException();
					}
				}
			}) {
			final int notf[] = new int[1];
			CloseableListener rl = new CloseableListener() {
				@Override
				public void closed(IOException e) {
					notf[0]++;
				}
			};
			cwt.add_listener(rl);
			cwt.add_listener(new CloseableListener() {
				@Override
				public void closed(IOException e) {
					notf[0]++;
				}
			});
			cwt.remove_listener(rl);
			
			assertEquals(false, cwt.closed());
			cwt.close();
			
			Thread.sleep(50);
			
			assertEquals(WtState.STOPPED, cwt.state());
			assertEquals(true, cwt.closed());
			assertEquals(true, cls[0]);
			assertEquals(1, notf[0]);
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void starting_after_closed() throws Exception {
		try (
				PipedInputStream pis = new PipedInputStream(15);
				PipedOutputStream pos = new PipedOutputStream(pis);
				CloseableWorkerThread<PipedInputStream> cwt =
						new CloseableWorkerThread<PipedInputStream>("y", pis,
						false) {
					@Override
					protected void do_cycle_operation(
							PipedInputStream closeable) throws Exception {
						if (closeable.read() == -1) {
							throw new EOFException();
						}
					}
				}) {
			cwt.start();
			assertEquals(WtState.RUNNING, cwt.state());
			Thread.sleep(250);
			assertEquals(WtState.RUNNING, cwt.state());
			cwt.close();
			Thread.sleep(250);
			assertEquals(WtState.ABORTED, cwt.state());
			cwt.start();
			assertEquals(WtState.RUNNING, cwt.state());
			Thread.sleep(250);
			assertEquals(WtState.RUNNING, cwt.state());
			cwt.stop();
			assertEquals(WtState.STOPPED, cwt.state());
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void closing_all_in_a_group() throws Exception {
		WorkerThreadGroup wtg1 = new WorkerThreadGroup("g1");
		WorkerThreadGroup wtg2 = new WorkerThreadGroup("g2");
		wtg1.add_subgroup(wtg2);
		
		WorkerThread t1 = new WorkerThread("t1");
		WorkerThread t2 = new WorkerThread("t1");
		WorkerThread t3 = new WorkerThread("t1");
		wtg1.add_thread(t1);
		wtg1.add_thread(t3);
		wtg2.add_thread(t2);
		wtg2.add_thread(t3);
		
		class MyCT extends CloseableWorkerThread<TestCloseable> {
			public MyCT(String name, TestCloseable closeable) {
				super(name, closeable, false);
			}

			@Override
			protected void do_cycle_operation(TestCloseable closeable)
					throws Exception {
				synchronized (this) {
					wait();
				}
			}
			
		}
		
		try (
				TestCloseable c1 = new TestCloseable();
				TestCloseable c2 = new TestCloseable();
				TestCloseable c3 = new TestCloseable()){
			MyCT ct1 = new MyCT("ct1", c1);
			MyCT ct2 = new MyCT("ct1", c2);
			MyCT ct3 = new MyCT("ct1", c3);
			
			wtg1.add_thread(ct1);
			wtg1.add_thread(ct3);
			wtg2.add_thread(ct2);
			wtg2.add_thread(ct3);
			
			assertFalse(ct1.closed());
			assertFalse(ct2.closed());
			assertFalse(ct3.closed());
			assertEquals(0, c1.m_closed);
			assertEquals(0, c2.m_closed);
			assertEquals(0, c3.m_closed);
			
			CloseableWorkerThreadGroupOps.close_all(wtg1);
			
			assertTrue(ct1.closed());
			assertTrue(ct2.closed());
			assertTrue(ct3.closed());
			assertEquals(1, c1.m_closed);
			assertEquals(1, c2.m_closed);
			assertEquals(1, c3.m_closed);
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void abort_does_not_close_closeable() throws Exception {
		try (TestCloseable c = new TestCloseable();
				TestCloseableWorkerThread<TestCloseable> t =
				new TestCloseableWorkerThread<>(c, false)) {
			t.start();
			Thread.sleep(50);
			
			assertEquals(0, c.m_closed);
			
			t.m_to_throw = new Exception("Kabuuuum");
			Thread.sleep(50);
			
			assertEquals(WtState.ABORTED, t.state());
			assertEquals(0, c.m_closed);
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void abort_closes_closeable() throws Exception {
		try (TestCloseable c = new TestCloseable();
				TestCloseableWorkerThread<TestCloseable> t =
				new TestCloseableWorkerThread<>(c, true)) {
			t.start();
			Thread.sleep(50);
			
			assertEquals(0, c.m_closed);
			
			t.m_to_throw = new Exception("Kabuuuum");
			Thread.sleep(50);
			
			assertEquals(WtState.ABORTED, t.state());
			assertEquals(1, c.m_closed);
		}
	}
}
