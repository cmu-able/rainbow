package org.sa.rainbow.gui.arch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.sa.rainbow.gui.RainbowWindoe;

public class DynamicDesktopIconUI extends BasicDesktopIconUI implements IErrorDisplay {
	
	private JComponent series;
	private MouseInputListener m_createMouseInputListener;
	private JPanel m_errorPane;
	private JLabel m_errorIcon;
	
	

	public DynamicDesktopIconUI(JComponent panel) {
		super();
		this.series = panel;
		
	}
	
	@Override
	protected void installComponents() {
		frame = desktopIcon.getInternalFrame();
//		frame.setGlassPane(glass);
		String title = frame.getTitle();
		desktopIcon.setBorder(null);
		desktopIcon.setOpaque(false);
		desktopIcon./*getInternalFrame().*/setLayout(new BorderLayout());
		String labelText = String.format("<html><div style=\"width:%dpx;text-align: center;\">%s</div><html>", Math.max(50,series.getMinimumSize().width), title);
		JLabel label = new JLabel(labelText, SwingConstants.CENTER);
		label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), 8));

		// Create component with layer
		JLayeredPane layerPane = new JLayeredPane();
		m_errorPane = new JPanel();
		m_errorPane.setOpaque(false);
		m_errorPane.setLayout(new BorderLayout(0,0));
		m_errorIcon = new JLabel(RainbowWindoe.ERROR_ICON);
		m_errorPane.add(m_errorIcon, BorderLayout.WEST);
		desktopIcon.add(layerPane, BorderLayout.CENTER);
		layerPane.add(m_errorPane, 1);
		m_errorPane.setVisible(false);
		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout(0,0));
		layerPane.add(contents, 0);
		contents./*desktopIcon.*//*getInternalFrame().*/add(series, BorderLayout.CENTER);

		contents/*desktopIcon*//*.getInternalFrame()*/.add(label, BorderLayout.SOUTH);
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
	
	@Override
	protected void installListeners() {
		super.installListeners();
		m_createMouseInputListener = createMouseInputListener();
		series.addMouseListener(m_createMouseInputListener);
		series.addMouseMotionListener(m_createMouseInputListener);
	}
	
	@Override
	protected void uninstallListeners() {
		super.uninstallListeners();
		series.removeMouseListener(m_createMouseInputListener);
		series.removeMouseMotionListener(m_createMouseInputListener);
		m_createMouseInputListener = null;
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
		m_errorPane.setVisible(true);
	}
	
	@Override
	public void clearError() {
		m_errorPane.setVisible(false);
	}
}
