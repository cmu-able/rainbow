package edu.cmu.cs.able.eseb;

import incubator.dispatch.DispatchHelper;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;
import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Checks that input threads work.
 */
@SuppressWarnings("javadoc")
public class InputOutputThreadTest extends DefaultTCase {
	@TestHelper
	private DispatchHelper m_dispatch_helper;
	@TestHelper
	private ThreadCountTestHelper m_count_helper;
	private PrimitiveScope m_scope;
	private DataValueEncoding m_enc;
	
	@Before
	public void set_up() {
		m_scope = new PrimitiveScope();
		m_enc = new DefaultTextEncoding();
	}
	
	@Test
	public void read_data_and_notify_eof() throws Exception {
		try (PipedInputStream pis = new PipedInputStream(17);
				PipedOutputStream pos = new PipedOutputStream(pis)) {
			final TestArraySaveQueue read = new TestArraySaveQueue();
			try (DataTypeInputStreamImpl dis = new DataTypeInputStreamImpl(pis,
					m_enc, m_scope);
					DataTypeInputThread dit = new DataTypeInputThread("DIT",
					dis);
					DataTypeOutputStreamImpl dos =
					new DataTypeOutputStreamImpl(pos, m_enc);
					DataTypeOutputThread dot = new DataTypeOutputThread("DOT",
							dos)) {
				dit.queue_group().add(read);
				
				DataValue[] to_write = new DataValue[3];
				to_write[0] = m_scope.int32().make(14);
				to_write[1] = m_scope.int64().make(14);
				to_write[2] = m_scope.string().make(
						RandomStringUtils.randomAlphabetic(250));
				
				dot.start();
				dit.start();
				
				dot.write(to_write[0]);
				Thread.sleep(5);
				dot.write(to_write[1]);
				Thread.sleep(5);
				dot.write(to_write[2]);
				Thread.sleep(5);
				pos.flush();
				Thread.sleep(50);
				pos.close();
				dot.write(m_scope.int32().make(0));
				Thread.sleep(50);
				
				assertEquals(1, dit.collector().throwables().size());
				assertTrue(dit.collector().throwables().get(0).throwable()
						instanceof IOException);
				assertEquals(1, dot.collector().throwables().size());
				assertTrue(dot.collector().throwables().get(0).throwable()
						instanceof IOException);
				
				assertEquals(to_write.length, read.m_values.size());
				for (int i = 0; i < to_write.length; i++) {
					assertEquals(to_write[i], read.m_values.get(i));
				}
				
				Thread.sleep(50);
				
				assertTrue(dot.closed());
				assertTrue(dit.closed());
			}
		}
		
		Thread.sleep(50);
	}
//	
//	@Test
//	public void removed_listeners_are_not_notified() throws Exception {
//		try (PipedInputStream pis = new PipedInputStream(7);
//				PipedOutputStream pos = new PipedOutputStream(pis);
//				DataTypeInputStreamImpl dis = new DataTypeInputStreamImpl(pis,
//						m_enc);
//				DataTypeInputThread dit = new DataTypeInputThread("DIT", dis);
//				DataTypeOutputStream dos = new DataTypeOutputStreamImpl(pos,
//						m_enc)) {
//			final List<DataValue> read = new ArrayList<>();
//			DataTypeInputListener l1 = new DataTypeInputListener() {
//				@Override
//				public void received(BusData t) {
//					read.add(t.value());
//				}
//			};
//			DataTypeInputListener l2 = new DataTypeInputListener() {
//				@Override
//				public void received(BusData t) {
//					read.add(t.value());
//				}
//			};
//			DataTypeInputListener l3 = new DataTypeInputListener() {
//				@Override
//				public void received(BusData t) {
//					read.add(t.value());
//				}
//			};
//		
//			dit.add_listener(l1);
//			dit.add_listener(l2);
//			dit.remove_listener(l2);
//			dit.add_listener(l3);
//			dit.start();
//			
//			dos.write(m_scope.int32().make(14));
//			pos.flush();
//			Thread.sleep(50);
//			
//			
//			assertEquals(2, read.size());
//			assertEquals(m_scope.int32().make(14), read.get(0));
//			assertEquals(m_scope.int32().make(14), read.get(1));
//			
//			dit.stop();
//		}
//	}
}
