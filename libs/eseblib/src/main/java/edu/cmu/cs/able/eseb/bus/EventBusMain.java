package edu.cmu.cs.able.eseb.bus;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.xml.DOMConfigurator;

import edu.cmu.cs.able.eseb.bus.rci.ConnectionInformationScbSynchronizer;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteConnectionInfo;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterfaceImpl;
import edu.cmu.cs.able.eseb.bus.rci.SyncConstants;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import incubator.rmi.RmiServerPublisher;
import incubator.scb.ScbEditableContainerImpl;
import incubator.scb.sync.SyncScbMaster;
import incubator.scb.sync.SyncScbMasterImpl;
import incubator.scb.sync.SyncScbSlave;

/**
 * Main class that starts eseb.
 */
public class EventBusMain {
	/**
	 * Maximum number of events to get from a queue.
	 */
	private static final int DEFAULT_QUEUE_LIMIT = 100;
	
	/**
	 * Default subscription / publishing port.
	 */
	private static final short DEFAULT_PORT = 2233;
	
	/**
	 * Number of milliseconds for connection information slaves to expire.
	 */
	public static final long SLAVE_DATA_EXPIRATION_MS = 60_000;
	
	/**
	 * Main method.
	 * @param args command-line arguments.
	 * @throws Exception failed to startup the program
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * Load the log4j configuration.
		 */
		File log4jxml = new File("log4j.xml");
		if (log4jxml.isFile()) {
			DOMConfigurator.configure(log4jxml.getAbsolutePath());
		}
		
		short port = -1;
		
		Pattern p = Pattern.compile("^--([^=]+)=(.*)$");
		for (String a : args) {
			Matcher m = p.matcher(a);
			if (!m.matches()) {
				show_help();
				return;
			}
			
			String key = m.group(1);
			String value = m.group(2);
			
			if (key.equals("port")) {
				if (port != -1) {
					show_help();
					return;
				}
				
				port = Short.parseShort(value);
				if (port <= 0) {
					show_help();
					return;
				}
			} else {
				show_help();
				return;
			}
		}
		
		if (port == -1) {
			port = DEFAULT_PORT;
		}
		
		PrimitiveScope scope = new PrimitiveScope();
		
		try(EventBus srv = new EventBus(port, scope)) {
			/*
			 * Set up the data synchronization.
			 */
			ScbEditableContainerImpl<EventBusRemoteConnectionInfo> cic =
					new ScbEditableContainerImpl<>();
			ConnectionInformationScbSynchronizer sync =
					new ConnectionInformationScbSynchronizer(srv, cic);
			
			SyncScbMasterImpl sync_master = new SyncScbMasterImpl(
					SLAVE_DATA_EXPIRATION_MS);
			sync_master.create_container(SyncConstants.CONTAINER_KEY,
					Integer.class, EventBusRemoteConnectionInfo.class);
			short master_port = (short) RmiServerPublisher.publish_service(
					SyncScbMaster.class, sync_master);
			SyncScbSlave data_slave = new SyncScbSlave(sync_master,
					ConnectionInformationScbSynchronizer
					.COUNT_UPDATE_INTERVAL_MS);
			data_slave.add_container(SyncConstants.CONTAINER_KEY, cic,
					Integer.class, EventBusRemoteConnectionInfo.class);
			
			/*
			 * Set up the control interface.
			 */
			EventBusRemoteControlInterfaceImpl ri =
					new EventBusRemoteControlInterfaceImpl(srv,
					DEFAULT_QUEUE_LIMIT, master_port);
			RmiServerPublisher.publish_service(
					EventBusRemoteControlInterface.class, ri);
			
			srv.start();
			
			/*
			 * Waits forever... and we fool the compiler thinking we may
			 * exit :)
			 */
			boolean t = true;
			while (t) {
				try {
					Thread.sleep(SLAVE_DATA_EXPIRATION_MS);
				} catch (InterruptedException e) {
					/*
					 * Ignored.
					 */
				}
			}
			
			sync.shutdown();
			data_slave.shutdown();
		}
	}
	
	/**
	 * Displays command-line help.
	 */
	private static void show_help() {
		System.out.println("Arguments: [--port=" + DEFAULT_PORT + "]");
	}
}
