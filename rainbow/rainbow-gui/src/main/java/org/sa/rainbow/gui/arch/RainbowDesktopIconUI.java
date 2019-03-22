package org.sa.rainbow.gui.arch;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicDesktopIconUI;
import javax.swing.plaf.basic.BasicDesktopIconUI.MouseInputHandler;

import org.sa.rainbow.gui.arch.DynamicDesktopIconUI.SelectionMouseInputHandler;

public class RainbowDesktopIconUI extends BasicDesktopIconUI {
	private final Icon icon;

	public RainbowDesktopIconUI(Icon icon) {
		this.icon = icon;
	}
	

	@Override
	protected void installComponents() {
		frame = desktopIcon.getInternalFrame();
		String title = frame.getTitle();

		JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), 8));

		desktopIcon.setBorder(null);
		desktopIcon.setOpaque(false);
		desktopIcon.setLayout(new GridLayout(1, 1));
		desktopIcon.add(label);
	}

	@Override
	protected void uninstallComponents() {
		desktopIcon.setLayout(null);
		desktopIcon.removeAll();
		frame = null;
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {

		LayoutManager layout = desktopIcon.getLayout();
		Dimension size = layout.minimumLayoutSize(desktopIcon);
		return new Dimension(size.width + 15, size.height + 15);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		return getMinimumSize(c);
	}

	@Override
	public Dimension getMaximumSize(JComponent c) {
		return getMinimumSize(c);
	}

	
	@Override
	protected MouseInputListener createMouseInputListener() {
		return new SelectionMouseInputHandler();
	}
	
	class SelectionMouseInputHandler extends MouseInputHandler {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			if (e.getClickCount() == 1) {
				frame.firePropertyChange("selection", 0, 1);
			}
		}
	}
}