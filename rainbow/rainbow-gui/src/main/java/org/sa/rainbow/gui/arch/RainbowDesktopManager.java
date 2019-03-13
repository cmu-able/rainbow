package org.sa.rainbow.gui.arch;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;

import javax.swing.DefaultDesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;


public class RainbowDesktopManager extends DefaultDesktopManager {

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
		}
	}
	
	@Override
	protected Rectangle getBoundsForIconOf(JInternalFrame frame) {
		JDesktopPane desktopPane = frame.getDesktopPane();
		if (desktopPane == null)
			return frame.getDesktopIcon().getBounds();
		Rectangle b = frame.getBounds();
		Dimension pref = frame.getDesktopIcon().getPreferredSize();
		
		Rectangle ideal = new Rectangle((int )Math.round(b.getCenterX()-pref.getWidth()/2),
										(int )Math.round(b.getCenterY()/*-pref.getWidth()/2*/),
										pref.width, pref.height);
		return ideal;
		
	}

}
