package incubator.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Utilities to frames.
 */
public class FrameUtils {
	/**
	 * Preference suffix for the window location, X coordinate.
	 */
	private static final String LOC_X_KEY_SUF = "::x";
	
	/**
	 * Preference suffix for window location, Y coordinate.
	 */
	private static final String LOC_Y_KEY_SUF = "::y";
	
	/**
	 * Preference suffix for window width.
	 */
	private static final String SIZE_W_KEY_SUF = "::w";
	
	/**
	 * Preference suffix for window height.
	 */
	private static final String SIZE_H_KEY_SUF = "::h";
	
	/**
	 * Preference suffix to state whether the window is visible or not.
	 */
	private static final String VISIBLE_SUF = "::vis";
	
	/**
	 * Preference suffix to show the window state.
	 */
	private static final String STATE_SUF = "::state";
	
	/**
	 * Centers a window in the screen.
	 * @param f the window to center
	 */
	public static void center(Window f) {
		if (f == null) {
			throw new IllegalArgumentException("f == null");
		}
		
		int width = f.getWidth();
		int height = f.getHeight();
		
		Dimension ssize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int x = (ssize.width - width) / 2;
		int y = (ssize.height - height) / 2;
		
		f.setLocation(x, y);
	}
	
	/**
	 * Centers a window in another window.
	 * @param f the window to center
	 * @param parent the window to which <code>f</code> should be centered
	 */
	public static void center(Window f, Window parent) {
		if (f == null) {
			throw new IllegalArgumentException("f == null");
		}
		
		if (parent == null) {
			throw new IllegalArgumentException("parent == null");
		}
		
		int width = f.getWidth();
		int height = f.getHeight();
		
		Dimension ssize = parent.getSize();
		Point ploc = parent.getLocation();
		
		int x = ploc.x + (ssize.width - width) / 2;
		int y = ploc.y + (ssize.height - height) / 2;
		
		f.setLocation(x, y);
	}
	
	/**
	 * Centers a window in the window that contains a component.
	 * @param f the window to center
	 * @param c the component used for centering
	 */
	public static void center(Window f, Component c) {
		if (f == null) {
			throw new IllegalArgumentException("f == null");
		}
		
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}
		
		Container container = c.getParent();
		for (; container.getParent() != null;
				container = container.getParent())
			;
		
		if (container instanceof Window) {
			center(f, (Window) container);
		}
	}
	
	/**
	 * Saves the window positioning in the preferences. This method should
	 * be used to save the position and size of the window such that, the
	 * next time a program starts, it will reopen the window in the same
	 * position and size. Information is stored in the user's preferences.
	 * @param w the window
	 * @param name the window's unique name
	 */
	public static void save_positioning(Component w, String name) {
		if (w == null) {
			throw new IllegalArgumentException("w == null");
		}
		
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
		Point wloc = w.getLocation();
		Dimension wdim = w.getSize();
		
		Preferences userPrefs;
		userPrefs = Preferences.userNodeForPackage(FrameUtils.class);
		userPrefs.putInt(name + LOC_X_KEY_SUF, wloc.x);
		userPrefs.putInt(name + LOC_Y_KEY_SUF, wloc.y);
		userPrefs.putInt(name + SIZE_H_KEY_SUF, wdim.height);
		userPrefs.putInt(name + SIZE_W_KEY_SUF, wdim.width);
		userPrefs.putBoolean(name + VISIBLE_SUF, w.isVisible());
		
		if (w instanceof Frame) {
			userPrefs.putInt(name + STATE_SUF, ((Frame) w).getExtendedState());
		}
		
		try {
			userPrefs.sync();
		} catch (BackingStoreException e) {
			/*
			 * We ignore because this isn't important.
			 */
		}
	}
	
	/**
	 * Loads the positioning of the window from the preferences. This method
	 * should be used with {@link #save_positioning(Component, String)}.
	 * @param w the window
	 * @param name the window's unique name
	 */
	public static void load_positioning(Component w, String name) {
		if (w == null) {
			throw new IllegalArgumentException("w == null");
		}
		
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
		Preferences userPrefs;
		userPrefs = Preferences.userNodeForPackage(FrameUtils.class);
		if (userPrefs.get(name + LOC_X_KEY_SUF, null) != null
				&& userPrefs.get(name + LOC_Y_KEY_SUF, null) != null) {
			Point wloc = new Point();
			wloc.x = userPrefs.getInt(name + LOC_X_KEY_SUF, 0);
			wloc.y = userPrefs.getInt(name + LOC_Y_KEY_SUF, 0);
			w.setLocation(wloc);
		}
		
		if (userPrefs.get(name + SIZE_W_KEY_SUF, null) != null
				&& userPrefs.get(name + SIZE_H_KEY_SUF, null) != null) {
			Dimension wsize = new Dimension();
			wsize.height = userPrefs.getInt(name + SIZE_H_KEY_SUF, 0);
			wsize.width = userPrefs.getInt(name + SIZE_W_KEY_SUF, 0);
			w.setSize(wsize);
		}
		
		if (userPrefs.get(name + VISIBLE_SUF, null) != null) {
			w.setVisible(userPrefs.getBoolean(name + VISIBLE_SUF, true));
		}
		
		if (w instanceof Frame
				&& userPrefs.get(name + STATE_SUF,  null) != null) {
			((Frame) w).setExtendedState(userPrefs.getInt(name + STATE_SUF,
					Frame.NORMAL));
		}
	}
	
	/**
	 * Loads a window positioning and keeps the information about its state
	 * and location automatically updated. Invoking this method is equivalent
	 * to invoke {@link #load_positioning(Component, String)} and then
	 * register a listener that will invoke
	 * {@link #save_positioning(Component, String)} every time the window
	 * changes.
	 * @param w the window
	 * @param name a unique name for the window
	 */
	public static void trackWindowPositioning(final Component w,
			final String name) {
		if (w == null) {
			throw new IllegalArgumentException("w == null");
		}
		
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
		load_positioning(w, name);
		if (w instanceof Window) {
			((Window) w).addWindowListener(new WindowListener() {
				@Override
				public void windowActivated(WindowEvent e) { /* */ }
				@Override
				public void windowClosed(WindowEvent e) {
					save_positioning(w, name);
				}
	
				@Override
				public void windowClosing(WindowEvent e) { /* */ }
				@Override
				public void windowDeactivated(WindowEvent e) { /* */ }
	
				@Override
				public void windowDeiconified(WindowEvent e) {
					save_positioning(w, name);
				}
	
				@Override
				public void windowIconified(WindowEvent e) {
					save_positioning(w, name);
				}
	
				@Override
				public void windowOpened(WindowEvent e) {
					save_positioning(w, name);
				}
			});
		}
		
		if (w instanceof JInternalFrame) {
			((JInternalFrame) w).addInternalFrameListener(
					new InternalFrameListener() {
				@Override
				public void internalFrameActivated(InternalFrameEvent e)
						{ /* */ }

				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					save_positioning(w, name);
				}

				@Override
				public void internalFrameClosing(InternalFrameEvent e)
					{ /* */ }
				@Override
				public void internalFrameDeactivated(InternalFrameEvent e)
					{ /* */ }

				@Override
				public void internalFrameDeiconified(InternalFrameEvent e) {
						save_positioning(w, name);
				}

				@Override
				public void internalFrameIconified(InternalFrameEvent e) {
					save_positioning(w, name);
				}

				@Override
				public void internalFrameOpened(InternalFrameEvent e) {
					save_positioning(w, name);
				}
			});
		}
		
		w.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				save_positioning(w, name);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				save_positioning(w, name);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				save_positioning(w, name);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				save_positioning(w, name);
			}
		});
	}
}
