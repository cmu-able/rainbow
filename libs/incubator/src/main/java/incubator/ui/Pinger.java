package incubator.ui;

import incubator.pval.Ensure;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Class that "pings" regularly other objects to do something regular.
 */
public class Pinger {
	/**
	 * Normal ping. THe method is invoked asynchronously by the pinging
	 * thread.
	 */
	public static final int NORMAL = 1;
	
	/**
	 * AWT synchronous ping. The method is invoked from within the AWT
	 * dispatcher thread.
	 */
	public static final int AWT_SYNC = 2;
	
	/**
	 * The ping is done by a separate thread launched specifically for this
	 * ping.
	 */
	public static final int SEPARATE = 3;
	
	/**
	 * Pinging thread.
	 */
	private static PingerThread m_pinger_thread;
	
	/**
	 * Ping information: contain the {@link PingInfo} objects to know which
	 * are the registered objects.
	 */
	private static Set<PingInfo> m_ping_info;
	
	/**
	 * Adds an object to the list of objects to be pinged.
	 * @param p the object
	 * @param interval the aproximate internal between pings in milliseconds
	 * @param type the type of ping to perform
	 */
	public static void ping(Pinged p, long interval, int type) {
		create_ping_thread();
		
		synchronized(m_ping_info) {
			PingInfo pi = new PingInfo();
			pi.m_pinged =  p;
			pi.m_interval = interval;
			pi.m_next_ping = System.currentTimeMillis() + interval;
			pi.m_type = type;
			m_ping_info.add(pi);
		}
	}
	
	/**
	 * Removes an object being pinged.
	 * @param p the object
	 */
	public static void unping(Pinged p) {
		synchronized(m_ping_info) {
			for (Iterator<PingInfo> it = m_ping_info.iterator(); it.hasNext(); ) {
				PingInfo pi = it.next();
				if (pi.m_pinged == p) {
					it.remove();
				}
			}
		}
	}
	
	/**
	 * Creates the pinging thread if necessary.
	 */
	private static synchronized void create_ping_thread() {
		if (m_pinger_thread == null) {
			m_ping_info = new HashSet<>();
			m_pinger_thread = new PingerThread();
		}
	}
	
	/**
	 * Thread that does the pinging.
	 */
	private static class PingerThread extends Thread {
		/**
		 * Creates and starts the thread.
		 */
		private PingerThread() {
			super("Pinger");
			
			start();
		}
		
		@Override
		public void run() {
			while(true) {
				check();
				
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Checks if there is anything to do and do it.
		 */
		private void check() {
			Set<PingInfo> todo = new HashSet<>();
			synchronized(m_ping_info) {
				for (PingInfo pi : m_ping_info) {
					if (pi.m_next_ping <= System.currentTimeMillis()) {
						todo.add(pi);
					}
				}
			}
			
			for (final PingInfo pi : todo) {
				switch (pi.m_type) {
					case NORMAL:
						try {
							pi.m_pinged.ping();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						pi.m_next_ping = System.currentTimeMillis()
								+ pi.m_interval;
						break;
					case AWT_SYNC:
						/*
						 * Update will be done by the runnable.
						 */
						pi.m_next_ping = System.currentTimeMillis() + 1000000;
						
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								try {
									pi.m_pinged.ping();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								pi.m_next_ping = System.currentTimeMillis()
										+ pi.m_interval;
							}
						});
						
						break;
					case SEPARATE:
						/*
						 * Update will be done by the thread.
						 */
						pi.m_next_ping = System.currentTimeMillis() + 1000000;
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									pi.m_pinged.ping();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								pi.m_next_ping = System.currentTimeMillis()
										+ pi.m_interval;
							}
						}).start();
						break;
					default:
						Ensure.unreachable();
				}
			}
		}
	}
	
	/**
	 * Maintains information about a "pinged" object.
	 */
	private static class PingInfo {
		/**
		 * The object.
		 */
		private Pinged m_pinged;
		
		/**
		 * Interval between pings, in milliseconds.
		 */
		private long m_interval;
		
		/**
		 * Next time stamp in which the a ping should be done, in system
		 * milliseconds
		 */
		private long m_next_ping;
		
		/**
		 * Type of ping.
		 */
		private int m_type;
	}
}
