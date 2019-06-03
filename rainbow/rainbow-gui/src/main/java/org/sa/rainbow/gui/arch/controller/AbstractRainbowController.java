package org.sa.rainbow.gui.arch.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.ArchEffectorPanel;
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public abstract class AbstractRainbowController implements IRainbowUIController, PropertyChangeListener {

	static class IconFrameSelectedBorder extends AbstractBorder implements UIResource {
		private static final int corner = 14;
		
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color background = MetalLookAndFeel.getPrimaryControlDarkShadow();
			Color highlight = MetalLookAndFeel.getPrimaryControlShadow();
			Color shadow = MetalLookAndFeel.getPrimaryControlInfo();
			
            g.setColor(background);
            // Draw outermost lines
            g.drawLine( 1, 0, w-2, 0);
            g.drawLine( 0, 1, 0, h-2);
            g.drawLine( w-1, 1, w-1, h-2);
            g.drawLine( 1, h-1, w-2, h-1);

            // Draw the bulk of the border
            for (int i = 1; i < 5; i++) {
                g.drawRect(x+i,y+i,w-(i*2)-1, h-(i*2)-1);
            }
			
		}
		
		@Override
		public Insets getBorderInsets(Component c, Insets newInsets) {
			newInsets.set(5, 5, 5, 5);
			return newInsets;
		}
		
	}
	
	
	protected class HighlightBorderActivity {
		TimerTask currentTask;
		Border preBorder;
		private JComponent m_component;

		public HighlightBorderActivity(JComponent component) {
			m_component = component;

		}

		public void run() {
			JComponent vFrame = getVisibleFrame();
			if (currentTask != null)
				currentTask.cancel();
			else
				preBorder = vFrame.getBorder();
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
			synchronized (this) {
				vFrame.setBorder(preBorder);
			}
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
			if ("selected".equals(e.getPropertyName())) {
				Boolean selected = (Boolean) e.getNewValue();
				m_selectionManager.selectionChanged(this.getModel(), selected);
				JComponent vf = getVisibleFrame();
				if (vf instanceof JDesktopIcon) {
					if (selected) {
						if (m_highlightAct == null) {
							vf.setBorder(getBorder());
							vf.setSize(vf.getWidth() + 10, vf.getHeight() + 10);
						}
						else
							synchronized (m_highlightAct) {
								if (m_highlightAct.currentTask != null) {
									m_highlightAct.preBorder = getBorder();
									vf.setSize(vf.getWidth() + 10, vf.getHeight() + 10);
								} else {
									vf.setSize(vf.getWidth() + 10, vf.getHeight() + 10);
									vf.setBorder(getBorder());
								}
							}
					} else {
						if (m_highlightAct == null) {
							vf.setBorder(null);
							vf.setSize(vf.getWidth() - 10, vf.getHeight() - 10);

						}
						else
							synchronized (m_highlightAct) {
								if (m_highlightAct.currentTask != null) {
									m_highlightAct.preBorder = null;
									vf.setSize(vf.getWidth() - 10, vf.getHeight() - 10);
								} else {
									vf.setBorder(null);
									vf.setSize(vf.getWidth() - 10, vf.getHeight() - 10);
								}
							}
					}
				}

			} else if ("userdragged".equals(e.getPropertyName())) {
				Point loc = (Point) e.getNewValue();
				move(new Point2D.Double(loc.x, loc.y), true);
			}
		});
		getVisibleFrame().putClientProperty("controller", this);
	}

	protected Border getBorder() {
		return new IconFrameSelectedBorder();
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
			Dimension size = getVisibleFrame().getSize();
			realPoint = new Point((int) Math.round(point.getX() - size.getWidth() / 2),
					(int) Math.round(point.getY() - size.getHeight() / 2));
		}
		getVisibleFrame().setLocation(realPoint);
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
		if (m_highlightAct == null)
			m_highlightAct = new HighlightBorderActivity(m_frame);
		m_highlightAct.run();

	}

	protected abstract Color[] highlightColors();

}
