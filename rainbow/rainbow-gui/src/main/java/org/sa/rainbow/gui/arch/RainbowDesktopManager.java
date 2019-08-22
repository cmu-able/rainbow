package org.sa.rainbow.gui.arch;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;

import org.hibernate.id.CompositeNestedGeneratedValueGenerator.GenerationContextLocator;
import org.sa.rainbow.gui.arch.controller.AbstractRainbowController;


public class RainbowDesktopManager extends DefaultDesktopManager {

	Set<JInternalFrame> m_iconed = new HashSet<>();
	private JDesktopPane m_desktop;
	
	public RainbowDesktopManager(JDesktopPane desktop) {
		super();
		m_desktop = desktop;
	}
	
	@Override
	public void iconifyFrame(JInternalFrame frame) {
		JDesktopPane p = frame.getDesktopPane();
		JDesktopIcon icon = frame.getDesktopIcon();
		if (p != null && p.getSelectedFrame() == frame) {
			p.setSelectedFrame(null);
		}
		else {
			try {
				frame.setSelected(false);
			} catch (PropertyVetoException e) {
			}
		}
		
		Container c = frame.getParent();
		
//		if (!wasIcon(frame)) {
			Rectangle r = getBoundsForIconOf(frame);
			icon.setBounds(r);
//			setWasIcon(frame, Boolean.TRUE);
//		}
		
		if (c != null) {
			if (icon != null) {
				c.add(icon);
				icon.setVisible(true);
			}
			Rectangle b = frame.getBounds();
			c.remove(frame);
			c.repaint(b.x, b.y, b.width, b.height);
			m_iconed.add(frame);
			try { frame.setIcon(true);} catch (PropertyVetoException e) {}
			m_desktop.repaint();
		}
	}
	
	@Override
	public void deiconifyFrame(JInternalFrame f) {
		super.deiconifyFrame(f);
		m_iconed.remove(f);
		try {f.setIcon(false);} catch (PropertyVetoException e) {}
		
		Rectangle r = getBoundsForFrameOf(f);
		f.setBounds(r);

		m_desktop.repaint();
	}
	
	public boolean isIcon(JInternalFrame f) {
		return m_iconed.contains(f);
	}
	
	@Override
	protected Rectangle getBoundsForIconOf(JInternalFrame frame) {
		JDesktopPane desktopPane = frame.getDesktopPane();
		if (desktopPane == null)
			return frame.getDesktopIcon().getBounds();
		Rectangle b = frame.getBounds();
		Dimension pref = frame.getDesktopIcon().getPreferredSize();
		
		Rectangle ideal = new Rectangle((int )Math.round(b.getCenterX()-pref.getWidth()/2),
										(int )Math.round(b.getCenterY()-pref.getWidth()/2),
										pref.width, pref.height);
		return ideal;
		
	}
	
	protected Rectangle getBoundsForFrameOf(JInternalFrame frame) {
		JDesktopPane desktopPane = frame.getDesktopPane();
		if (desktopPane == null)
			return frame.getBounds();
		Rectangle b = frame.getDesktopIcon().getBounds();
		Dimension size = frame.getSize();
		
		Rectangle ideal = new Rectangle((int )Math.round(b.getCenterX()-size.getWidth()/2),
										(int )Math.round(b.getCenterY() - size.getHeight()/2),
										size.width, size.height);
		return ideal;
	}
	
	@Override
	public void endDraggingFrame(JComponent f) {
		super.endDraggingFrame(f);
		Object ctrl = f.getClientProperty("controller");
		if (ctrl instanceof AbstractRainbowController) {
			AbstractRainbowController controller = (AbstractRainbowController) ctrl;
			controller.move(f.getLocation(), true);
			
		}
	}
	
	@Override
	public void dragFrame(JComponent f, int newX, int newY) {
		f.setLocation(newX, newY);
		m_desktop.repaint();
	}

}
