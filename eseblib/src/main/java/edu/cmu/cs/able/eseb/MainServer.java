package edu.cmu.cs.able.eseb;

import incubator.rmi.RmiCommException;
import incubator.rmi.RmiServerPublisher;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.xml.DOMConfigurator;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Main class that starts the eseb server.
 */
public class MainServer {
	/**
	 * Maximum number of events to get from a queue.
	 */
	private static final int DEFAULT_QUEUE_LIMIT = 100;
	
	/**
	 * Default subscription / publishing port.
	 */
	private static final short DEFAULT_PORT = 2233;
	
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
		
		@SuppressWarnings("resource")
		final BusServer srv = new BusServer(port, scope);
		srv.start();
		
		/*
		 * Set up the control interface.
		 */
		BusServerRemoteInterfaceImpl ri = new BusServerRemoteInterfaceImpl(
				srv, DEFAULT_QUEUE_LIMIT);
		try {
			RmiServerPublisher.publish_service(BusServerRemoteInterface.class,
					ri);
		} catch (RmiCommException e) {
			srv.close();
			throw e;
		}
	}
	
	/**
	 * Displays command-line help.
	 */
	private static void show_help() {
		System.out.println("Arguments: [--port=" + DEFAULT_PORT + "]");
	}
}
