package org.sa.rainbow.gui.arch.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.InternalFrameUI;

import org.sa.rainbow.gui.RainbowWindoe;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.IErrorDisplay;
import org.sa.rainbow.gui.arch.RainbowDesktopIconUI;
import org.sa.rainbow.gui.arch.model.RainbowArchProbeModel;

public class RainbowProbeController extends AbstractRainbowController {

	private JTextArea m_reportArea;

	public RainbowProbeController(RainbowArchProbeModel model, SelectionManager m) {
		super(m);
		setModel(model);
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		String pid = shortName(getModel().getId());
		m_frame = new JInternalFrame(pid);
		m_frame.setFrameIcon(new ImageIcon(this.getClass().getResource("/probe.png"), pid));
		m_frame.setResizable(true);
		m_frame.setClosable(false);
		m_frame.setIconifiable(true);
		m_frame.setToolTipText(getModel().getId());
		m_reportArea = new JTextArea();

		JScrollPane sp = new JScrollPane();
		sp.setViewportView(m_reportArea);
		m_frame.add(sp, BorderLayout.CENTER);
		parent.add(m_frame);
		m_frame.getDesktopIcon().setUI(new RainbowDesktopIconUI(m_frame.getFrameIcon()));
		parent.getDesktopManager().iconifyFrame(m_frame);
		m_frame.setVisible(true);
		return m_frame;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case RainbowArchProbeModel.ERROR_PROPERTY:
			InternalFrameUI f = m_frame.getUI();
			if (f instanceof IErrorDisplay) {
				IErrorDisplay e = (IErrorDisplay) f;
				e.displayError((String) evt.getNewValue());
			}
			break;
		case RainbowArchProbeModel.PROBE_REPORT_PROPERTY:
			m_reportArea.append((String) evt.getNewValue());
			highlightActivity();
			break;
		}

	}

	@Override
	public JComponent getView() {
		return getVisibleFrame();
	}

	@Override
	protected Color[] highlightColors() {
		return new Color[] { RainbowWindoe.EFFECTORS_COLOR, RainbowWindoe.SYSTEM_COLOR_LIGHT };
	}

}
