package org.sa.rainbow.gui.arch.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindoe;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.elements.ArchConsolePanel;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public class RainbowExecutorController extends AbstractRainbowController {

	private Map<String, Object> m_uidb;
	private JComponent m_uiComp;

	public RainbowExecutorController(RainbowArchExecutorModel model, SelectionManager selectionManager,
			Map<String, Object> uidb) {
		super(selectionManager);
		m_uidb = uidb;
		setModel(model);
	}
	
	@Override
	public RainbowArchExecutorModel getModel() {
		return (RainbowArchExecutorModel) super.getModel();
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		String clazz = (String) ((Map<Object,Object>) m_uidb.get("executors")).get(getModel().getExecutor().getClass().getName());
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
		JInternalFrame frame = new JInternalFrame(getModel().getId(), true, false, true);
		frame.setVisible(true);
		frame.add(m_uiComp);
		Dimension s = m_uiComp.getPreferredSize();
		frame.setSize(s.width, s.height+25);
		attachControllerToFrame(frame);
		parent.add(frame);
		m_frame = frame;
		return m_frame;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Color[] highlightColors() {
		return new Color[] {RainbowWindoe.EXECUTORS_COLOR, RainbowWindoe.EXECUTORS_COLOR_LIGHT};
	}

	public void processReport(ReportType type, String message) {
		if (m_uiComp instanceof IUIReporter) 
			((IUIReporter )m_uiComp).processReport(type, message);
	}

}
