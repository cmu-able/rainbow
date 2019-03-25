package org.sa.rainbow.gui.arch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.sa.rainbow.gui.RainbowWindoe;

public class RainbowDesktopIconUI extends BasicDesktopIconUI implements IErrorDisplay {
	private final Icon icon;
	private JLabel m_errorIcon;

	public RainbowDesktopIconUI(Icon icon) {
		this.icon = icon;
	}
	

	@Override
	protected void installComponents() {
		frame = desktopIcon.getInternalFrame();
		String title = frame.getTitle();
		desktopIcon.setBorder(null);
		desktopIcon.setOpaque(false);
		desktopIcon.setLayout(new BorderLayout(0,0));
		JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), 8));

		JLayeredPane layerPane = new JLayeredPane();
//		layerPane.setLayout(new BorderLayout(0,0));
		m_errorIcon = new JLabel(RainbowWindoe.ERROR_ICON);
		desktopIcon.add(layerPane, BorderLayout.CENTER);
		layerPane.add(m_errorIcon, 1);
		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout(0,0));
		layerPane.add(contents, 0);
		
//		m_errorIcon.setVisible(false);
	
		contents.add(label);
		contents.setOpaque(false);
		contents.setLocation(0, 0);
		m_errorIcon.setLocation(0, 0);
		desktopIcon.add(layerPane, BorderLayout.CENTER);
		layerPane.setMinimumSize(label.getMinimumSize());
		layerPane.setPreferredSize(label.getPreferredSize());
		contents.setBounds(0, 0, label.getMinimumSize().width, label.getMinimumSize().height);
		m_errorIcon.setBounds(0, 0, m_errorIcon.getMinimumSize().width, m_errorIcon.getMinimumSize().height);
		
//		desktopIcon.add(layerPane);
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

	@Override
	public void displayError(String message) {
		m_errorIcon.setVisible(true);
		m_errorIcon.setToolTipText(message);
	}


	@Override
	public void clearError() {
		m_errorIcon.setVisible(false);
	}
}