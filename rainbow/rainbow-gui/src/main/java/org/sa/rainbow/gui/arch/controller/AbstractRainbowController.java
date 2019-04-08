package org.sa.rainbow.gui.arch.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.ArchEffectorPanel;
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public abstract class AbstractRainbowController implements IRainbowUIController, PropertyChangeListener {

	protected class HighlightBorderActivity {
		TimerTask currentTask;
		Border preBorder;
		private JComponent m_component;

		public HighlightBorderActivity(JComponent component) {
			m_component = component;

		}

		public void run() {
			if (currentTask != null)
				currentTask.cancel();
			else
				preBorder = m_component.getBorder();
			JComponent vFrame = getVisibleFrame();
			currentTask = new TimerTask() {

				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							unhighlight(vFrame);
							currentTask = null;
							preBorder = null;
						}

					});
				}
			};
			highlight(vFrame);
			HIGHLIGHT_TIMER.schedule(currentTask, 1000);
		}

		protected void unhighlight(JComponent vFrame) {
			vFrame.setBorder(preBorder);
		}
		
		protected void highlight(JComponent vFrame) {
			vFrame.setBorder(new LineBorder(highlightColors()[0], 2));
		}
	};

	private RainbowArchModelElement m_model;
	private SelectionManager m_selectionManager;
	protected JInternalFrame m_frame;

	protected static final Timer HIGHLIGHT_TIMER = new Timer();
	protected HighlightBorderActivity m_highlightAct;

	protected AbstractRainbowController(SelectionManager m) {
		m_selectionManager = m;
	}

	protected String shortName(String gaugeID) {
		return gaugeID.split("@")[0].split(":")[0];
	}

	protected void attachControllerToFrame(JInternalFrame frame) {
		frame.addPropertyChangeListener(e -> {
			if ("selection".equals(e.getPropertyName())) {
				m_selectionManager.selectionChanged(this.getModel());
			}
		});
	}

	@Override
	public RainbowArchModelElement getModel() {
		return m_model;
	}

	protected void setupDiagram(JInternalFrame frame, ArchEffectorPanel uiComp) {
		frame.add(uiComp);
		Dimension s = uiComp.getPreferredSize();
		frame.setSize(s.width, s.height + 25);
	}

	@Override
	public void setModel(RainbowArchModelElement model) {
		m_model = model;
		model.setController(this);
	}

	public void move(Point2D point, boolean isUserSet) {
		Point realPoint = new Point((int) Math.round(point.getX()), (int) Math.round(point.getY()));
		if (!isUserSet) { // let's assume that the point is the center
			Dimension size = m_frame.getSize();
			new Point((int) Math.round(point.getX() - size.getWidth() / 2),
					(int) Math.round(point.getY() - size.getHeight() / 2));
		}
		m_frame.setLocation(realPoint);
		if (isUserSet) {
			m_model.setUserLocation(realPoint);
		}
	}

	@Override
	public JComponent getView() {
		return m_frame.isIcon() ? m_frame.getDesktopIcon() : m_frame;
	}

	protected JComponent getVisibleFrame() {
		JComponent visibleGFrame = m_frame;
		if (!visibleGFrame.isVisible() || m_frame.isIcon()
				|| (m_frame.getDesktopPane().getDesktopManager() instanceof RainbowDesktopManager
						&& ((RainbowDesktopManager) m_frame.getDesktopPane().getDesktopManager()).isIcon(m_frame))) {
			visibleGFrame = m_frame.getDesktopIcon();
		}
		return visibleGFrame;
	}

	protected void highlightActivity() {
		if (m_highlightAct == null) m_highlightAct = new HighlightBorderActivity(m_frame);
		m_highlightAct.run();
		
	}

	protected abstract Color[] highlightColors();

}
