package incubator.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextProvider;
import incubator.ctxaction.CompositeActionContext;
import incubator.pval.Ensure;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Desktop pane that maintains a context with the copy of the context of
 * the selected internal window, as well as keeping a menu with all
 * internal windows.
 */
public class DesktopPane extends JDesktopPane
		implements ActionContextProvider {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Execution context.
	 */
	private CompositeActionContext m_context;
	
	/**
	 * Current action context.
	 */
	private ActionContext m_current;
	
	/**
	 * Windows whose names are in the menu (in the order by which they are
	 * in the menu).
	 */
	private List<JInternalFrame> m_frames;
	
	/**
	 * Listener added to the internal frames.
	 */
	private ComponentListener m_component_listener;
	
	/**
	 * Listener added to the internal frames.
	 */
	private InternalFrameListener m_internal_listener;
	
	/**
	 * Listener added to the internal frames.
	 */
	private PropertyChangeListener m_property_listener;
	
	/**
	 * Menu window the names of the open windows.
	 */
	private JMenu m_menu;
	
	/**
	 * Creates a new pane.
	 * @param menu an optional menu where, in the end, window names will
	 * be added. Items can be added to the menu at any time as long as they
	 * are not added to the end or in the middle of window names.
	 */
	public DesktopPane(JMenu menu) {
		m_context = new CompositeActionContext();
		addContainerListener(new ContainerListener() {
			@Override
			public void componentAdded(ContainerEvent e) {
				added(e.getChild());
			}

			@Override
			public void componentRemoved(ContainerEvent e) {
				removed(e.getChild());
			}
		});
		
		m_component_listener = new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
				hidden(e.getComponent());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void componentResized(ComponentEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void componentShown(ComponentEvent e) {
				shown(e.getComponent());
			}
			
		};
		
		m_internal_listener = new InternalFrameListener() {
			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
				rebuild_context();
			}

			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void internalFrameDeactivated(InternalFrameEvent e) {
				rebuild_context();
			}

			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void internalFrameIconified(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
		};
		
		m_property_listener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("title")
						&& (evt.getSource() instanceof JInternalFrame)) {
					renamed((JInternalFrame) evt.getSource(),
							(String) evt.getOldValue());
				}
			}
		};
		
		m_frames = new ArrayList<>();
		this.m_menu = menu;
		setBackground(Color.WHITE);
	}
	
	@Override
	public ActionContext getActionContext() {
		return m_context;
	}
	
	/**
	 * Rebuilds the execution context.
	 */
	private void rebuild_context() {
		ActionContext current = null;
		
		JInternalFrame selectedFrame = getSelectedFrame();
		if (selectedFrame != null &&
				selectedFrame instanceof ActionContextProvider) {
			current = ((ActionContextProvider) selectedFrame).getActionContext();
		}
		
		if (current == this.m_current) {
			return;
		}
		
		if (this.m_current != null) {
			m_context.removeActionContext(this.m_current);
		}
		
		this.m_current = current;
		
		if (this.m_current != null) {
			m_context.addActionContext(this.m_current);
		}
	}
	
	/**
	 * A component has been added to the desktop pane.
	 * @param c the component added
	 */
	private void added(Component c) {
		if (!(c instanceof JInternalFrame)) {
			return;
		}
		
		JInternalFrame frame = (JInternalFrame) c;
		frame.addInternalFrameListener(m_internal_listener);
		frame.addComponentListener(m_component_listener);
		frame.addPropertyChangeListener(m_property_listener);
		
		if (frame.isVisible()) {
			add_to_menu(frame, frame.getTitle());
		}
	}
	
	/**
	 * A component has been removed from the desktop pane.
	 * @param c the component removed
	 */
	private void removed(Component c) {
		if (!(c instanceof JInternalFrame)) {
			return;
		}
		
		JInternalFrame frame = (JInternalFrame) c;
		frame.removeComponentListener(m_component_listener);
		frame.removeInternalFrameListener(m_internal_listener);
		frame.removePropertyChangeListener(m_property_listener);
		remove_from_menu(frame, frame.getTitle());
	}
	
	/**
	 * A frame has been renamed.
	 * @param frame the frame
	 * @param old_name the frame's old name
	 */
	private void renamed(JInternalFrame frame, String old_name) {
		remove_from_menu(frame, old_name);
		add_to_menu(frame, frame.getTitle());
	}
	
	/**
	 * A component has been hidden.
	 * @param c the hidden component
	 */
	private void hidden(Component c) {
		if (!(c instanceof JInternalFrame)) {
			return;
		}
		
		JInternalFrame frame = (JInternalFrame) c;
		remove_from_menu(frame, frame.getTitle());
	}
	
	/**
	 * A component became visible.
	 * @param c the component
	 */
	private void shown(Component c) {
		if (!(c instanceof JInternalFrame)) {
			return;
		}
		
		JInternalFrame frame = (JInternalFrame) c;
		add_to_menu(frame, frame.getTitle());
	}
	
	/**
	 * Adds a new window to the list, if it is not already there.
	 * @param frame the window to add
	 * @param wname the window name
	 */
	private void add_to_menu(JInternalFrame frame, String wname) {
		if (m_frames.contains(frame)) {
			return;
		}
		
		int menuPos = 0;
		for (Iterator<JInternalFrame> names_it = m_frames.iterator();
				names_it.hasNext(); menuPos++) {
			JInternalFrame f = names_it.next();
			String name = f.getTitle();
			if (name.compareTo(wname) > 0) {
				break;
			}
		}
		
		m_frames.add(menuPos, frame);
		
		if (m_menu != null) {
			menuPos += m_menu.getItemCount() - m_frames.size() + 1;
			FrameActivationActionListener menuListener;
			menuListener = new FrameActivationActionListener(frame);
			JMenuItem mitem = new JMenuItem(wname);
			mitem.addActionListener(menuListener);
			m_menu.add(mitem, menuPos);
		}
	}
	
	/**
	 * Removes a window from the list.
	 * @param frame the window
	 * @param wname the window name
	 */
	private void remove_from_menu(JInternalFrame frame, String wname) {
		int pos = 0;
		for (Iterator<JInternalFrame> it = m_frames.iterator();
				it.hasNext(); pos++) {
			if (frame == it.next()) {
				break;
			}
		}
		
		/*
		 * It is ok if we have no idea of what frame it is.
		 */
		if (pos == m_frames.size()) {
			return;
		}
		
		assert pos >= 0 && pos < m_frames.size();
		
		int menuPos = pos;
		if (m_menu != null) {
			menuPos += m_menu.getItemCount() - m_frames.size();
			m_menu.remove(menuPos);
		}
		
		m_frames.remove(pos);
	}
	
	
	/**
	 * Class representing an action listener connected to the window
	 * menu that activates the windows when the user selected the menu
	 * item.
	 */
	private class FrameActivationActionListener implements ActionListener {
		/**
		 * The window
		 */
		private JInternalFrame m_frame;
		
		/**
		 * Creates a new listener for the given frame.
		 * @param frame the frame
		 */
		private FrameActivationActionListener(JInternalFrame frame) {
			Ensure.not_null(frame);
			m_frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				m_frame.setSelected(true);
			} catch (PropertyVetoException ex) {
				/*
				 * Weird. Anyway, nothing we can do about it. The user will
				 * find this weird :)
				 */
			}
		}
	}
}
