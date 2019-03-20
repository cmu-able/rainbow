package org.sa.rainbow.gui.arch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.sa.rainbow.gui.widgets.TimeSeriesPanel;

public class DynamicDesktopIconUI extends BasicDesktopIconUI {
	
	private JComponent series;

	public DynamicDesktopIconUI(JComponent panel) {
		super();
		this.series = panel;
		
	}
	
	@Override
	protected void installComponents() {
		frame = desktopIcon.getInternalFrame();
		String title = frame.getTitle();
		desktopIcon.setBorder(null);
		desktopIcon.setOpaque(false);
		desktopIcon.setLayout(new BorderLayout());
		desktopIcon.add(series, BorderLayout.CENTER);
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), 8));

		desktopIcon.add(label, BorderLayout.SOUTH);
	}
	
	@Override
	protected void uninstallComponents() {
		desktopIcon.setLayout(null);
		desktopIcon.removeAll();
		frame = null;
	}
	
	public java.awt.Dimension getMinimumSize(javax.swing.JComponent c) {
		LayoutManager layout = desktopIcon.getLayout();
		Dimension size = layout.minimumLayoutSize(desktopIcon);
		
		return new Dimension(size.width, size.height);
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
