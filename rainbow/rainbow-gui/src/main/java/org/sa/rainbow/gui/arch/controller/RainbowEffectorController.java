package org.sa.rainbow.gui.arch.controller;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.sa.rainbow.gui.RainbowWindoe;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.ArchEffectorPanel;
import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel.EffectorExecutions;

public class RainbowEffectorController extends AbstractRainbowController  {

	private ArchEffectorPanel m_effectorUI;
	public RainbowEffectorController(RainbowArchEffectorModel model, SelectionManager m) {
		super(m);
		setModel(model);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case RainbowArchEffectorModel.EFFECTOR_EXECUTED: 
			EffectorExecutions ex = (EffectorExecutions) evt.getNewValue();
			m_effectorUI.reportExecuted(null, ex.outcome, ex.args);
			highlightActivity();
			break;
		case RainbowArchEffectorModel.EFFECTOR_EXECUTING:
			ex = (EffectorExecutions) evt.getNewValue();
			m_effectorUI.reportExecuting(null, ex.args);
			highlightActivity();
		}
	}
	
	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		String effectorId = getModel().getId();
		m_frame = new JInternalFrame(shortName(effectorId), true, false, true);
		m_frame.setVisible(true);
		m_effectorUI = new ArchEffectorPanel();
		setupDiagram(m_frame, m_effectorUI);
		parent.add(m_frame);
		attachControllerToFrame(m_frame);
		return m_frame;
	

	}
	
//	@Override
//	protected void highlightActivity() {
//		if (m_highlightAct == null) m_highlightAct = new HighlightBorderActivity(m_frame) {
//			@Override
//			protected void highlight(JComponent vFrame) {
//				super.highlight(vFrame);
//				m_effectorUI.m_table
//			}
//		}
//	}
	
	@Override
	protected Color[] highlightColors() {
		return new Color[] {RainbowWindoe.EFFECTORS_COLOR, RainbowWindoe.SYSTEM_COLOR_LIGHT};
	}

}
