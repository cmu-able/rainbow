package edu.cmu.cs.able.eseb.ui;
//
//import incubator.ui.IconResourceLoader;
//
//import java.awt.BorderLayout;
//import java.awt.EventQueue;
//import java.lang.reflect.InvocationTargetException;
//
//import javax.swing.ImageIcon;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//import org.apache.log4j.Logger;
//
//import edu.cmu.cs.able.eseb.BusConnection;
//
///**
// * Component that shows the connection state.
// */
//@SuppressWarnings("serial")
//public class ConnectionStateComponent extends JPanel {
//	/**
//	 * Logger to use.
//	 */
//	private static final Logger LOG = Logger.getLogger(
//			ConnectionStateComponent.class);
//	
//	/**
//	 * The connection.
//	 */
//	private BusConnection conn;
//	
//	/**
//	 * Are we currently connected?
//	 */
//	private boolean connected;
//	
//	/**
//	 * Connected text.
//	 */
//	private String connectedString;
//	
//	/**
//	 * Disconnected text.
//	 */
//	private String disconnectedString;
//	
//	/**
//	 * Label with text.
//	 */
//	private JLabel textLabel;
//	
//	/**
//	 * Label with icon.
//	 */
//	private JLabel iconLabel;
//	
//	/**
//	 * Connection is OK.
//	 */
//	private ImageIcon goodIcon;
//	
//	/**
//	 * Connection is not OK:
//	 */
//	private ImageIcon badIcon;
//	
//	/**
//	 * Creates a new component.
//	 * @param c the bus connection
//	 */
//	public ConnectionStateComponent(BusConnection c) {
//		if (c == null) {
//			throw new IllegalArgumentException("connn == null");
//		}
//		
//		conn = c;
//		connected = false;
//		
//		String host_string = conn.host() + " ("
//				+ (conn.pubPort() == 0? "no pub" : "pub = " + conn.pubPort())
//				+ ", "
//				+ (conn.subPort() == 0? "no sub" : "sub = " + conn.subPort())
//				+ ")";
//		
//		connectedString = "Connected to " + host_string + ".";
//		disconnectedString = "Connecting to " + host_string + "...";
//		goodIcon = IconResourceLoader.loadIcon(ConnectionStateComponent.class,
//				"circle-green-16.png");
//		badIcon = IconResourceLoader.loadIcon(ConnectionStateComponent.class,
//				"circle-red-16.png");
//		if (goodIcon == null || badIcon == null) {
//			throw new RuntimeException("Missing resource icon.");
//		}
//		
//		textLabel = new JLabel(disconnectedString);
//		iconLabel = new JLabel(badIcon);
//		
//		setLayout(new BorderLayout());
//		add(textLabel, BorderLayout.CENTER);
//		add(iconLabel, BorderLayout.WEST);
//		
//		new Thread() {
//			{ setDaemon(true); }
//			@Override
//			public void run() {
//				while(true) {
//					try {
//						Thread.sleep(250);
//					
//						final boolean myconn = conn.isConnected();
//						if (myconn != connected) {
//							EventQueue.invokeAndWait(new Runnable() {
//								@Override
//								public void run() {
//									connected = myconn;
//									textLabel.setText(connected?
//											connectedString
//											: disconnectedString);
//									iconLabel.setIcon(connected?
//											goodIcon
//											: badIcon);
//								}
//							});
//						}
//					} catch (InterruptedException e) {
//						/*
//						 * We'll ignore.
//						 */
//					} catch (InvocationTargetException e) {
//						/*
//						 * This is weird.
//						 */
//						LOG.error(e);
//					}
//				}
//			}
//		}.start();
//	}
//}
