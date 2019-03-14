package org.sa.rainbow.gui.arch;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicDesktopIconUI;

public class SimpleDesktopIconUI extends BasicDesktopIconUI {
	private final Icon icon;

	public SimpleDesktopIconUI(Icon icon) {
		this.icon = icon;
	}

	@Override
	protected void installComponents() {
		frame = desktopIcon.getInternalFrame();
		String title = frame.getTitle();

		JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), (int) Math.round(label.getFont().getStyle()*0.8)));

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
}