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
import org.sa.rainbow.gui.arch.model.RainbowArchAdapationManagerModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public class RainbowAdaptationManagerController extends AbstractRainbowController {

	private final Map<String, Object> m_uidb;
	private JComponent m_uiComp;

	public RainbowAdaptationManagerController(RainbowArchAdapationManagerModel model, SelectionManager selectionManager,
			Map<String, Object> uidb) {
		super(selectionManager);
		m_uidb = uidb;
		setModel(model);
	}
	
	@Override
	public RainbowArchAdapationManagerModel getModel() {
		return (RainbowArchAdapationManagerModel) super.getModel();
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		String clazz = (String) ((Map<Object,Object>) m_uidb.get("managers")).get(getModel().getAdaptationManager().getClass().getName());
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
		Dimension pf = m_uiComp.getPreferredSize();
		frame.setSize(pf.width, pf.height+25);
		m_frame = frame;
		parent.add(frame);
		attachControllerToFrame(m_frame);

		return m_frame;
	}
	
		protected String getCustomClass(Object a, String key) {
		Map<Object, Object> map = (Map<Object,Object>) m_uidb.get(key);
		String clazz = null;
		if (map != null) {
			clazz = (String) map.get(a.getClass().getName());
		}
		return clazz;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

	@Override
	protected Color[] highlightColors() {
		return new Color[] {RainbowWindoe.ADAPTION_MANAGER_COLOR,RainbowWindoe.ADAPTION_MANAGER_COLOR_LIGHT};
	}

	public void processReport(ReportType type, String message) {
		if (m_uiComp instanceof IUIReporter) {
			((IUIReporter )m_uiComp).processReport(type, message);
		}
	}

}
