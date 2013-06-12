package edu.cmu.cs.able.eseb;

import incubator.cmdintf.Command;
import incubator.cmdintf.CommandInterface;
import incubator.cmdintf.CommandManager;
import incubator.cmdintf.ServerSocketCommandManager;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.xml.DOMConfigurator;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Main class that starts the eseb server.
 */
public class MainServer {
	/**
	 * Main method.
	 * @param args command-line arguments.
	 * @throws IOException failed to startup the program
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * Load the log4j configuration.
		 */
		File log4jxml = new File("log4j.xml");
		if (log4jxml.isFile()) {
			DOMConfigurator.configure(log4jxml.getAbsolutePath());
		}
		
		short port = -1;
		short ci_port = -1;
		
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
			} else if (key.equals("cmd-port")) {
				if (ci_port != -1) {
					show_help();
					return;
				}
				
				ci_port = Short.parseShort(value);
				if (ci_port <= 0) {
					show_help();
					return;
				}
			} else {
				show_help();
				return;
			}
		}
		
		if (port == -1) {
			port = 2233;
		}
		
		if (ci_port == -1) {
			ci_port = 2255;
		}
		
		PrimitiveScope scope = new PrimitiveScope();
		
		@SuppressWarnings("resource")
		final BusServer srv = new BusServer(port, scope);
		srv.start();
		
		/*
		 * Set up the command-line interface.
		 */
		final ServerSocketCommandManager cmgr = new ServerSocketCommandManager(
				ci_port);
		cmgr.addCommand(new Command() {
			@Override
			public String getName() {
				return "shutdown";
			}
			@Override
			public String getDescription() {
				return "Shuts down the ebus server.";
			}
			
			@Override
			public void execute(int sid, CommandManager manager,
					CommandInterface cmdInterface) throws Exception {
				srv.close();
				cmgr.shutdown();
				cmdInterface.writeLine("Shutdown command received. "
						+ "Shutting down server.");
			}
		});
	}
	
	/**
	 * Displays command-line help.
	 */
	private static void show_help() {
		System.out.println("Arguments: [--pub-port=2233] [--sub-port=2244] "
				+ "[--cmd-port=2255]");
	}
}
