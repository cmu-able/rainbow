package edu.cmu.cs.able.eseb.ui.srv;

import java.awt.EventQueue;
import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * Class that starts up the server user interface.
 */
public class ServerUiMain {
	/**
	 * Constructor.
	 */
	public ServerUiMain() {
		/*
		 * Load the log4j configuration.
		 */
		File log4jxml = new File("log4j.xml");
		if (log4jxml.isFile()) {
			DOMConfigurator.configure(log4jxml.getAbsolutePath());
		}
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ServerUiMainFrame.make();
			}
		});
	}
	
	/**
	 * Program startup.
	 * @param args unused
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new ServerUiMain();
	}
}
