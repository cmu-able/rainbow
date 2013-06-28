package edu.cmu.cs.able.eseb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import edu.cmu.cs.able.eseb.bus.rci.LimitedDistributionQueue;
import edu.cmu.cs.able.eseb.bus.rci.LimitedDistributionQueueElement;
import auxtestlib.ComparisonUtils;
import auxtestlib.DefaultTCase;
import auxtestlib.RandomGenerator;

/**
 * Test case checking the limited distribution queue.
 */
@SuppressWarnings("javadoc")
public class LimitedDistributionQueueTest extends DefaultTCase {
	@Test
	public void queue_element_keeps_data() throws Exception {
		Date d = new Date();
		byte[] c = RandomGenerator.randBytes(10);
		int id = RandomUtils.nextInt();
		
		LimitedDistributionQueueElement e =
				new LimitedDistributionQueueElement(d, c, id);
		assertEquals(d, e.date());
		assertTrue(ComparisonUtils.arrayEquals(c, e.contents()));
		assertEquals(id, e.client_id());
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void cannot_create_queue_element_without_date() throws Exception {
		new LimitedDistributionQueueElement(null, new byte[0], -1);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void cannot_create_queue_element_without_contents()
			throws Exception {
		new LimitedDistributionQueueElement(new Date(), null, -1);
	}
	
	@Test
	@SuppressWarnings("unused")
	public void can_create_queue_element_with_empty_content() throws Exception {
		new LimitedDistributionQueueElement(new Date(), new byte[0], -1);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void create_queue_invalid_size() throws Exception {
		new LimitedDistributionQueue(0);
	}
	
	@Test(expected = AssertionError.class)
	public void add_null_element_to_queue() throws Exception {
		LimitedDistributionQueue q = new LimitedDistributionQueue(5);
		q.add(null);
	}
	
	@Test
	public void get_data_from_empty_queue() throws Exception {
		LimitedDistributionQueue q = new LimitedDistributionQueue(5);
		assertEquals(0, q.all().size());
		assertEquals(5, q.limit());
		assertEquals(0, q.lost());
		assertEquals(0, q.size());
		assertNull(q.started());
	}
	
	@Test
	public void get_data_from_queue_with_few_elements() throws Exception {
		Date d1 = new Date();
		Date d2 = new Date(d1.getTime() + 50);
		Date d3 = new Date(d2.getTime() + 80);
		
		byte[] c1 = RandomGenerator.randBytes(10);
		byte[] c2 = RandomGenerator.randBytes(10);
		byte[] c3 = RandomGenerator.randBytes(10);
		
		LimitedDistributionQueueElement e1 =
				new LimitedDistributionQueueElement(d1, c1, 5);
		LimitedDistributionQueueElement e2 =
				new LimitedDistributionQueueElement(d2, c2, 9);
		LimitedDistributionQueueElement e3 =
				new LimitedDistributionQueueElement(d3, c3, 1);
		
		LimitedDistributionQueue q = new LimitedDistributionQueue(5);
		q.add(e1);
		q.add(e2);
		q.add(e3);
		
		assertEquals(3, q.all().size());
		assertEquals(d1, q.all().get(0).date());
		assertTrue(ComparisonUtils.arrayEquals(c1, q.all().get(0).contents()));
		assertEquals(5, q.all().get(0).client_id());
		assertEquals(d2, q.all().get(1).date());
		assertTrue(ComparisonUtils.arrayEquals(c2, q.all().get(1).contents()));
		assertEquals(9, q.all().get(1).client_id());
		assertEquals(d3, q.all().get(2).date());
		assertTrue(ComparisonUtils.arrayEquals(c3, q.all().get(2).contents()));
		assertEquals(1, q.all().get(2).client_id());
		
		assertEquals(5, q.limit());
		assertEquals(0, q.lost());
		assertEquals(3, q.size());
		assertEquals(d1, q.started());
	}
	
	@Test
	public void get_data_from_overflow_queue() throws Exception {
		Date d1 = new Date();
		Date d2 = new Date(d1.getTime() + 50);
		Date d3 = new Date(d2.getTime() + 80);
		
		byte[] c1 = RandomGenerator.randBytes(10);
		byte[] c2 = RandomGenerator.randBytes(10);
		byte[] c3 = RandomGenerator.randBytes(10);
		
		LimitedDistributionQueueElement e1 =
				new LimitedDistributionQueueElement(d1, c1, 5);
		LimitedDistributionQueueElement e2 =
				new LimitedDistributionQueueElement(d2, c2, 9);
		LimitedDistributionQueueElement e3 =
				new LimitedDistributionQueueElement(d3, c3, 1);
		
		LimitedDistributionQueue q = new LimitedDistributionQueue(2);
		q.add(e1);
		q.add(e2);
		q.add(e3);
		
		assertEquals(2, q.all().size());
		assertEquals(d2, q.all().get(0).date());
		assertTrue(ComparisonUtils.arrayEquals(c2, q.all().get(0).contents()));
		assertEquals(9, q.all().get(0).client_id());
		assertEquals(d3, q.all().get(1).date());
		assertTrue(ComparisonUtils.arrayEquals(c3, q.all().get(1).contents()));
		assertEquals(1, q.all().get(1).client_id());
		
		assertEquals(2, q.limit());
		assertEquals(1, q.lost());
		assertEquals(2, q.size());
		assertEquals(d1, q.started());
	}
	
	@Test
	public void marshall_unmarshall_queue() throws Exception {
		Date d1 = new Date();
		byte[] c1 = RandomGenerator.randBytes(10);
		
		LimitedDistributionQueueElement e1 =
				new LimitedDistributionQueueElement(d1, c1, 5);
		
		LimitedDistributionQueue q = new LimitedDistributionQueue(5);
		q.add(e1);
		
		byte[] conv;
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bout)) {
			oos.writeObject(q);
			conv = bout.toByteArray();
		}
		
		LimitedDistributionQueue qq = null;
		try (ByteArrayInputStream bin = new ByteArrayInputStream(conv);
				ObjectInputStream ois = new ObjectInputStream(bin)) {
			qq = (LimitedDistributionQueue) ois.readObject();
		}
		
		assertNotNull(qq);
		assertNotSame(q, qq);
		
		assertEquals(1, q.all().size());
		assertEquals(d1, q.all().get(0).date());
		assertTrue(ComparisonUtils.arrayEquals(c1, q.all().get(0).contents()));
		assertEquals(5, q.all().get(0).client_id());
		
		assertEquals(5, q.limit());
		assertEquals(0, q.lost());
		assertEquals(1, q.size());
		assertEquals(d1, q.started());
	}
}
