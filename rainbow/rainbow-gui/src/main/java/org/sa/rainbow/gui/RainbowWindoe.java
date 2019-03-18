package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.sa.rainbow.core.IDisposable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.arch.ArchGuagePanel;
import org.sa.rainbow.gui.arch.GaugeInfo;
import org.sa.rainbow.gui.arch.RainbowDesktopIconUI;
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.sa.rainbow.util.Util;

public class RainbowWindoe extends RainbowWindow
		implements IRainbowGUI, IDisposable, IRainbowReportingSubscriberCallback {

	public class ComponentReseter implements Runnable {

		private int m_delay;
		private Runnable m_task;

		public ComponentReseter(int delay, Runnable task) {
			m_delay = delay;
			m_task = task;
		}

		@Override
		public void run() {
			final java.util.Timer t = new Timer();
			t.schedule(new TimerTask() {

				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							m_task.run();
						}
					});
				}
			}, m_delay);
		}

	}

	final static int CENTER = 0, WEST = 1, NW = 3, NORTH = 2, NE = 6, EAST = 4, SE = 12, SOUTH = 8, SW = 9;

	private static final int WIDTH = 1280;
	private static final int HEIGHT = 900;
	private static final Rectangle PROBE_REGION = new Rectangle((int) (WIDTH * 1 / 3f), (int) (HEIGHT - HEIGHT / 4f),
			(int) (2 / 3f * WIDTH), (int) (HEIGHT / 4f));
	private static final Rectangle GAUGE_REGION = new Rectangle((int) (WIDTH * 1 / 3f),
			(int) (HEIGHT - 2 * HEIGHT / 4f), (int) (2 / 3f * WIDTH), (int) (HEIGHT / 4f));

	public class ProbeInfo {
		JInternalFrame frame;
		ProbeAttributes description;
		List<String> reports;
		boolean hasError;
		List<String> gauges = new LinkedList<>();
	}

	Map<String, ProbeInfo> m_probes = new HashMap<>();
	Map<String, GaugeInfo> m_gauges = new HashMap<>();
	Map<String, JInternalFrame> m_models = new HashMap<>();

	private Map<String, JTextArea> m_probeTextAreas = new HashMap<>();

	public RainbowWindoe(IMasterCommandPort master) {
		super(master);
	}

	public RainbowWindoe() {
		super();
	}

	@Override
	protected void createDesktopPane() {
		m_desktopPane = new JDesktopPane() {
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);

//				g.drawRect(GAUGE_REGION.x, GAUGE_REGION.y, GAUGE_REGION.width, GAUGE_REGION.height);
//				g.drawRect(PROBE_REGION.x, PROBE_REGION.y, PROBE_REGION.width, PROBE_REGION.height);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				drawConnections(g2, this);
			}
		};
		m_desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		m_desktopPane.setDesktopManager(new RainbowDesktopManager(m_desktopPane));

		m_frame.getContentPane().add(m_desktopPane, BorderLayout.CENTER);
	}

	@Override
	protected void createProbesUI() {
	}

	@Override
	protected void createGaugesUI() {
	}

	@Override
	protected void createMasterUI(List<String> expectedDelegateLocations) {
		super.createMasterUI(expectedDelegateLocations);
		m_masterFrame.setVisible(false);
		// Update status pane to be in menu
		JPanel statusPane = m_oracleMessagePane.getStatusPane();
		statusPane.getParent().remove(statusPane);
		((FlowLayout) statusPane.getLayout()).setAlignment(FlowLayout.RIGHT);
		statusPane.setBorder(null);
		statusPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		statusPane.setBackground(m_menuBar.getBackground());
		m_menuBar.add(statusPane);

	}

	Map<String, Object> toMap(List<TypedAttributeWithValue> tavs) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		for (TypedAttributeWithValue tav : tavs) {
			m.put(tav.getName(), tav.getValue());
		}
		return m;
	}

	protected void drawConnections(Graphics2D g2, JDesktopPane jDesktopPane) {
		for (Map.Entry<String, GaugeInfo> entry : m_gauges.entrySet()) {
			GaugeInfo gInfo = entry.getValue();
			Component visibleGFrame = getVisibleFrame(gInfo.getFrame());
			Rectangle gBounds = visibleGFrame.getBounds();
//			int x1 = (int) Math.round(gBounds.getCenterX());
//			int y1 = (int) Math.round(gBounds.getCenterY());
			if (gInfo.getProbes() != null) {
				for (String p : gInfo.getProbes()) {
					ProbeInfo pInfo = m_probes.get(p);
					Component visiblePFrame = getVisibleFrame(pInfo.frame);
					Rectangle pBounds = visiblePFrame.getBounds();
//					int x2 = (int) Math.round(pBounds.getCenterX());
//					int y2 = (int) Math.round(pBounds.getCenterY());
					Point p1 = findClosestCornerNS(gBounds, pBounds);
					Point p2 = findClosestCornerNS(pBounds, gBounds);
					g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
				}

			}
		}
	}

	private Point findClosestCornerNS(Rectangle r1, Rectangle r2) {
		Point p = new Point();
		int outcode = r1.outcode(r2.getCenterX(), r2.getCenterY());
		switch (outcode) {
		case NORTH:
		case NW:
		case NE:
			p.x = r1.x + r1.width / 2;
			p.y = r1.y;
			break;
//		case NW:
//			p.x = r1.x;
//			p.y = r1.y;
//			break;
		case WEST:
			p.x = r1.x;
			p.y = r1.y + r1.height / 2;
			break;
//		case SW:
//			p.x = r1.x;
//			p.y = r1.y + r1.height;
//			break;
		case SOUTH:
		case SW:
		case SE:
			p.x = r1.x + r1.width / 2;
			p.y = r1.y + r1.height;
			break;
//		case SE:
//			p.x = r1.x + r1.width;
//			p.y = r1.y + r1.height;
//			break;
		case EAST:
			p.x = r1.x + r1.width;
			p.y = r1.y + r1.height / 2;
			break;
//		case NE:
//			p.x = r1.x + r1.width;
//			p.y = r1.y;
//			break;
		default /* CENTER */:
			System.out.println("outcode = CENTER");
		}
		return p;

	}

	private JComponent getVisibleFrame(JInternalFrame frame) {
		JComponent visibleGFrame = frame;
		if (!visibleGFrame.isVisible() || frame.isIcon()
				|| (m_desktopPane.getDesktopManager() instanceof RainbowDesktopManager
						&& ((RainbowDesktopManager) m_desktopPane.getDesktopManager()).isIcon(frame))) {
			visibleGFrame = frame.getDesktopIcon();
		}
		return visibleGFrame;
	}

	private JComponent getVIsibleComponentToHiglight(JInternalFrame frame) {
		JComponent visibleGFrame = frame;
		if (!visibleGFrame.isVisible() || frame.isIcon()
				|| (m_desktopPane.getDesktopManager() instanceof RainbowDesktopManager
						&& ((RainbowDesktopManager) m_desktopPane.getDesktopManager()).isIcon(frame))) {
			visibleGFrame = (JComponent) (frame.getDesktopIcon()).getComponent(0);
		}
		return visibleGFrame;
	}

	private void processProbeIntoGauge(String gaugeKey, GaugeInfo gInfo, Map<String, Object> setupParams, String tpt) {
		Pair<String, String> probe = Util.decomposeID(tpt);
		if (probe.secondValue() == null) {
			probe.setSecondValue((String) setupParams.get("targetIP"));
			String pid = Util.genID(probe.firstValue(), probe.secondValue());
			gInfo.getProbes().add(pid);
			m_probes.get(pid).gauges.add(gaugeKey);
		} else if (IGauge.ALL_LOCATIONS.equals(probe.secondValue())) {
			Set<String> keySet = m_probes.keySet();
			for (String probeId : keySet) {
				Pair<String, String> candidate = Util.decomposeID(tpt);
				if (candidate.firstValue().equals(probe.firstValue())) {
					gInfo.getProbes().add(probeId);
					m_probes.get(probeId).gauges.add(gaugeKey);
				}

			}
		} else {
			gInfo.getProbes().add(tpt);
			m_probes.get(tpt).gauges.add(gaugeKey);
		}
	}

	protected void layoutArchitecture() {

		try {
			for (Map.Entry<String, GaugeInfo> entry : m_gauges.entrySet()) {
				GaugeInfo gInfo = entry.getValue();
				if (gInfo.getProbes() == null) {
					Map<String, Object> configParams = toMap(gInfo.getDescription().configParams());
					Map<String, Object> setupParams = toMap(gInfo.getDescription().setupParams());

					entry.getValue().setProbes(new LinkedList<>());

					if (configParams.get("targetProbeType") instanceof String) {
						String tpt = (String) configParams.get("targetProbeType");
						processProbeIntoGauge(entry.getKey(), gInfo, setupParams, tpt);
					}
					if (configParams.get("targetProbeList") instanceof String) {
						String probeIds = (String) configParams.get("targetProbeList");
						for (String probeId : probeIds.split(",")) {
							probeId = probeId.trim();
							processProbeIntoGauge(entry.getKey(), gInfo, setupParams, probeId);
						}

					}

				}

			}

			layoutGaugeProbeLevels();

		} finally {
		}
	}

	private void layoutGaugeProbeLevels() {
		ArrayList<String> processedProbes = new ArrayList<>(m_probes.size());
		ArrayList<String> processedGauges = new ArrayList<>();
		for (Entry<String, GaugeInfo> ge : m_gauges.entrySet()) {
			for (String pid : ge.getValue().getProbes()) {
				if (processedProbes.contains(pid) && !m_probes.get(pid).gauges.isEmpty()) {
					String nextToGauge = m_probes.get(pid).gauges.iterator().next();
					int gi = processedGauges.indexOf(nextToGauge);
					if (gi == -1) {
						processedGauges.add(ge.getKey());
					} else {
						processedGauges.add(gi + 1, ge.getKey());
						break;
					}
				}
			}
			if (!processedGauges.contains(ge.getKey())) {
				processedGauges.add(ge.getKey());
			}
			for (String pid : ge.getValue().getProbes()) {
				if (!processedProbes.contains(pid))
					processedProbes.add(pid);
			}
		}
		for (String pid : m_probes.keySet()) {
			if (!processedProbes.contains(pid))
				processedProbes.add(pid);
		}
		int gidx = 0;
		int gaugeStep = Math.round(GAUGE_REGION.width / (float) processedGauges.size());
		for (String gid : processedGauges) {
			JDesktopIcon frameToPosition = m_gauges.get(gid).getFrame().getDesktopIcon();
			frameToPosition.setLocation(
					GAUGE_REGION.x + gidx * gaugeStep + (gaugeStep - frameToPosition.getWidth()) / 2,
					GAUGE_REGION.y + GAUGE_REGION.height / 2 - frameToPosition.getHeight() / 2);
			gidx++;
		}

		gidx = 0;
		gaugeStep = Math.round(PROBE_REGION.width / (float) processedProbes.size());
		for (String gid : processedProbes) {
			JDesktopIcon frameToPosition = m_probes.get(gid).frame.getDesktopIcon();
			frameToPosition.setLocation(
					PROBE_REGION.x + gidx * gaugeStep + (gaugeStep - frameToPosition.getWidth()) / 2,
					PROBE_REGION.y + PROBE_REGION.height / 2 - frameToPosition.getHeight() / 2);
			gidx++;
		}
	}

	protected void populateUI() {
		createProbes();
		createGauges();
		layoutArchitecture();
	}

	private void createGauges() {
		GaugeManager gaugeManager = Rainbow.instance().getRainbowMaster().gaugeManager();
		int i = 1;
		for (String g : gaugeManager.getCreatedGauges()) {
			if (m_gauges.get(g) == null) {
				GaugeInstanceDescription description = Rainbow.instance().getRainbowMaster().gaugeDesc().instSpec
						.get(g.split("@")[0].split(":")[0]);
				GaugeInfo info = new GaugeInfo();

				final JInternalFrame frame = new JInternalFrame(shortName(g), true, false, true);
				frame.setFrameIcon(new ImageIcon(this.getClass().getResource("/gauge.png"), shortName(g)));
				frame.setIconifiable(true);
				frame.setToolTipText(g);

				frame.setVisible(true);
				frame.setSize(100, 100);
				frame.setLocation(WIDTH - i * 100, HEIGHT - 340);
				i++;

				info.setFrame(frame);
				IGauge gauge = Rainbow.instance().lookupGauge(g);
				info.setDescription(description);
				info.setOperations(new HashMap<>());
				for (Pair<String, OperationRepresentation> key : description.commandSignatures()) {
					info.getOperations().put(key.secondValue().getName(), new LinkedList<>());
				}
				final ArchGuagePanel p = new ArchGuagePanel(g, info);
				p.createContent();
				Dimension preferredSize = frame.getPreferredSize();
				Dimension pSize = p.getPreferredSize();
				preferredSize.setSize(
						new Dimension(Math.max(preferredSize.width, pSize.width), preferredSize.height + pSize.height));
				frame.setSize(preferredSize);
//				JScrollPane sp = new JScrollPane();
//				sp.setViewportView(p);
				frame.add(p, BorderLayout.CENTER);
				m_desktopPane.add(frame);

				frame.getDesktopIcon().setUI(new RainbowDesktopIconUI(frame.getFrameIcon()));
				m_desktopPane.getDesktopManager().iconifyFrame(frame);

				m_gauges.put(g, info);
				p.addUpdateListener(() -> {
					final JComponent vFrame = getVIsibleComponentToHiglight(frame);
					vFrame.setBorder(new LineBorder(GAUGES_COLOR));
					p.m_table.setSelectionBackground(GAUGES_COLOR_LIGHT);
					final java.util.Timer t = new Timer();
					t.schedule(new TimerTask() {

						@Override
						public void run() {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									vFrame.setBorder(null);
								}
							});
						}
					}, 1000);
				});
			}
		}
	}

	private void createProbes() {
		try {
			m_createProbeReportingPortSubscriber = RainbowPortFactory
					.createProbeReportingPortSubscriber(new IProbeReportPort() {

						@Override
						public void dispose() {

						}

						@Override
						public void reportData(IProbeIdentifier probe, String data) {
							String pid = probe.type() + "@" + probe.location();
							ProbeInfo probeInfo = m_probes.get(pid);
							final JComponent vc = getVIsibleComponentToHiglight(probeInfo.frame);
							vc.setBorder(new LineBorder(SYSTEM_COLOR_LIGHT, 2));
							m_probeSections.get(pid).setText(data);
							ComponentReseter tcc = new ComponentReseter(1000, () -> vc.setBorder(null));
							tcc.run();
						}
					});
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ProbeDescription probes = Rainbow.instance().getRainbowMaster().probeDesc();
		int i = 1;
		for (ProbeAttributes probe : probes.probes) {
			String probeId = probe.alias + "@" + probe.getLocation();
			if (m_probes.get(probeId) == null) {
				JInternalFrame frame = addProbeFrame(probeId);
				frame.setSize(100, 100);
				frame.setLocation(WIDTH - i * 100, HEIGHT - 140);
				i++;

				m_desktopPane.getDesktopManager().iconifyFrame(frame);
				frame.setVisible(true);
				ProbeInfo info = new ProbeInfo();
				info.description = probe;
				info.frame = frame;
				info.reports = new LinkedList<>();
				info.hasError = false;

				m_probes.put(probeId, info);
			}
			m_createProbeReportingPortSubscriber.subscribeToProbe(probe.alias, probe.getLocation());

		}
	}

	private JInternalFrame addProbeFrame(String probeId) {
		JInternalFrame frame = new JInternalFrame(shortName(probeId));
		frame.setFrameIcon(new ImageIcon(this.getClass().getResource("/probe.png"), shortName(probeId)));
		frame.setResizable(true);
		frame.setClosable(false);
		frame.setIconifiable(true);
		frame.setToolTipText(probeId);
		JTextArea p = new JTextArea();
		m_probeSections.put(probeId, p);
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(p);
		frame.add(sp, BorderLayout.CENTER);
		m_desktopPane.add(frame);
		frame.getDesktopIcon().setUI(new RainbowDesktopIconUI(frame.getFrameIcon()));
		return frame;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
//					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					RainbowWindoe window = new RainbowWindoe(null);
					JInternalFrame frame = window.addProbeFrame("Test");
					frame.setSize(100, 100);
					frame.setLocation(100, 100);
					frame.setVisible(true);
					window.m_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
