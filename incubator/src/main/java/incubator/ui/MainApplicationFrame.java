package incubator.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.CompositeActionContext;
import incubator.pval.Ensure;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

/**
 * Application main window.
 */
public class MainApplicationFrame extends JFrame {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Execution context.
	 */
	private CompositeActionContext m_action_context;
	
	/**
	 * Pane with the windows.
	 */
	private DesktopPane m_desktop_pane;
	
	/**
	 * Menu bar.
	 */
	private JMenuBar m_menu_bar;
	
	/**
	 * Creates the application's main window.
	 * @param title the window title
	 * @param icon_resource the resource with the application icon (optional)
	 */
	public MainApplicationFrame(String title, String icon_resource) {
		super(title);
		
		if (icon_resource != null) {
			initTray(title, icon_resource);
		}
		
		init(icon_resource);
	}
	
	/**
	 * Initializes the tray icon.
	 * @param title the application title
	 * @param icon_resource the resource with the application icon
	 */
	private void initTray(String title, String icon_resource) {
		assert title != null;
		assert icon_resource != null;
		
		/*
		 * We start to load the image.
		 */
		URL iconUrl = getClass().getResource(icon_resource);
		if (iconUrl == null) {
			throw new RuntimeException("Icon resource '" + icon_resource
					+ "' not found.");
		}
		
		Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
		
		SystemTray systemTray = SystemTray.getSystemTray();
		TrayIcon trayIcon = new TrayIcon(img);
		
		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			throw new RuntimeException("Failed to add icon to system tray.", e);
		}
			
		
		trayIcon.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isVisible()) {
					setVisible(false);
				} else {
					setVisible(true);
					setExtendedState(getExtendedState() & ~ICONIFIED);
					toFront();
				}
			}
			@Override public void mouseEntered(MouseEvent e) { /* */ }
			@Override public void mouseExited(MouseEvent e) { /* */ }
			@Override public void mousePressed(MouseEvent e) { /* */ }
			@Override public void mouseReleased(MouseEvent e) { /* */ }
		});
	}
	
	/**
	 * Adds a new window.
	 * @param frame the frame
	 */
	public void add_frame(JInternalFrame frame) {
		Ensure.not_null(frame);
		m_desktop_pane.add(frame);
	}
	
	/**
	 * Initializes the application.
	 * @param icon_resource resource with the application icon, which
	 * may be <code>null</code>
	 */
	private void init(String icon_resource) {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		m_menu_bar = new JMenuBar();
		setJMenuBar(m_menu_bar);
		
		JMenu fileMenu = new JMenu("File");
		m_menu_bar.add(fileMenu);
		JMenu windowMenu = new JMenu("Window");
		m_menu_bar.add(windowMenu);
		
		m_action_context = new CompositeActionContext();
		
		QuitAction qa = new QuitAction();
		qa.bind(m_action_context);
		fileMenu.add(qa.createJMenuItem(false));
		
		getContentPane().setLayout(new BorderLayout());
		m_desktop_pane = new DesktopPane(windowMenu);
		m_action_context.addActionContext(m_desktop_pane.getActionContext());
		getContentPane().add(m_desktop_pane, BorderLayout.CENTER);
		
		if (icon_resource != null) {
			URL iconResourceUrl = getClass().getResource(icon_resource);
			if (iconResourceUrl != null) {
				Image img = Toolkit.getDefaultToolkit().createImage(
						iconResourceUrl);
				setIconImage(img);
			}
		}
		
		addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e) { /* */ }
			@Override
			public void windowClosed(WindowEvent e) { /* */ }
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
			@Override
			public void windowDeactivated(WindowEvent e) { /* */ }
			@Override
			public void windowDeiconified(WindowEvent e) { /* */ }
			@Override
			public void windowIconified(WindowEvent e) {
				setVisible(false);
			}
			@Override
			public void windowOpened(WindowEvent e) { /* */ }
		});
	}
	
	/**
	 * Shuts down the application.
	 */
	private void quit() {
		System.exit(0);
	}
	
	/**
	 * Obtains a copy of the file menu.
	 * @return the file menu
	 */
	protected JMenu get_file_menu() {
		for (int i = 0; i < m_menu_bar.getMenuCount(); i++) {
			JMenu m = m_menu_bar.getMenu(i);
			if (m.getText().equals("File")) {
				return m;
			}
		}
		
		return null;
	}
	
	/**
	 * Adds a menu to the menu bar, after an existing menu.
	 * @param menu the menu to add
	 * @param previous the name of the menu after which the menu should be
	 * added
	 */
	protected void add_menu_after(JMenu menu, String previous) {
		for (int i = 0; i < m_menu_bar.getMenuCount(); i++) {
			JMenu m = m_menu_bar.getMenu(i);
			if (m.getText().equals(previous)) {
				m_menu_bar.add(menu, i + 1);
				return;
			}
		}
	}
	
	/**
	 * Obtains the action context.
	 * @return the action context
	 */
	protected ActionContext get_action_context() {
		return m_action_context;
	}
}
