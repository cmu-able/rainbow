package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
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
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.gui.arch.SimpleDesktopIconUI;
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

	class ProbeInfo {
		JInternalFrame frame;
		ProbeAttributes description;
		List<String> reports;
		boolean hasError;
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

	private void processProbeIntoGauge(GaugeInfo gInfo, Map<String, Object> setupParams, String tpt) {
		Pair<String, String> probe = Util.decomposeID(tpt);
		if (probe.secondValue() == null) {
			probe.setSecondValue((String) setupParams.get("targetIP"));
			gInfo.probes.add(Util.genID(probe.firstValue(), probe.secondValue()));
		} else if (IGauge.ALL_LOCATIONS.equals(probe.secondValue())) {
			Set<String> keySet = m_probes.keySet();
			for (String probeId : keySet) {
				Pair<String, String> candidate = Util.decomposeID(tpt);
				if (candidate.firstValue().equals(probe.firstValue())) {
					gInfo.probes.add(probeId);
				}

			}
		} else {
			gInfo.probes.add(tpt);
		}
	}

	protected void layoutArchitecture() {
		for (Map.Entry<String, GaugeInfo> entry : m_gauges.entrySet()) {
			GaugeInfo gInfo = entry.getValue();
			if (gInfo.probes == null) {
				Map<String, Object> configParams = toMap(gInfo.description.configParams());
				Map<String, Object> setupParams = toMap(gInfo.description.setupParams());

				entry.getValue().probes = new LinkedList<>();

				if (configParams.get("targetProbeType") instanceof String) {
					String tpt = (String) configParams.get("targetProbeType");
					processProbeIntoGauge(gInfo, setupParams, tpt);
				}
				if (configParams.get("targetProbeList") instanceof String) {
					String probeIds = (String) configParams.get("targetProbeList");
					for (String probeId : probeIds.split(",")) {
						probeId = probeId.trim();
						processProbeIntoGauge(gInfo, setupParams, probeId);
					}

				}

			}

		}
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

				frame.getDesktopIcon().setUI(new SimpleDesktopIconUI(frame.getFrameIcon()));

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
		frame.getDesktopIcon().setUI(new SimpleDesktopIconUI(frame.getFrameIcon()));
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
