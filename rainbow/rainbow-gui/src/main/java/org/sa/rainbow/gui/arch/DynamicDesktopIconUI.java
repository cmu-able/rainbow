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
		m_errorIcon = new JLabel(RainbowWindoe.ERROR_ICON);
		desktopIcon.add(layerPane, BorderLayout.CENTER);
		layerPane.add(m_errorIcon, 0);
//		m_errorIcon.setVisible(false);
		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout(0,0));
		contents.setOpaque(false);
		layerPane.add(contents, 1);
		contents./*desktopIcon.*//*getInternalFrame().*/add(series, BorderLayout.CENTER);

		contents/*desktopIcon*//*.getInternalFrame()*/.add(label, BorderLayout.SOUTH);
		desktopIcon.add(layerPane);
		
		layerPane.setMinimumSize(contents.getMinimumSize());
		layerPane.setPreferredSize(contents.getPreferredSize());
		contents.setBounds(0,0,contents.getMinimumSize().width,contents.getMinimumSize().height);
		m_errorIcon.setBounds(0, 0, m_errorIcon.getMinimumSize().width, m_errorIcon.getMinimumSize().height);
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
		m_errorIcon.setVisible(true);
		m_errorIcon.setToolTipText(message);
		desktopIcon.invalidate();

	}
	
	@Override
	public void clearError() {
		m_errorIcon.setVisible(false);
	}
}
