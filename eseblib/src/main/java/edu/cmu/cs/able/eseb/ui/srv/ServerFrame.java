package edu.cmu.cs.able.eseb.ui.srv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Frame that controls an eseb server.
 */
@SuppressWarnings("serial")
public class ServerFrame extends JFrame {
	/**
	 * Creates a new frame.
	 */
	public ServerFrame() {
		super("eseb server interface");
		
		setup_ui();
		
		setVisible(true);
	}
	
	/**
	 * Sets up the user interface.
	 */
	private void setup_ui() {
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);
		
		JMenu file_menu = new JMenu("File");
		mbar.add(file_menu);
		
		JMenuItem connect_item = new JMenuItem("Connect to server...");
		file_menu.add(connect_item);
		
		file_menu.addSeparator();
		
		JMenuItem quit_item = new JMenuItem("Quit");
		file_menu.add(quit_item);
		
		addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) { /**/ }
			@Override public void windowIconified(WindowEvent e) { /**/ }
			@Override public void windowDeiconified(WindowEvent e) { /**/ }
			@Override public void windowDeactivated(WindowEvent e) { /**/ }
			@Override public void windowClosed(WindowEvent e) { /**/ }
			@Override public void windowActivated(WindowEvent e) { /**/ }
			@Override
			public void windowClosing(WindowEvent e) {
				do_quit();
			}
		});
		
		connect_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				do_connect();
			}
		});
		
		quit_item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				do_quit();
			}
		});
		
		setup_enables();
		
		pack();
	}
	
	/**
	 * Sets up enables and disables.
	 */
	private void setup_enables() {
	}
	
	/**
	 * Quits the application.
	 */
	private void do_quit() {
		dispose();
	}
	
	/**
	 * Presents the connect dialog box and connects to the server.
	 */
	private void do_connect() {
		try {
			Socket conn = new Socket("localhost", 2255);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts a new server interface.
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new ServerFrame();
	}
}
