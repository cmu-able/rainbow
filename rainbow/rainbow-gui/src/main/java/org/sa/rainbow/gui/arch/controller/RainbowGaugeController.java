package org.sa.rainbow.gui.arch.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.gui.RainbowWindoe;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.ArchGuagePanel;
import org.sa.rainbow.gui.arch.model.RainbowArchGaugeModel;

public class RainbowGaugeController extends AbstractRainbowController {

	private Map<String, Object> m_uidb;
	private ArchGuagePanel m_gaugePanel;

	public RainbowGaugeController(RainbowArchGaugeModel model, SelectionManager m, Map<String, Object> uidb) {
		super(m);
		m_uidb = uidb;
		setModel(model);
	}
	
	@Override
	public RainbowArchGaugeModel getModel() {
		return (RainbowArchGaugeModel )super.getModel();
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		final JInternalFrame frame = new JInternalFrame(shortName(getModel().getId()), true, false, true);
		frame.setFrameIcon(new ImageIcon(this.getClass().getResource("/gauge.png"), shortName(getModel().getId())));
		frame.setIconifiable(true);
		frame.setToolTipText(getModel().getId());

		frame.setVisible(true);
		
		m_gaugePanel = new ArchGuagePanel(getModel().getId(), getModel(), frame);
		m_gaugePanel.createContent();
		Dimension preferredSize = frame.getPreferredSize();
		Dimension pSize = m_gaugePanel.getPreferredSize();
		preferredSize.setSize(
				new Dimension(Math.max(preferredSize.width, pSize.width), preferredSize.height + pSize.height));
		frame.setSize(preferredSize);
		frame.add(m_gaugePanel, BorderLayout.CENTER);
		parent.add(frame);

		frame.getDesktopIcon()
				.setUI(m_gaugePanel.createIcon(frame, (Map<String, Object>) (m_uidb != null ? m_uidb.get("gauges")
						: Collections.<String, Object>emptyMap())));
		parent.getDesktopManager().iconifyFrame(frame);
		m_frame = frame;
		attachControllerToFrame(m_frame);
		return m_frame;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			switch (evt.getPropertyName()) {
			case RainbowArchGaugeModel.GAUGEREPORT:
				m_gaugePanel.requestModelUpdate((IRainbowOperation )evt.getNewValue());
				highlightActivity();
				break;
			case RainbowArchGaugeModel.GAUGEREPORTS:
				m_gaugePanel.requestModelUpdate((List<IRainbowOperation> )evt.getNewValue(), true);
				highlightActivity();
				break;
			}
		} catch (IllegalStateException | RainbowException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void highlightActivity() {
		if (m_highlightAct == null) m_highlightAct = new HighlightBorderActivity(m_frame) {
			@Override
			protected void highlight(JComponent vFrame) {
				super.highlight(vFrame);
				m_gaugePanel.m_table.setSelectionBackground(RainbowWindoe.GAUGES_COLOR_LIGHT);
			}
			
			@Override
			protected void unhighlight(JComponent vFrame) {
				super.unhighlight(vFrame);
				m_gaugePanel.m_table.clearSelection();
			}
		};
		m_highlightAct.run();
	}

	@Override
	protected Color[] highlightColors() {
		return new Color[] {RainbowWindoe.GAUGES_COLOR, RainbowWindoe.GAUGES_COLOR_LIGHT};
	}

}
