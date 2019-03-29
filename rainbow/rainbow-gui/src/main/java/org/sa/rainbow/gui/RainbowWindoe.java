package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.plaf.DesktopIconUI;
import javax.swing.plaf.InternalFrameUI;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSourceDOT;
import org.ho.yaml.Yaml;
import org.sa.rainbow.core.IDisposable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.arch.ArchEffectorPanel;
import org.sa.rainbow.gui.arch.ArchGuagePanel;
import org.sa.rainbow.gui.arch.ArchModelPanel;
import org.sa.rainbow.gui.arch.GaugeInfo;
import org.sa.rainbow.gui.arch.IErrorDisplay;
import org.sa.rainbow.gui.arch.RainbowDesktopIconUI;
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.gui.arch.elements.ArchConsolePanel;
import org.sa.rainbow.gui.arch.elements.GaugeDetailPanel;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.arch.elements.IUIUpdater;
import org.sa.rainbow.gui.arch.elements.ProbeTabbedPane;
import org.sa.rainbow.gui.widgets.DesktopScrollPane;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
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

	public static final ImageIcon ERROR_ICON = new ImageIcon(RainbowWindoe.class.getResource("/error.png"));

	public class ProbeInfo {
		JInternalFrame frame;
		public ProbeAttributes description;
		public List<String> reports;
		public boolean hasError;
		List<String> gauges = new LinkedList<>();
	}

	public class ModelInfo {
		ModelReference modelRef;
		JInternalFrame frame;
		List<String> gauges = new LinkedList<>();
		public ArchModelPanel panel;
	}

	public static class SelectionManager {
		public interface ISelectionListener {
			public void selectionChanged(Object o);
		}

		Collection<ISelectionListener> m_listeners = new HashSet<>();

		public void addSelectionListener(ISelectionListener l) {
			m_listeners.add(l);
		}

		public void removeSelectionListener(ISelectionListener l) {
			m_listeners.remove(l);
		}

		public void selectionChanged(Object o) {
			for (ISelectionListener l : m_listeners) {
				SwingUtilities.invokeLater(() -> l.selectionChanged(o));
			}
		}

	}

	SelectionManager m_selectionManager = new SelectionManager();

	Map<String, ProbeInfo> m_probes = new HashMap<>();
	Map<String, GaugeInfo> m_gauges = new HashMap<>();
	Map<String, ModelInfo> m_models = new HashMap<>();
	Map<String, JComponent> m_analyzers = new HashMap<>();
	Map<String, JComponent> m_adaptationManagers = new HashMap<>();
	Map<String, JComponent> m_executors = new HashMap<>();
	Map<String, ArchEffectorPanel> m_effectors = new HashMap<>();

	private Map<String, JTextArea> m_probeTextAreas = new HashMap<>();

	private Map<String, Object> m_uidb;

	private ArrayList<java.awt.geom.Line2D> m_lines;

	private JPanel m_rootPane;

	private JTabbedPane m_selectionPanel;

	private JTabbedPane m_logTabs;

	private JPanel m_detailsPanel;

	private JTextArea m_errorArea;

	private ProbeTabbedPane m_probePanel;

	private GaugeDetailPanel m_gaugePanel;

	private JLabel m_statusWindow;

	private Timer m_uiTimer = new Timer();
	
	public RainbowWindoe(IMasterCommandPort master) {
		super(master);
		init();
	}

	public RainbowWindoe() {
		super();
		init();
	}

	private void init() {
		File specs = Util.getRelativeToPath(Rainbow.instance().getTargetPath(),
				Rainbow.instance().getProperty("rainbow.gui.specs"));
		if (specs != null) {
			try {
				m_uidb = (Map<String, Object>) Yaml.load(specs);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void createDesktopPane() {
		m_rootPane = new JPanel();
		m_rootPane.setLayout(new BorderLayout());

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

//		m_frame.getContentPane().add(m_desktopPane, BorderLayout.CENTER);

		DesktopScrollPane dsp = new DesktopScrollPane(m_desktopPane);
		m_frame.getContentPane().add(dsp, BorderLayout.CENTER);

		m_selectionPanel = new JTabbedPane(JTabbedPane.LEFT);
		m_selectionPanel.setPreferredSize(new Dimension(WIDTH, 200));
		m_frame.getContentPane().add(m_selectionPanel, BorderLayout.SOUTH);

		m_logTabs = new JTabbedPane(JTabbedPane.BOTTOM);
		m_selectionPanel.addTab("Logs", m_logTabs);

		m_detailsPanel = new JPanel();
		m_detailsPanel.setLayout(new CardLayout());
		m_selectionPanel.addTab("Details", m_detailsPanel);

		m_errorArea = createTextAreaInTab(m_selectionPanel, "Errors");

		m_selectionManager.addSelectionListener(o -> {
			m_probePanel.setVisible(false);
			m_gaugePanel.setVisible(false);
			if (o instanceof ProbeInfo) {
				ProbeInfo probeInfo = (ProbeInfo) o;
				m_selectionPanel.setSelectedIndex(1);
				m_probePanel.setVisible(true);
				m_probePanel.setProbeInfo(probeInfo);
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "probes");
			} else if (o instanceof GaugeInfo) {
				GaugeInfo gaugeInfo = (GaugeInfo) o;
				m_selectionPanel.setSelectedIndex(1);
				m_gaugePanel.setVisible(true);
				m_gaugePanel.initDataBindings(gaugeInfo);
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "gauges");

			}
		});
		m_statusWindow = new JLabel("Waiting for Rainbow to start...");
		m_statusWindow.setFont(new Font(m_statusWindow.getFont().getFontName(), m_statusWindow.getFont().getStyle(), 18));
		m_statusWindow.setSize(m_statusWindow.getPreferredSize());
		m_statusWindow.setLocation(0, 0);
		m_statusWindow.setVisible(true);
		m_desktopPane.add(m_statusWindow);
	}

	@Override
	protected void createProbesUI() {

		m_probePanel = new ProbeTabbedPane();
		m_detailsPanel.add(m_probePanel);
		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_probePanel, "probes");
		m_probePanel.setVisible(false);

		JTextArea probeLogs = createTextAreaInTab(m_logTabs, "Probes");
		m_allTabs.put(RainbowComponentT.MASTER, probeLogs);
	}

	@Override
	protected void createGaugesUI() {

		m_gaugePanel = new GaugeDetailPanel();
		m_detailsPanel.add(m_gaugePanel);
		m_gaugePanel.setVisible(false);
		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_gaugePanel, "gauges");

		JTextArea gaugesLogs = createTextAreaInTab(m_logTabs, "Gauges");
		m_allTabs.put(RainbowComponentT.GAUGE_MANAGER, gaugesLogs);
		m_allTabs.put(RainbowComponentT.GAUGE, gaugesLogs);
	}

	@Override
	protected void createModelsManagerUI() {
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Models");
		m_allTabs.put(RainbowComponentT.MODEL, modelsLogs);
	}

	@Override
	protected void createAdaptationManagerUI() {
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Adaptation Manager");
		m_allTabs.put(RainbowComponentT.ADAPTATION_MANAGER, modelsLogs);
	}

	@Override
	protected void createExecutorsUI() {
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Execution");
		m_allTabs.put(RainbowComponentT.EXECUTOR, modelsLogs);
	}

	@Override
	protected void createAnalyzersUI() {
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Analyzers");
		m_allTabs.put(RainbowComponentT.ANALYSIS, modelsLogs);
	}

	@Override
	protected void createEffectorsUI() {
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Effectors");
		m_allTabs.put(RainbowComponentT.EFFECTOR, modelsLogs);
		m_allTabs.put(RainbowComponentT.EFFECTOR_MANAGER, modelsLogs);
	}

	@Override
	protected void createMasterUI(List<String> expectedDelegateLocations) {
		super.createMasterUI(expectedDelegateLocations);
		m_masterFrame.setVisible(false);
		// Update status pane to be in menu
		JPanel statusPane = m_oracleMessagePane.getStatusPane();
		((FlowLayout) statusPane.getLayout()).setAlignment(FlowLayout.RIGHT);
		statusPane.setBorder(null);
		statusPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		statusPane.setBackground(m_menuBar.getBackground());

		// These removals are hacsk
		statusPane.getParent().remove(statusPane);
		m_menuBar.add(statusPane);
		JTextArea managementText = m_oracleMessagePane.getTextArea();
		managementText.getParent().getParent().getParent().remove(managementText.getParent().getParent());
		m_logTabs.insertTab("Management", null, managementText.getParent().getParent(), null, 0);
		m_allTabs.put(RainbowComponentT.MASTER, managementText);
		m_logTabs.setSelectedIndex(0);

	}

	Map<String, Object> toMap(List<TypedAttributeWithValue> tavs) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		for (TypedAttributeWithValue tav : tavs) {
			m.put(tav.getName(), tav.getValue());
		}
		return m;
	}

	protected void drawConnections(Graphics2D g2, JDesktopPane jDesktopPane) {
		if (m_lines != null && !m_lines.isEmpty()) {
			for (Line2D l : m_lines) {
				g2.draw(l);
			}

			return;

		}
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
				ModelInfo mi = m_models.get(new ModelReference(gInfo.getDescription().modelDesc().getName(),
						gInfo.getDescription().modelDesc().getType()).toString());
				mi.gauges.add(entry.getKey());

			}

			if (!layoutDOT())
				layoutGaugeProbeLevels();

		} finally {
		}
	}

	private boolean layoutDOT() {
		try {
			m_lines = new ArrayList<Line2D>();
			Graph g = createGraphToLayout();
//			g.setAttribute("size", "" + toInches(Math.max(GAUGE_REGION.width, PROBE_REGION.width), res) + ","
//					+ toInches(GAUGE_REGION.height + PROBE_REGION.height, res));

			FileSinkDOT fs = new FileSinkDOT();
			File tmp = File.createTempFile("rainbow", "dot");
			File tmpo = File.createTempFile("layout", "dot");
			fs.writeAll(g, tmp.getAbsolutePath());

			Runtime rt = Runtime.getRuntime();
			String[] args = { "/usr/bin/dot", "-Tdot", /* "-Gdpi=" + res, */tmp.getAbsolutePath(), "-o",
					tmpo.getAbsolutePath() };
			Process p = rt.exec(args);
			p.waitFor();
			Graph inGraph = new DefaultGraph("input");
			FileSourceDOT in = new FileSourceDOT();
			in.addSink(inGraph);
			try {
				in.readAll(tmpo.getAbsolutePath());
			} finally {
				in.removeSink(inGraph);
			}
			Rectangle graphBB = getBoundingBox(inGraph.getAttribute("bb", String.class));
			Node node = inGraph.getNode("root");
			if (node != null) {
				double h = node.getAttribute("height");
				graphBB = adjustFromTop(graphBB, (float) h);
			}
			for (Node n : inGraph.getNodeSet()) {
				if (m_gauges.containsKey(n.getId())) {
					GaugeInfo gi = m_gauges.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0]),
							Float.parseFloat(pos.split(",")[1]), getVisibleFrame(gi.getFrame()).getBounds().getSize());
//					Point realPoint = convertOrigin(location, graphBB);
//					realPoint.translate(GAUGE_REGION.x, GAUGE_REGION.y);
					Point realPoint = location;
					getVisibleFrame(gi.getFrame()).setLocation(realPoint);
				} else if (m_probes.containsKey(n.getId())) {
					ProbeInfo pi = m_probes.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0]),
							Float.parseFloat(pos.split(",")[1]), getVisibleFrame(pi.frame).getBounds().getSize());
//					Point realPoint = convertOrigin(location, graphBB);
//					realPoint.translate(GAUGE_REGION.x, GAUGE_REGION.y);
					Point realPoint = location;
					getVisibleFrame(pi.frame).setLocation(realPoint);
				} else if (m_models.containsKey(n.getId())) {
					ModelInfo mi = m_models.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0])/* + 10*/,
							Float.parseFloat(pos.split(",")[1])/* + 10*/, getVisibleFrame(mi.frame).getBounds().getSize());
					Point realPoint = location;
					getVisibleFrame(mi.frame).setLocation(realPoint);

				} else if (m_analyzers.containsKey(n.getId())) {
					JComponent a = m_analyzers.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0])/* + 10*/,
							Float.parseFloat(pos.split(",")[1])/* + 10*/, getDesktopFram(a).getBounds().getSize());
					Point realPoint = location;
					getDesktopFram(a).setLocation(realPoint);
				} else if (m_adaptationManagers.containsKey(n.getId())) {
					JComponent a = m_adaptationManagers.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0]) /*+ 10*/,
							Float.parseFloat(pos.split(",")[1]) /*+ 10*/, getDesktopFram(a).getBounds().getSize());
					Point realPoint = location;
					getDesktopFram(a).setLocation(realPoint);
				} else if (m_executors.containsKey(n.getId())) {
					JComponent a = m_executors.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0])/* + 10*/,
							Float.parseFloat(pos.split(",")[1])/* + 10*/, getDesktopFram(a).getBounds().getSize());
					Point realPoint = location;
					getDesktopFram(a).setLocation(realPoint);
				} else if (m_effectors.containsKey(n.getId())) {
					JComponent a = m_effectors.get(n.getId());
					String pos = (String) n.getAttribute("pos");
					Point location = getTopLeft(Float.parseFloat(pos.split(",")[0])/* + 10*/,
							Float.parseFloat(pos.split(",")[1])/* + 10*/, getDesktopFram(a).getBounds().getSize());
					Point realPoint = location;
					getDesktopFram(a).setLocation(realPoint);
				}
			}

			for (Edge e : inGraph.getEdgeSet()) {
				if (!e.getId().contains("root")) {
					String posA = e.getAttribute("pos");
					String[] posS = posA.split(" ");
					Point[] locs = new Point[posS.length];
					for (int i = 0; i < posS.length; i++) {
						String pos = posS[i];
						String[] point = pos.split(",");
						Point location = new Point(Math.round(Float.parseFloat(point[0])),
								Math.round(Float.parseFloat(point[1])));
//						Point realPoint = convertOrigin(location, graphBB);
//						realPoint.translate(GAUGE_REGION.x, GAUGE_REGION.y);
						Point realPoint = location;
						locs[i] = realPoint;
						if (i >= 1) {
							m_lines.add(new Line2D.Float(locs[i - 1].x, locs[i - 1].y, locs[i].x, locs[i].y));
						}
					}

				}
			}

		} catch (HeadlessException | IdAlreadyInUseException | EdgeRejectedException | IOException
				| InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private Container getDesktopFram(Container a) {
		while (!(a.getParent() instanceof JInternalFrame) && a.getParent() != null) {
			a = a.getParent();
		}
		return a.getParent();
	}

	protected Graph createGraphToLayout() {
		int res = 80; // Toolkit.getDefaultToolkit().getScreenResolution();
		Graph g = new SingleGraph("gauges-and-probes");

		Node root = g.addNode("root");
		Map<String, Node> processedIds = new HashMap<>();

		for (Entry<String, GaugeInfo> ge : m_gauges.entrySet()) {
			GaugeInfo gaugeInfo = ge.getValue();
			Node gN = g.addNode(ge.getKey());
			Dimension size = getVisibleFrame(gaugeInfo.getFrame()).getSize();
			gN.addAttribute("width", toInches(size.width, res));
			gN.addAttribute("height", toInches(size.height, res));
			gN.addAttribute("fixedsize", true);
			gN.addAttribute("shape", "box");
			processedIds.put(ge.getKey(), gN);
			for (String probe : gaugeInfo.getProbes()) {
				ProbeInfo pi = m_probes.get(probe);
				String pid = probe;
				Node pN = processedIds.get(pid);
				if (pN == null) {
					pN = g.addNode(pid);
					size = getVisibleFrame(pi.frame).getSize();
					pN.addAttribute("width", toInches(size.width, res));
					pN.addAttribute("height", toInches(size.height, res));
					pN.addAttribute("fixedsize", true);
					pN.addAttribute("shape", "box");
					processedIds.put(pid, pN);
				}
				g.addEdge(gN.getId() + "-" + pN.getId(), gN, pN);
			}

		}
		for (Entry<String, ModelInfo> me : m_models.entrySet()) {
			ModelInfo mi = me.getValue();
			Node mN = g.addNode(me.getKey());
			Dimension size = mi.frame.getSize();
			mN.addAttribute("width", toInches(size.width /*+ 40*/, res));
			mN.addAttribute("height", toInches(size.height, res));
			mN.addAttribute("fixedsize", true);
			mN.addAttribute("shape", "box");
			for (String ga : mi.gauges) {
				if (processedIds.containsKey(ga)) {
					g.addEdge(mN.getId() + "-" + ga, mN, processedIds.get(ga));
				}
			}
			processedIds.put(me.getKey(), mN);
		}
		for (IRainbowAnalysis a : Rainbow.instance().getRainbowMaster().analyzers()) {
			JComponent comp = m_analyzers.get(a.id());
			Node aN = g.addNode(a.id());
			Dimension size = getDesktopFram(comp).getSize();
			aN.addAttribute("width", toInches(size.width /*+ 40*/, res));
			aN.addAttribute("height", toInches(size.height, res));
			aN.addAttribute("fixedsize", true);
			aN.addAttribute("shape", "box");
			for (String m : m_models.keySet()) {
				if (processedIds.containsKey(m))
					g.addEdge(aN.getId() + "-" + m, aN, processedIds.get(m));
			}

		}

		Map<String, Node> model2am = new HashMap<>();

		for (IAdaptationManager<?> am : Rainbow.instance().getRainbowMaster().adaptationManagers().values()) {
			JComponent comp = m_adaptationManagers.get(am.id());
			Node aN = g.addNode(am.id());
			Dimension size = getDesktopFram(comp).getSize();
			aN.addAttribute("width", toInches(size.width/* + 40*/, res));
			aN.addAttribute("height", toInches(size.height, res));
			aN.addAttribute("fixedsize", true);
			aN.addAttribute("shape", "box");
			String model = am.getManagedModel().toString();
			model2am.put(model, aN);
			Node n = processedIds.get(model);
			if (n != null) {
				g.addEdge(aN.getId() + "-" + n.getId(), aN, n);

			}
		}

		Set<Node> executorNodes = new HashSet<>();

		for (IAdaptationExecutor<?> ae : Rainbow.instance().getRainbowMaster().adaptationExecutors().values()) {
			JComponent comp = m_executors.get(ae.id());
			Node aN = g.addNode(ae.id());
			Dimension size = getDesktopFram(comp).getSize();
			aN.addAttribute("width", toInches(size.width/* + 40*/, res));
			aN.addAttribute("height", toInches(size.height, res));
			aN.addAttribute("fixedsize", true);
			aN.addAttribute("shape", "box");
			String model = ae.getManagedModel().toString();
			Node n = processedIds.get(model);
			if (n != null) {
				g.addEdge(aN.getId() + "-" + n.getId(), aN, n);

			}
			Node aM = model2am.get(model);
			if (aM != null) {
				g.addEdge(aM.getId() + "-" + aN.getId(), aM, aN);
			}
			executorNodes.add(aN);
		}

		Collection<Entry<String, ArchEffectorPanel>> values = m_effectors.entrySet();
		for (Entry<String, ArchEffectorPanel> entry : values) {
			Node eN = g.addNode(entry.getKey());
			Dimension size = getDesktopFram(entry.getValue()).getSize();
			eN.addAttribute("width", toInches(size.width + 40, res));
			eN.addAttribute("height", toInches(size.height, res));
			eN.addAttribute("fixedsize", true);
			eN.addAttribute("shape", "box");
			for (Node ex : executorNodes) {
				g.addEdge(ex.getId() + "-" + eN.getId(), ex, eN);
			}
		}

		g.setAttribute("splines", "compound");
		g.setAttribute("rankdir", "BT"); // Put probes and effectors at bottom
		return g;
	}

	private Point convertOrigin(Point location, Rectangle graphBB) {
		return new Point(location.x, graphBB.height - location.y);
	}

	private Rectangle adjustFromTop(Rectangle graphBB, Float attribute) {
		graphBB.setSize(graphBB.width, graphBB.height - Math.round(attribute));
		return graphBB;
	}

	private Rectangle getBoundingBox(String attribute) {
		String[] r = attribute.split(",");
		return new Rectangle(Math.round(Float.parseFloat(r[0])), Math.round(Float.parseFloat(r[1])),
				Math.round(Float.parseFloat(r[2])), Math.round(Float.parseFloat(r[3])));
	}

	private Point getTopLeft(float centerX, float centerY, Dimension size) {
		return new Point((int) Math.round(centerX - size.getWidth() / 2),
				(int) Math.round(centerY - size.getHeight() / 2));
	}

	protected float toInches(int unit, float res) {
		return unit / res;
	}

	protected int fromInches(float unit, int res) {
		return Math.round(unit);
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
		m_statusWindow.setText("Populating UI");
		createProbes();
		createModels();
		createGauges();
		createAnalyzers();
		createAdaptationManagers();
		createExecutors();
		createEffectors();
		layoutArchitecture();
		m_statusWindow.setVisible(false);
	}

	private void createEffectors() {
		try {
			m_createClientSideEffectorLifecyclePort = RainbowPortFactory
					.createClientSideEffectorLifecyclePort(new IEffectorLifecycleBusPort() {

						@Override
						public void dispose() {
							// TODO Auto-generated method stub

						}

						@Override
						public void reportExecuted(IEffectorIdentifier effector, Outcome outcome, List<String> args) {
							String eid = effector.id();
							ArchEffectorPanel ta = m_effectors.get(eid);

							if (ta != null) {
								ta.reportExecuted(effector, outcome, args);
//						ta.append(effector.id().split("@")[0] + args.toString() + " -> " + outcome.name() + "\n");
//						ta.setCaretPosition(ta.getText().length());
//						if (ta.getText().length() > MAX_TEXT_LENGTH) {
//							ta.setText(ta.getText().substring(TEXT_HALF_LENGTH));
//						}
//						TabColorChanger tcc= new TabColorChanger(m_tabs.get(RainbowComponentT.EFFECTOR), ta.getParent().getParent(), SYSTEM_COLOR_LIGHT);
//						tcc.run();
							}
						}

						@Override
						public void reportExecuting(IEffectorIdentifier effector, List<String> args) {
							String eid = effector.id();
							ArchEffectorPanel ta = m_effectors.get(eid);

							if (ta != null)
								ta.reportExecuting(effector, args);
						}

						@Override
						public void reportDeleted(IEffectorIdentifier effector) {
							// TODO Auto-generated method stub

						}

						@Override
						public void reportCreated(IEffectorIdentifier effector) {
							// TODO Auto-generated method stub

						}
					});
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		EffectorDescription effectorDesc = Rainbow.instance().getRainbowMaster().effectorDesc();
		for (EffectorAttributes ea : effectorDesc.effectors) {
			String effectorId = ea.name + "@" + ea.getLocation();
			if (m_effectors.get(effectorId) == null) {
				JInternalFrame frame = new JInternalFrame(shortName(effectorId), true, false, true);
				frame.setVisible(true);
				ArchEffectorPanel uiComp = new ArchEffectorPanel();

				frame.add(uiComp);
				Dimension s = uiComp.getPreferredSize();
				frame.setSize(s.width, s.height+25);
				frame.addPropertyChangeListener(e -> {
					System.out.println("Selected " + effectorId);
					if ("selection".equals(e.getPropertyName())) {
						m_selectionManager.selectionChanged(ea);
					}
				});
				m_desktopPane.add(frame);
				m_effectors.put(effectorId, uiComp);
			}
		}
	}

	private void createExecutors() {
		Map<String, IAdaptationExecutor<?>> executors = Rainbow.instance().getRainbowMaster().adaptationExecutors();
		Map<String, Object> aui = m_uidb.containsKey("executors") ? (Map<String, Object>) m_uidb.get("executors")
				: Collections.<String, Object>emptyMap();
		for (IAdaptationExecutor a : executors.values()) {
			String clazz = (String) aui.get(a.getClass().getName());
			JComponent uiComp = null;
			if (clazz != null) {
				try {
					Class<? extends JComponent> uiClass = (Class<? extends JComponent>) this.getClass().getClassLoader()
							.loadClass(clazz);
					uiComp = uiClass.newInstance();

				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				}
			}
			if (uiComp == null) {
				uiComp = new ArchConsolePanel();
			}
			m_executors.put(a.id(), uiComp);
			JInternalFrame frame = new JInternalFrame(a.id(), true, false, true);
			frame.setVisible(true);
			frame.add(uiComp);
			Dimension s = uiComp.getPreferredSize();
			frame.setSize(s.width, s.height+25);
			frame.addPropertyChangeListener(e -> {
				System.out.println("Selected " + a.id());
				if ("selection".equals(e.getPropertyName())) {
					m_selectionManager.selectionChanged(a);
				}
			});
			if (uiComp instanceof IUIUpdater) {
				final JComponent fComp = uiComp;
//				final java.util.Timer timer = new Timer();
				Runnable r = new Runnable() {
					TimerTask currentTask;
					
					@Override
					public void run() {
						if (currentTask != null) currentTask.cancel();
						fComp.setBorder(new LineBorder(EXECUTORS_COLOR, 2));
						m_uiTimer.schedule(new TimerTask() {

							@Override
							public void run() {
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										fComp.setBorder(null);
									}
								});
							}
						}, 1000);
					}
					
				};
				((IUIUpdater) uiComp).addUpdateListener(r);
			}
			m_desktopPane.add(frame);
		}
	}

	private void createAdaptationManagers() {
		Map<String, IAdaptationManager<?>> analyzers = Rainbow.instance().getRainbowMaster().adaptationManagers();
		Map<String, Object> aui = m_uidb.containsKey("managers") ? (Map<String, Object>) m_uidb.get("managers")
				: Collections.<String, Object>emptyMap();
		for (IAdaptationManager a : analyzers.values()) {
			String clazz = (String) aui.get(a.getClass().getName());
			JComponent uiComp = null;
			if (clazz != null) {
				try {
					Class<? extends JComponent> uiClass = (Class<? extends JComponent>) this.getClass().getClassLoader()
							.loadClass(clazz);
					uiComp = uiClass.newInstance();

				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				}
			}
			if (uiComp == null) {
				uiComp = new ArchConsolePanel();
			}
			m_adaptationManagers.put(a.id(), uiComp);
			JInternalFrame frame = new JInternalFrame(a.id(), true, false, true);
			frame.setVisible(true);
			frame.add(uiComp);
			Dimension pf = uiComp.getPreferredSize();
			frame.setSize(pf.width, pf.height+25);
			frame.addPropertyChangeListener(e -> {
				System.out.println("Selected " + a.id());
				if ("selection".equals(e.getPropertyName())) {
					m_selectionManager.selectionChanged(a);
				}
			});
			if (uiComp instanceof IUIUpdater) {
				final JComponent fComp = uiComp;
//				final java.util.Timer timer = new Timer();
				Runnable r = new Runnable() {
					TimerTask currentTask;
					
					@Override
					public void run() {
						if (currentTask != null) currentTask.cancel();
						fComp.setBorder(new LineBorder(ADAPTION_MANAGER_COLOR, 2));
						m_uiTimer.schedule(new TimerTask() {

							@Override
							public void run() {
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										fComp.setBorder(null);
									}
								});
							}
						}, 1000);
					}
					
				};
				((IUIUpdater) uiComp).addUpdateListener(r);
			}
			
			m_desktopPane.add(frame);

		}
	}

	private void createAnalyzers() {
		Collection<IRainbowAnalysis> analyzers = Rainbow.instance().getRainbowMaster().analyzers();
		Map<String, Object> aui = m_uidb.containsKey("analyzers") ? (Map<String, Object>) m_uidb.get("analyzers")
				: Collections.<String, Object>emptyMap();
		for (IRainbowAnalysis a : analyzers) {
			String clazz = (String) aui.get(a.getClass().getName());
			JComponent uiComp = null;
			if (clazz != null) {
				try {
					Class<? extends JComponent> uiClass = (Class<? extends JComponent>) this.getClass().getClassLoader()
							.loadClass(clazz);
					uiComp = uiClass.newInstance();

				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				}

			}
			if (uiComp == null) {
				uiComp = new ArchConsolePanel();
			}
			m_analyzers.put(a.id(), uiComp);
			JInternalFrame frame = new JInternalFrame(a.id(), true, false, true);
			frame.setVisible(true);
			frame.add(uiComp);
			Dimension s = uiComp.getPreferredSize();
			frame.setSize(s.width, s.height+25);
			frame.addPropertyChangeListener(e -> {
				System.out.println("Selected " + a.id());
				if ("selection".equals(e.getPropertyName())) {
					m_selectionManager.selectionChanged(a);
				}
			});
			if (uiComp instanceof IUIUpdater) {
//				final java.util.Timer timer = new Timer();
				final JComponent fComp = uiComp;
				Runnable r = new Runnable() {
					TimerTask currentTask;
					
					@Override
					public void run() {
						if (currentTask != null) currentTask.cancel();
						fComp.setBorder(new LineBorder(ANALYZERS_COLOR, 2));
						m_uiTimer.schedule(new TimerTask() {

							@Override
							public void run() {
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										fComp.setBorder(null);
									}
								});
							}
						}, 1000);
					}
					
				};
				((IUIUpdater) uiComp).addUpdateListener(r);
			}
			m_desktopPane.add(frame);
		}
	}

	private void createModels() {
		ModelsManager modelsManager = Rainbow.instance().getRainbowMaster().modelsManager();
		Collection<? extends String> types = modelsManager.getRegisteredModelTypes();
		for (String t : types) {
			Collection<? extends IModelInstance<?>> models = modelsManager.getModelsOfType(t);
			for (IModelInstance<?> m : models) {

				String modelName = m.getModelName();
				String modelType = m.getModelType();
				ModelReference modelRef = new ModelReference(modelName, modelType);
				if (!m_models.containsKey(modelRef.toString()) && !"UtilityModel".equals(modelType)
						&& !"ExecutionHistory".equals(modelType)) {
					JInternalFrame frame = new JInternalFrame(modelName, true, false, true);
					frame.setVisible(true);
					ArchModelPanel mp = new ArchModelPanel(modelRef);
					frame.add(mp);
					Dimension s = mp.getPreferredSize();
					frame.setSize(s.width, s.height + 25);
					ModelInfo mi = new ModelInfo();
					mi.frame = frame;
					mi.modelRef = modelRef;
					m_models.put(modelRef.toString(), mi);
					mi.panel = mp;

					frame.addPropertyChangeListener(e -> {
						System.out.println("Selected " + modelRef.toString());
						if ("selection".equals(e.getPropertyName())) {
							m_selectionManager.selectionChanged(mi);
						}
					});
//					final java.util.Timer timer = new Timer();
					Runnable r = new Runnable() {
						TimerTask currentTask;
						
						@Override
						public void run() {
							if (currentTask != null) currentTask.cancel();
							mp.setBorder(new LineBorder(MODELS_MANAGER_COLOR, 2));
							mp.m_table.setSelectionBackground(MODELS_MANAGER_COLOR_LIGHT);
							m_uiTimer.schedule(new TimerTask() {

								@Override
								public void run() {
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											mp.setBorder(null);
											mp.m_table.clearSelection();
										}
									});
								}
							}, 1000);
						}
						
					};
					mp.addUpdateListener(r);
					m_desktopPane.add(frame);
				}
			}
		}
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

				frame.getDesktopIcon()
						.setUI(p.createIcon(frame, (Map<String, Object>) (m_uidb != null ? m_uidb.get("gauges")
								: Collections.<String, Object>emptyMap())));
				m_desktopPane.getDesktopManager().iconifyFrame(frame);
				frame.addPropertyChangeListener(e -> {
					System.out.println("Selected " + g);
					if ("selection".equals(e.getPropertyName())) {
						m_selectionManager.selectionChanged(info);
					}
				});

				m_gauges.put(g, info);
				p.addGaugeReportListener(c -> {

					if (m_gaugePanel.m_gaugeInfo == info)
						m_gaugePanel.initDataBindings(info);
				});
				p.addUpdateListener(() -> {
					final JComponent vFrame = getVisibleFrame(frame);
					vFrame.setBorder(new LineBorder(GAUGES_COLOR, 2));
					p.m_table.setSelectionBackground(GAUGES_COLOR_LIGHT);
//					final java.util.Timer t = new Timer();
					m_uiTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									vFrame.setBorder(null);
									p.m_table.clearSelection();
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
							probeInfo.reports.add(0, data);
							final JComponent vc = getVIsibleComponentToHiglight(probeInfo.frame);
							vc.setBorder(new LineBorder(SYSTEM_COLOR_LIGHT, 2));
							m_probeSections.get(pid).setText(data);
							ComponentReseter tcc = new ComponentReseter(1000, () -> vc.setBorder(null));
							tcc.run();
							m_probePanel.addToReports(probeInfo.description.alias);
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
				frame.addPropertyChangeListener(e -> {
					System.out.println("Selected " + probeId);
					if ("selection".equals(e.getPropertyName())) {
						m_selectionManager.selectionChanged(info);
					}
				});
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

	private static Pattern ERROR_PATTERN = Pattern.compile("\\[\\[(.*)\\]\\]: (.*)", Pattern.DOTALL);
	

	@Override
	public void report(RainbowComponentT component, ReportType type, String message) {
		super.report(component, type, message);
		Matcher m = ERROR_PATTERN.matcher(message);
		if (component == RainbowComponentT.MODEL) {
			Collection<ModelInfo> values = m_models.values();
			for (ModelInfo mi : values) {
				mi.panel.processReport(type, message);
			}
		} else if (component == RainbowComponentT.ANALYSIS && m.matches()) {
			JComponent c = m_analyzers.get(m.group(1));
			if (c instanceof IUIReporter) {
				((IUIReporter) c).processReport(type, m.group(2));
			}
		} else if (component == RainbowComponentT.ADAPTATION_MANAGER && m.matches()) {
			JComponent c = m_adaptationManagers.get(m.group(1));
			if (c instanceof IUIReporter) {
				((IUIReporter) c).processReport(type, m.group(2));
			}
		} else if (component == RainbowComponentT.EXECUTOR && m.matches()) {
			JComponent c = m_executors.get(m.group(1));
			if (c instanceof IUIReporter) {
				((IUIReporter) c).processReport(type, m.group(2));
			}
		}

		if (type == ReportType.ERROR || type == ReportType.FATAL) {
			m_errorArea.append(message);
			m_errorArea.setCaretPosition(m_errorArea.getText().length());
			m_selectionPanel.setIconAt(2, ERROR_ICON);

			if (m.matches()) {
				m_errorArea.append("Error in component: " + m.group(1));
				switch (component) {
				case GAUGE:
					GaugeInfo gi = m_gauges.get(m.group(1));
					if (gi != null) {
						DesktopIconUI f = gi.getFrame().getDesktopIcon().getUI();
						if (f instanceof IErrorDisplay) {
							IErrorDisplay e = (IErrorDisplay) f;
							e.displayError(m.group(2));
						}
					}
					break;
				case PROBE:
					ProbeInfo pi = m_probes.get(m.group(1));
					if (pi != null) {
						InternalFrameUI f = pi.frame.getUI();
						if (f instanceof IErrorDisplay) {
							IErrorDisplay e = (IErrorDisplay) f;
							e.displayError(message);
						}
					}

				}
			}

		}
	}

}
