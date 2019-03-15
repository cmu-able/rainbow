package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import org.sa.rainbow.core.IDisposable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.arch.RainbowDesktopIconUI;
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.util.Util;

public class RainbowWindoe implements IRainbowGUI, IDisposable, IRainbowReportingSubscriberCallback {

	protected static Color bleach(Color color, double amount) {
		int red = (int) ((color.getRed() * (1 - amount) / 255 + amount) * 255);
		int green = (int) ((color.getGreen() * (1 - amount) / 255 + amount) * 255);
		int blue = (int) ((color.getBlue() * (1 - amount) / 255 + amount) * 255);
		return new Color(red, green, blue);
	}

	private static final Color GAUGES_COLOR = Color.BLUE;
	private static final Color EFFECTORS_COLOR = Color.ORANGE;
	private static final Color SYSTEM_COLOR_LIGHT;
	private static final Color MODELS_MANAGER_COLOR = Color.MAGENTA;
	private static final Color MODELS_MANAGER_COLOR_LIGHT;
	private static final Color EXECUTORS_COLOR = Color.GREEN;
	private static final Color EXECUTORS_COLOR_LIGHT;
	private static final Color ANALYZERS_COLOR = Color.PINK;
	private static final Color ANALYZERS_COLOR_LIGHT;
	private static final Color ADAPTION_MANAGER_COLOR = Color.RED;
	private static final Color ADAPTION_MANAGER_COLOR_LIGHT;
	private static final Color GAUGES_COLOR_LIGHT;

	static {
		GAUGES_COLOR_LIGHT = bleach(GAUGES_COLOR, .75);
		SYSTEM_COLOR_LIGHT = bleach(EFFECTORS_COLOR, 0.75);
		MODELS_MANAGER_COLOR_LIGHT = bleach(MODELS_MANAGER_COLOR, 0.75);
		EXECUTORS_COLOR_LIGHT = bleach(EFFECTORS_COLOR, 0.75);
		ANALYZERS_COLOR_LIGHT = bleach(EFFECTORS_COLOR, 0.75);
		ADAPTION_MANAGER_COLOR_LIGHT = bleach(EFFECTORS_COLOR, 0.75);
	}
	public static final int MAX_TEXT_LENGTH = 100000;
	/** Convenience constant: size of text field to set to when Max is exceeded. */
	public static final int TEXT_HALF_LENGTH = 50000;
	public static final float TEXT_FONT_SIZE = 9.0f;
	
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 900;
	private static final Rectangle PROBE_REGION = new Rectangle((int )(WIDTH*2/3f), (int )(HEIGHT-HEIGHT/4f), (int )(WIDTH-2/3f*WIDTH), (int )(HEIGHT/4f));
	private static final Rectangle GAUGE_REGION = new Rectangle((int )(WIDTH*2/3f), (int )(HEIGHT-2*HEIGHT/4f), (int )(WIDTH-2/3f*WIDTH), (int )(HEIGHT/4f));
	class ProbeInfo {
		JInternalFrame frame;
		ProbeAttributes description;
		List<String> reports;
		boolean hasError;
		List<String> gauges = new LinkedList<>();
	}

	class GaugeInfo {
		JInternalFrame frame;
		GaugeInstanceDescription description;
		Map<String, List<IRainbowOperation>> operations;
		List<String> probes = null;
	}

	Map<String, ProbeInfo> m_probes = new HashMap<>();
	Map<String, GaugeInfo> m_gauges = new HashMap<>();
	Map<String, JInternalFrame> m_models = new HashMap<>();
	private IMasterCommandPort m_master;
	private JFrame m_frame;
	private JDesktopPane m_desktopPane;
	private OracleStatusPanel m_oracleMessagePane;
	Map<RainbowComponentT, JInternalFrame> m_internalFrames = new HashMap<>();
	private Timer m_tabTimer;

	public RainbowWindoe(IMasterCommandPort master) {
		setMaster(master);
	}

	public RainbowWindoe() {
		m_master = null;
	}

	private void initialize() {
		m_frame = new JFrame();
		m_frame.setBounds(0, 0, WIDTH, HEIGHT);
		m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m_frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				quit();
			}
		});
		m_desktopPane = new JDesktopPane() {
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				drawConnections(g2, this);
			}
		};
		m_desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		m_frame.getContentPane().add(m_desktopPane, BorderLayout.CENTER);

		List<String> expectedDelegateLocations;
		Throwable error = null;
		try {
			expectedDelegateLocations = m_master.getExpectedDelegateLocations();
		} catch (Throwable e) {
			expectedDelegateLocations = Arrays.asList("Error");
			error = e;
		}

		JInternalFrame masterFrame = new JInternalFrame("Rainbow Master");
		masterFrame.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		masterFrame.setMaximizable(true);
		masterFrame.setIconifiable(true);
		masterFrame.setResizable(true);
		masterFrame.setBounds(0, 0, 420, 60);
		m_desktopPane.add(masterFrame);
		masterFrame.setVisible(true);
		m_desktopPane.setDesktopManager(new RainbowDesktopManager(m_desktopPane));
//		m_desktopPane.getDesktopManager().minimizeFrame(masterFrame);

		m_oracleMessagePane = new OracleStatusPanel(Color.white, expectedDelegateLocations);
		masterFrame.getContentPane().add(m_oracleMessagePane, BorderLayout.CENTER);
		m_internalFrames.put(RainbowComponentT.MASTER, masterFrame);

		m_tabTimer = new javax.swing.Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (m_master.allDelegatesOK()) {
					populateArchitecture();
					layoutArchitecture();
					if (m_master.autoStartProbes()) {
						m_master.startProbes();
					}
					m_tabTimer.stop();
					m_tabTimer = null;
				}
			}
		});
		m_tabTimer.start();
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
			Component visibleGFrame = getVisibleComponent(gInfo.frame);
			int x1 = (int) Math.round(visibleGFrame.getBounds().getCenterX());
			int y1 = (int) Math.round(visibleGFrame.getBounds().getCenterY());
			if (gInfo.probes != null) {
				for (String p : gInfo.probes) {
					ProbeInfo pInfo = m_probes.get(p);
					Component visiblePFrame = getVisibleComponent(pInfo.frame);
					int x2 = (int) Math.round(visiblePFrame.getBounds().getCenterX());
					int y2 = (int) Math.round(visiblePFrame.getBounds().getCenterY());
					g2.drawLine(x1, y1, x2, y2);
				}

			}
		}
	}

	private Component getVisibleComponent(JInternalFrame frame) {
		Component visibleGFrame = frame;
		if (!visibleGFrame.isVisible() || frame.isIcon()
				|| (m_desktopPane.getDesktopManager() instanceof RainbowDesktopManager
						&& ((RainbowDesktopManager) m_desktopPane.getDesktopManager()).isIcon(frame))) {
			visibleGFrame = frame.getDesktopIcon();
		}
		return visibleGFrame;
	}

	private void processProbeIntoGauge(String gaugeKey, GaugeInfo gInfo, Map<String, Object> setupParams, String tpt) {
		Pair<String, String> probe = Util.decomposeID(tpt);
		if (probe.secondValue() == null) {
			probe.setSecondValue((String) setupParams.get("targetIP"));
			String pid = Util.genID(probe.firstValue(), probe.secondValue());
			gInfo.probes.add(pid);
			m_probes.get(pid).gauges.add(gaugeKey);
		} else if (IGauge.ALL_LOCATIONS.equals(probe.secondValue())) {
			Set<String> keySet = m_probes.keySet();
			for (String probeId : keySet) {
				Pair<String, String> candidate = Util.decomposeID(tpt);
				if (candidate.firstValue().equals(probe.firstValue())) {
					gInfo.probes.add(probeId);
					m_probes.get(probeId).gauges.add(gaugeKey);
				}

			}
		} else {
			gInfo.probes.add(tpt);
			m_probes.get(tpt).gauges.add(gaugeKey);
		}
	}

	protected void layoutArchitecture() {
//		mxGraph graph = new mxGraph();
//		Object parent = graph.getDefaultParent();
//		Object parentNode = graph.insertVertex(parent, null, null, 0, 0, 10, 10);
//		Set<Object> cells = new HashSet<>();
//		graph.getModel().beginUpdate();
		try {
			for (Map.Entry<String, GaugeInfo> entry : m_gauges.entrySet()) {
				GaugeInfo gInfo = entry.getValue();
//				Component visibleFrame = getVisibleComponent(gInfo.frame);
//				Object gaugeNode = graph.insertVertex(parent, gInfo.description.id(), visibleFrame, visibleFrame.getX(),
//						visibleFrame.getY(), visibleFrame.getWidth(), visibleFrame.getHeight());
//				cells.add(gaugeNode);
//				graph.insertEdge(parent, null, "edge", parentNode, gaugeNode);
				if (gInfo.probes == null) {
					Map<String, Object> configParams = toMap(gInfo.description.configParams());
					Map<String, Object> setupParams = toMap(gInfo.description.setupParams());

					entry.getValue().probes = new LinkedList<>();

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
//					for (String pid : gInfo.probes) {
//						ProbeInfo pInfo = m_probes.get(pid);
//						visibleFrame = getVisibleComponent(pInfo.frame);
//						Object probeNode = graph.insertVertex(parent, pInfo.description.name, visibleFrame,
//								visibleFrame.getX(), visibleFrame.getY(), visibleFrame.getWidth(),
//								visibleFrame.getHeight());
//						cells.add(probeNode);
//						graph.insertEdge(parent, null, "edge", gaugeNode, probeNode);
//					}

				}

			}

			String[] probeLevel = new String[m_probes.size()];
			String[] gaugeLevel = new String[m_gauges.size()];
			Arrays.fill(probeLevel, "");
			Arrays.fill(gaugeLevel, "");
			;
			ArrayList<String> processedProbes = new ArrayList<>(m_probes.size());
			ArrayList<String> processedGauges = new ArrayList<>();
			for (Entry<String, GaugeInfo> ge : m_gauges.entrySet()) {
				for (String pid : ge.getValue().probes) {
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
				for (String pid : ge.getValue().probes) {
					if (!processedProbes.contains(pid))
						processedProbes.add(pid);
				}
			}
			for (String pid : m_probes.keySet()) {
				if (!processedProbes.contains(pid))
					processedProbes.add(pid);
			}
			int gidx = 0;
			int gaugeStep = Math.round(GAUGE_REGION.width / (float )processedGauges.size());
			for (String gid : processedGauges) {
				JDesktopIcon frameToPosition = m_gauges.get(gid).frame.getDesktopIcon();
				frameToPosition.setLocation(GAUGE_REGION.x + gidx*gaugeStep + (gaugeStep + frameToPosition.getWidth())/2, GAUGE_REGION.y + (GAUGE_REGION.height + frameToPosition.getHeight())/2);
				gidx++;
			}
			
			gidx = 0;
			gaugeStep = Math.round(PROBE_REGION.width / (float )processedProbes.size());
			for (String gid : processedProbes) {
				JDesktopIcon frameToPosition = m_probes.get(gid).frame.getDesktopIcon();
				frameToPosition.setLocation(PROBE_REGION.x + gidx*gaugeStep + (gaugeStep + frameToPosition.getWidth())/2, PROBE_REGION.y + (PROBE_REGION.height + frameToPosition.getHeight())/2);
				gidx++;
			}
			
//			System.out.println(processedGauges);
//			System.out.println(processedProbes);
			
		} finally {
//			graph.getModel().endUpdate();
		}
//		graph.setMaximumGraphBounds(new mxRectangle(400,400,WIDTH-400,HEIGHT-400));
//		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
////		layout.setLevelDistance(50);
//		layout.execute(graph.getDefaultParent());
//		for (Object c : cells) {
//			mxRectangle b = graph.getCellBounds(c);
//			com.mxgraph.model.mxCell cell = (mxCell) c;
//			if (cell.getValue() instanceof Component) {
//				Component f = (Component) cell.getValue();
//				f.setBounds((int) b.getX()/50, (int) b.getY(), (int) b.getWidth(), (int) b.getHeight());
//			}
//
//		}
//		int childCount = graph.getModel().getChildCount(parent);
//		for (GaugeInfo gi : m_gauges.values()) {
//			mxRectangle b = graph.getCellBounds(gi.description.id());
//			gi.frame.setBounds((int) b.getX(), (int) b.getY(), (int) b.getWidth(), (int) b.getHeight());
//		}
//
//		for (ProbeInfo pi : m_probes.values()) {
//			mxRectangle b = graph.getCellBounds(pi.description.name);
//			pi.frame.setBounds((int) b.getX(), (int) b.getY(), (int) b.getWidth(), (int) b.getHeight());
//		}
	}

	protected void quit() {
		Rainbow.instance().signalTerminate();
	}

	private void populateArchitecture() {
		createProbes();
		createGauges();
	}

	private void createGauges() {
		GaugeManager gaugeManager = Rainbow.instance().getRainbowMaster().gaugeManager();
		int i = 1;
		for (String g : gaugeManager.getCreatedGauges()) {
			if (m_gauges.get(g) == null) {
				GaugeInstanceDescription description = Rainbow.instance().getRainbowMaster().gaugeDesc().instSpec
						.get(g.split("@")[0].split(":")[0]);
				JInternalFrame frame = new JInternalFrame(shortName(g), true, false, true);
				frame.setFrameIcon(new ImageIcon(this.getClass().getResource("/gauge.png"), shortName(g)));
				frame.setIconifiable(true);
				frame.setToolTipText(g);
				JTextArea p = new JTextArea();
				JScrollPane sp = new JScrollPane();
				sp.setViewportView(p);
				frame.add(sp, BorderLayout.CENTER);
				m_desktopPane.add(frame);

				frame.getDesktopIcon().setUI(new RainbowDesktopIconUI(frame.getFrameIcon()));

				frame.setVisible(true);
				frame.setSize(100, 100);
				frame.setLocation(WIDTH - i * 100, HEIGHT - 340);
				i++;
				m_desktopPane.getDesktopManager().iconifyFrame(frame);

				GaugeInfo info = new GaugeInfo();
				info.frame = frame;
				IGauge gauge = Rainbow.instance().lookupGauge(g);
				info.description = description;
				info.operations = new HashMap<>();
				for (String key : gauge.commandKeys()) {
					info.operations.put(key, new LinkedList<>());
				}
				m_gauges.put(g, info);
			}
		}
	}

	private String shortName(String g) {
		return g.split("@")[0].split(":")[0];
	}

	private void createProbes() {
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
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(p);
		frame.add(sp, BorderLayout.CENTER);
		m_desktopPane.add(frame);
		frame.getDesktopIcon().setUI(new RainbowDesktopIconUI(frame.getFrameIcon()));
		return frame;
	}

	@Override
	public void report(RainbowComponentT component, ReportType type, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void display() {
		if (m_frame != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					show();
				}
			});
		}
	}

	protected void show() {
		if (m_frame == null) {
			initialize();
//			populateArchitecture();
		}
		m_frame.setVisible(true);
	}

	@Override
	public void setMaster(IMasterCommandPort master) {
		boolean needsInit = m_master == null;
		m_master = master;
		if (needsInit)
			initialize();
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
