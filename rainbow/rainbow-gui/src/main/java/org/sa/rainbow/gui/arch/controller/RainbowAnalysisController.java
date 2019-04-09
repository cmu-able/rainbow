package org.sa.rainbow.gui.arch.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindoe;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.RainbowWindow;
import org.sa.rainbow.gui.arch.elements.ArchConsolePanel;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.arch.model.RainbowArchAnalysisModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public class RainbowAnalysisController extends AbstractRainbowController {

	private Map<String, Object> m_uidb;
	private JComponent m_uiComp;

	public RainbowAnalysisController(RainbowArchAnalysisModel model, SelectionManager selectionManager,
			Map<String, Object> uidb) {
		super(selectionManager);
		m_uidb = uidb;
		setModel(model);
	}
	
	@Override
	public RainbowArchAnalysisModel getModel() {
		return (RainbowArchAnalysisModel) super.getModel();
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		IRainbowAnalysis a = getModel().getAnalysis();
		String clazz = (String) m_uidb.get(a.getClass().getName());
		m_uiComp = null;
		if (clazz != null) {
			try {
				Class<? extends JComponent> uiClass = (Class<? extends JComponent>) this.getClass().getClassLoader()
						.loadClass(clazz);
				m_uiComp = uiClass.newInstance();

			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			}

		}
		if (m_uiComp == null) {
			m_uiComp = new ArchConsolePanel();
		}
		JInternalFrame frame = new JInternalFrame(a.id(), true, false, true);
		frame.setVisible(true);
		frame.add(m_uiComp);
		Dimension s = m_uiComp.getPreferredSize();
		frame.setSize(s.width, s.height+25);
		m_frame = frame;
		parent.add(m_frame);
		attachControllerToFrame(m_frame);
		return m_frame;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
	}

	@Override
	protected Color[] highlightColors() {
		return new Color[] {RainbowWindoe.ANALYZERS_COLOR, RainbowWindow.ANALYZERS_COLOR_LIGHT};
	}

	public void processReport(ReportType type, String message) {
		if (m_uiComp instanceof IUIReporter) {
			((IUIReporter )m_uiComp).processReport(type, message);
		}
	}

}
