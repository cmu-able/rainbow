package org.sa.rainbow.gui.arch.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindoe;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.ArchModelPanel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;

public class RainbowModelController extends AbstractRainbowController {

	protected ArchModelPanel m_mp;

	public RainbowModelController(RainbowArchModelModel model, SelectionManager m) {
		super(m);
		setModel(model);
	}
	
	@Override
	public RainbowArchModelModel getModel() {
		return (RainbowArchModelModel) super.getModel();
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		JInternalFrame frame = new JInternalFrame(getModel().getModelRef().getModelName(), true, false, true);
		frame.setVisible(true);
		m_mp = new ArchModelPanel(getModel().getModelRef());
		frame.add(m_mp);
		Dimension s = m_mp.getPreferredSize();
		frame.setSize(s.width, s.height + 25);
		m_frame = frame;
		parent.add(frame);
		attachControllerToFrame(frame);
		return m_frame;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case RainbowArchModelModel.OPERATION_PROP:
			m_mp.addOperation((IRainbowOperation )evt.getNewValue(), false, false);
			highlightActivity();
			break;
		case RainbowArchModelModel.OPERATION__ERROR_PROP:
			m_mp.addOperation((IRainbowOperation )evt.getNewValue(), true, false);
			m_frame.setFrameIcon(RainbowWindoe.ERROR_ICON);
			highlightActivity();
			break;
		}
	}

	@Override
	protected Color[] highlightColors() {
		return new Color[] {RainbowWindoe.MODELS_MANAGER_COLOR, RainbowWindoe.MODELS_MANAGER_COLOR_LIGHT};
	}

	public void processReport(ReportType type, String message) {
		m_mp.processReport(type, message);
	}

}
