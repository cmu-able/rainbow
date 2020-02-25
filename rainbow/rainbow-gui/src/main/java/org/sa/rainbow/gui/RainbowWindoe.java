package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSourceDOT;
import org.ho.yaml.Yaml;
import org.sa.rainbow.core.IDisposable;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.gauges.IGauge;
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
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.arch.RainbowDesktopManager;
import org.sa.rainbow.gui.arch.controller.IRainbowUIController;
import org.sa.rainbow.gui.arch.controller.RainbowAdaptationManagerController;
import org.sa.rainbow.gui.arch.controller.RainbowAnalysisController;
import org.sa.rainbow.gui.arch.controller.RainbowEffectorController;
import org.sa.rainbow.gui.arch.controller.RainbowExecutorController;
import org.sa.rainbow.gui.arch.controller.RainbowGaugeController;
import org.sa.rainbow.gui.arch.controller.RainbowModelController;
import org.sa.rainbow.gui.arch.controller.RainbowProbeController;
import org.sa.rainbow.gui.arch.elements.AdaptationManagerTabbedPane;
import org.sa.rainbow.gui.arch.elements.DefaultThreadInfoPane;
import org.sa.rainbow.gui.arch.elements.EffectorTabbedPane;
import org.sa.rainbow.gui.arch.elements.GaugeTabbedPane;
import org.sa.rainbow.gui.arch.elements.ModelTabbedPane;
import org.sa.rainbow.gui.arch.elements.ProbeTabbedPane;
import org.sa.rainbow.gui.arch.model.RainbowArchAdapationManagerModel;
import org.sa.rainbow.gui.arch.model.RainbowArchAnalysisModel;
import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchGaugeModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;
import org.sa.rainbow.gui.arch.model.RainbowArchProbeModel;
import org.sa.rainbow.gui.arch.model.RainbowSystemModel;
import org.sa.rainbow.gui.arch.model.RainbowSystemModel.IRainbowModelVisitor;
import org.sa.rainbow.gui.visitor.DOTStringGraphConstructor;
import org.sa.rainbow.gui.widgets.DesktopScrollPane;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.sa.rainbow.util.Util;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;

public class RainbowWindoe extends RainbowWindow
		implements IRainbowGUI, IDisposable, IRainbowReportingSubscriberCallback {

	private static final int WIDTH = 1280;

	public static final ImageIcon ERROR_ICON = new ImageIcon(RainbowWindoe.class.getResource("/error.png"));

	public static class SelectionManager {
		public interface ISelectionListener {
			public void selectionChanged(Object o, Boolean selected);
		}

		Collection<ISelectionListener> m_listeners = new HashSet<>();

		public void addSelectionListener(ISelectionListener l) {
			m_listeners.add(l);
		}

		public void removeSelectionListener(ISelectionListener l) {
			m_listeners.remove(l);
		}

		public void selectionChanged(Object o, Boolean selected) {
			for (ISelectionListener l : m_listeners) {
				SwingUtilities.invokeLater(() -> l.selectionChanged(o, selected));
			}
		}

	}

	SelectionManager m_selectionManager = new SelectionManager();
	RainbowSystemModel m_rainbowModel = new RainbowSystemModel();

	private Map<String, JTextArea> m_probeTextAreas = new HashMap<>();

	private Map<String, Object> m_uidb;

	private ArrayList<java.awt.geom.Line2D> m_lines;

	private JPanel m_rootPane;

	private JTabbedPane m_selectionPanel;

	private JTabbedPane m_logTabs;

	private JPanel m_detailsPanel;

	private JTextArea m_errorArea;

	private ProbeTabbedPane m_probePanel;

	private GaugeTabbedPane m_gaugePanel;

	private ModelTabbedPane m_modelPanel;

	private AdaptationManagerTabbedPane m_amPanel;

	private EffectorTabbedPane m_effectorPanel;

	private JLabel m_statusWindow;

	private PropertyChangeListener locationChange = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// DOT layout does not allow to fix node positions
//			if (RainbowArchModelElement.LOCATION_PROPERTY.equals(evt.getPropertyName())) {
//				layoutDOT();
//			}
		}
	};

	private DefaultThreadInfoPane m_exPanel;

	private DefaultThreadInfoPane m_anPanel;
	
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();


	public RainbowWindoe(IMasterCommandPort master) {
		super(master);
		init();
	}

	public RainbowWindoe() {
		super();
		init();
	}

	private void init() {
		File specs = Util.getRelativeToPath(m_rainbowEnvironment.getTargetPath(),
				m_rainbowEnvironment.getProperty("rainbow.gui.specs"));
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

				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				drawConnections(g2, this);
			}
		};
		m_desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		m_desktopPane.setDesktopManager(new RainbowDesktopManager(m_desktopPane));

		DesktopScrollPane dsp = new DesktopScrollPane(m_desktopPane);
		dsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		dsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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

		m_selectionManager.addSelectionListener((o, selected) -> {
			if (!selected)
				return;
			m_probePanel.setVisible(false);
			m_gaugePanel.setVisible(false);
			m_modelPanel.setVisible(false);
			m_amPanel.setVisible(false);
			m_effectorPanel.setVisible(false);
			m_anPanel.setVisible(false);
			m_exPanel.setVisible(false);
			if (o instanceof RainbowArchProbeModel) {
				RainbowArchProbeModel probeInfo = (RainbowArchProbeModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_probePanel.setVisible(true);
				m_probePanel.setProbeInfo(probeInfo);
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "probes");
			} else if (o instanceof RainbowArchGaugeModel) {
				RainbowArchGaugeModel gaugeInfo = (RainbowArchGaugeModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_gaugePanel.setVisible(true);
				m_gaugePanel.initDataBindings(gaugeInfo);
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "gauges");

			} else if (o instanceof RainbowArchModelModel) {
				RainbowArchModelModel gaugeInfo = (RainbowArchModelModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_modelPanel.setVisible(true);
				m_modelPanel.initDataBindings(gaugeInfo);
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "models");

			} else if (o instanceof RainbowArchAdapationManagerModel) {
				RainbowArchAdapationManagerModel amModel = (RainbowArchAdapationManagerModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_amPanel.setVisible(true);
				m_amPanel.initBindings(amModel);
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "adaptationmanagers");
			} else if (o instanceof RainbowArchEffectorModel) {
				RainbowArchEffectorModel effModel = (RainbowArchEffectorModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_effectorPanel.setVisible(true);
				m_effectorPanel.initBindings(effModel);

				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "effectors");

			} else if (o instanceof RainbowArchExecutorModel) {
				RainbowArchExecutorModel exModel = (RainbowArchExecutorModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_exPanel.setVisible(true);
				m_exPanel.initBindings(exModel);
				
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "executors");

			}else if (o instanceof RainbowArchAnalysisModel) {
				RainbowArchAnalysisModel anModel = (RainbowArchAnalysisModel) o;
				m_selectionPanel.setSelectedIndex(1);
				m_anPanel.setVisible(true);
				m_anPanel.initBindings(anModel);
				
				((CardLayout) m_detailsPanel.getLayout()).show(m_detailsPanel, "analyzers");

			}
		});
		m_statusWindow = new JLabel("Waiting for Rainbow to start...");
		m_statusWindow
				.setFont(new Font(m_statusWindow.getFont().getFontName(), m_statusWindow.getFont().getStyle(), 18));
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

		m_gaugePanel = new GaugeTabbedPane();
		m_detailsPanel.add(m_gaugePanel);
		m_gaugePanel.setVisible(false);
		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_gaugePanel, "gauges");

		JTextArea gaugesLogs = createTextAreaInTab(m_logTabs, "Gauges");
		m_allTabs.put(RainbowComponentT.GAUGE_MANAGER, gaugesLogs);
		m_allTabs.put(RainbowComponentT.GAUGE, gaugesLogs);
	}

	@Override
	protected void createModelsManagerUI() {
		m_modelPanel = new ModelTabbedPane();
		m_detailsPanel.add(m_modelPanel);
		m_modelPanel.setVisible(false);
		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_modelPanel, "models");

		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Models");
		m_allTabs.put(RainbowComponentT.MODEL, modelsLogs);
	}

	@Override
	protected void createAdaptationManagerUI() {
		if (m_uidb.containsKey("details")) {
			String className = ((Map<String, String>) m_uidb.get("details")).get("managers");
			if (className != null) {
				try {
					Class<?> clazz = this.getClass().forName(className);
					m_amPanel = (AdaptationManagerTabbedPane) clazz.newInstance();

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		if (m_amPanel == null) {
			m_amPanel = new AdaptationManagerTabbedPane();
		}
		m_detailsPanel.add(m_amPanel);
		m_amPanel.setVisible(true);

		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_amPanel, "adaptationmanagers");

		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Adaptation Manager");
		m_allTabs.put(RainbowComponentT.ADAPTATION_MANAGER, modelsLogs);
	}

	@Override
	protected void createExecutorsUI() {
		
		if (m_uidb.containsKey("details")) {
			String className = ((Map<String, String>) m_uidb.get("details")).get("executors");
			if (className != null) {
				try {
					Class<?> clazz = this.getClass().forName(className);
					m_exPanel = (DefaultThreadInfoPane) clazz.newInstance();

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		if (m_exPanel == null) {
			m_exPanel = new DefaultThreadInfoPane();
		}
		m_detailsPanel.add(m_exPanel);
		m_exPanel.setVisible(true);

		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_exPanel, "executors");

		
		
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Execution");
		m_allTabs.put(RainbowComponentT.EXECUTOR, modelsLogs);
	}

	@Override
	protected void createAnalyzersUI() {
		if (m_uidb.containsKey("details")) {
			String className = ((Map<String, String>) m_uidb.get("details")).get("analyzers");
			if (className != null) {
				try {
					Class<?> clazz = this.getClass().forName(className);
					m_amPanel = (AdaptationManagerTabbedPane) clazz.newInstance();

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		if (m_anPanel == null) {
			m_anPanel = new DefaultThreadInfoPane();
		}
		m_detailsPanel.add(m_anPanel);
		m_anPanel.setVisible(true);

		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_anPanel, "analyzers");
		
		
		JTextArea modelsLogs = createTextAreaInTab(m_logTabs, "Analyzers");
		m_allTabs.put(RainbowComponentT.ANALYSIS, modelsLogs);
	}

	@Override
	protected void createEffectorsUI() {
		m_effectorPanel = new EffectorTabbedPane();
		m_detailsPanel.add(m_effectorPanel);
		m_effectorPanel.setVisible(false);
		((CardLayout) m_detailsPanel.getLayout()).addLayoutComponent(m_effectorPanel, "effectors");

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
	}

	private void processProbeIntoGauge(String gaugeKey, RainbowArchGaugeModel gInfo, Map<String, Object> setupParams,
			String tpt) {
		Pair<String, String> probe = Util.decomposeID(tpt);
		if (probe.secondValue() == null) {
			probe.setSecondValue((String) setupParams.get("targetIP"));
			String pid = Util.genID(probe.firstValue(), probe.secondValue());
			gInfo.addProbe(pid);
			m_rainbowModel.getProbe(pid).addListeningGauge(gaugeKey);
		} else if (IGauge.ALL_LOCATIONS.equals(probe.secondValue())) {
			Collection<RainbowArchProbeModel> keySet = m_rainbowModel.getProbes();
			for (RainbowArchProbeModel pm : keySet) {
				Pair<String, String> candidate = Util.decomposeID(tpt);
				if (candidate.firstValue().equals(probe.firstValue())) {
					gInfo.addProbe(pm.getId());
					m_rainbowModel.getProbe(pm.getId()).addListeningGauge(gaugeKey);
				}

			}
		} else {
			gInfo.addProbe(tpt);
			m_rainbowModel.getProbe(tpt).addListeningGauge(gaugeKey);
		}
	}

	protected void layoutArchitecture() {

		try {
			Collection<RainbowArchGaugeModel> gauges = m_rainbowModel.getGauges();
			for (RainbowArchGaugeModel gInfo : gauges) {
				if (gInfo.getProbes() == null) {
					Map<String, Object> configParams = toMap(gInfo.getGaugeDesc().configParams());
					Map<String, Object> setupParams = toMap(gInfo.getGaugeDesc().setupParams());
					if (configParams.get("targetProbeType") instanceof String) {
						String tpt = (String) configParams.get("targetProbeType");
						if (tpt.contains(",")) {
							for (String probeId : tpt.split(",")) {
								probeId = probeId.trim();
								processProbeIntoGauge(gInfo.getId(), gInfo, setupParams, probeId);
							}
						}
						else 
							processProbeIntoGauge(gInfo.getId(), gInfo, setupParams, tpt);
					}
					if (configParams.get("targetProbeList") instanceof String) {
						String probeIds = (String) configParams.get("targetProbeList");
						for (String probeId : probeIds.split(",")) {
							probeId = probeId.trim();
							processProbeIntoGauge(gInfo.getId(), gInfo, setupParams, probeId);
						}

					}
					TypedAttribute model = gInfo.getGaugeDesc().modelDesc();
					String modelref = new ModelReference(model.getName(), model.getType()).toString();
					RainbowArchModelModel mm = m_rainbowModel.getModel(modelref);
					mm.addGaugeReference(gInfo.getId());

				}
			}

			layoutDOT("dot");

		} finally {
		}
	}

	private boolean layoutDOT(String layout) {
		try {
			m_lines = new ArrayList<Line2D>();

//			GraphStreamGraphConstructor gc = new GraphStreamGraphConstructor();
//			m_rainbowModel.visit(gc);
//			Graph g = gc.m_graph;
//			
//			FileSinkDOT fs = new FileSinkDOT();
			File tmp = File.createTempFile("rainbow", ".dot");
			File tmpo = File.createTempFile("layout", ".dot");
//			fs.writeAll(g, tmp.getAbsolutePath());

			DOTStringGraphConstructor gc = new DOTStringGraphConstructor();
			m_rainbowModel.visit(gc);

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {
				writer.append(gc.m_graph);
			}

			Runtime rt = Runtime.getRuntime();
			String[] args = { "/usr/bin/dot", "-K" + layout, "-Tdot", /* "-Gdpi=" + res, */tmp.getAbsolutePath(), "-o",
					tmpo.getAbsolutePath() };
			Process p = rt.exec(args);
			p.waitFor();

			loadLayoutIntoDisplayMerz(tmpo);
//			loadLayoutIntoDisplayGraphstream(tmpo);

		} catch (HeadlessException | IdAlreadyInUseException | EdgeRejectedException | IOException
				| InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	protected void loadLayoutIntoDisplayMerz(File tmpo) throws IOException, FileNotFoundException {
		Parser gvp = new Parser();
		try (FileReader fr = new FileReader(tmpo)) {
			gvp.parse(fr);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		ArrayList<com.alexmerz.graphviz.objects.Graph> graphs = gvp.getGraphs();
		com.alexmerz.graphviz.objects.Graph graph = graphs.get(0);

		for (com.alexmerz.graphviz.objects.Node n : graph.getNodes(false)) {
			RainbowArchModelElement model = m_rainbowModel.getRainbowElement(n.getId().getLabel());
			if (model != null) {
				String pos = n.getAttribute("pos");
				String[] posA = pos.split(",");
				Point2D location = new Point2D.Float(Float.parseFloat(posA[0]), Float.parseFloat(posA[1]));
				model.getController().move(location, false);
			}
		}

		for (com.alexmerz.graphviz.objects.Edge e : graph.getEdges()) {
			String posA = e.getAttribute("pos");
			String[] posS = posA.split(" ");
			Point[] locs = new Point[posS.length];
			for (int i = 0; i < posS.length; i++) {
				String pos = posS[i];
				String[] point = pos.split(",");
				Point location = new Point(Math.round(Float.parseFloat(point[0])),
						Math.round(Float.parseFloat(point[1])));
				Point realPoint = location;
				locs[i] = realPoint;
				if (i >= 1) {
					m_lines.add(new Line2D.Float(locs[i - 1].x, locs[i - 1].y, locs[i].x, locs[i].y));
				}
			}

		}
	}

	protected void loadLayoutIntoDisplayGraphstream(File tmpo) throws IOException {
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
			RainbowArchModelElement model = m_rainbowModel.getRainbowElement(n.getId());
			if (model != null) {
				String pos = (String) n.getAttribute("pos");
				Point2D location = new Point2D.Float(Float.parseFloat(pos.split(",")[0])/* + 10 */,
						Float.parseFloat(pos.split(",")[1])); /// * + 10*/, getDesktopFram(a).getBounds().getSize());
				Point2D realPoint = location;
				model.getController().move(realPoint, false);
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
					Point realPoint = location;
					locs[i] = realPoint;
					if (i >= 1) {
						m_lines.add(new Line2D.Float(locs[i - 1].x, locs[i - 1].y, locs[i].x, locs[i].y));
					}
				}

			}
		}
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
		m_desktopPane.remove(m_statusWindow);
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
							RainbowArchEffectorModel model = m_rainbowModel.getEffectorModel(effector.id());
							if (model != null)
								model.executed(outcome, args);

						}

						@Override
						public void reportExecuting(IEffectorIdentifier effector, List<String> args) {
							RainbowArchEffectorModel model = m_rainbowModel.getEffectorModel(effector.id());
							if (model != null)
								model.executing(args);
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

		EffectorDescription effectorDesc = m_rainbowEnvironment.getRainbowMaster().effectorDesc();
		for (EffectorAttributes ea : effectorDesc.effectors) {
			if (!m_rainbowModel.hasEffector(ea)) {
				RainbowArchEffectorModel model = new RainbowArchEffectorModel(ea);
				model.addPropertyChangeListener(locationChange);
				IRainbowUIController ctrl = new RainbowEffectorController(model, m_selectionManager);
				JInternalFrame frame = ctrl.createView(m_desktopPane);
				m_rainbowModel.addEffector(model);
			}
		}
	}

	private void createExecutors() {
		Map<String, IAdaptationExecutor<?>> executors = m_rainbowEnvironment.getRainbowMaster().adaptationExecutors();
		Map<String, Object> aui = m_uidb.containsKey("executors") ? (Map<String, Object>) m_uidb.get("executors")
				: Collections.<String, Object>emptyMap();
		for (IAdaptationExecutor a : executors.values()) {
			RainbowArchExecutorModel model = new RainbowArchExecutorModel(a);
			model.addPropertyChangeListener(locationChange);

			RainbowExecutorController ctrl = new RainbowExecutorController(model, m_selectionManager, m_uidb);
			ctrl.getModel().addPropertyChangeListener(locationChange);
			ctrl.createView(m_desktopPane);
			m_rainbowModel.addExecutor(model);
		}
	}

	private void createAdaptationManagers() {
		Map<String, IAdaptationManager<?>> analyzers = m_rainbowEnvironment.getRainbowMaster().adaptationManagers();
		Map<String, Object> aui = m_uidb.containsKey("managers") ? (Map<String, Object>) m_uidb.get("managers")
				: Collections.<String, Object>emptyMap();
		for (IAdaptationManager a : analyzers.values()) {
			RainbowArchAdapationManagerModel model = new RainbowArchAdapationManagerModel(a);
			model.addPropertyChangeListener(locationChange);

			RainbowAdaptationManagerController ctrl = new RainbowAdaptationManagerController(model, m_selectionManager,
					m_uidb);
			ctrl.createView(m_desktopPane);
			m_rainbowModel.addAdaptationManager(model);
		}
	}

	private void createAnalyzers() {
		Collection<IRainbowAnalysis> analyzers = m_rainbowEnvironment.getRainbowMaster().analyzers();
		Map<String, Object> aui = m_uidb.containsKey("analyzers") ? (Map<String, Object>) m_uidb.get("analyzers")
				: Collections.<String, Object>emptyMap();
		for (IRainbowAnalysis a : analyzers) {
			RainbowArchAnalysisModel model = new RainbowArchAnalysisModel(a);
			model.addPropertyChangeListener(locationChange);

			RainbowAnalysisController ctrl = new RainbowAnalysisController(model, m_selectionManager, m_uidb);
			ctrl.createView(m_desktopPane);
			m_rainbowModel.addAnalyzer(model);
		}
	}

	private void createModels() {
		ModelsManager modelsManager = m_rainbowEnvironment.getRainbowMaster().modelsManager();
		Collection<? extends String> types = modelsManager.getRegisteredModelTypes();
		for (String t : types) {
			Collection<? extends IModelInstance<?>> models = modelsManager.getModelsOfType(t);
			for (IModelInstance<?> m : models) {

				String modelName = m.getModelName();
				String modelType = m.getModelType();
				ModelReference modelRef = new ModelReference(modelName, modelType);
				if (!m_rainbowModel.hasModel(modelRef.toString()) && !"UtilityModel".equals(modelType)
						&& !"ExecutionHistory".equals(modelType)) {
					RainbowArchModelModel model = new RainbowArchModelModel(modelRef);
					model.addPropertyChangeListener(locationChange);

					IRainbowUIController ctrl = new RainbowModelController(model, m_selectionManager);
					JInternalFrame frame = ctrl.createView(m_desktopPane);
					m_rainbowModel.addModel(model);
				}
			}
		}
	}

	private void createGauges() {
		GaugeManager gaugeManager = m_rainbowEnvironment.getRainbowMaster().gaugeManager();
		int i = 1;
		for (String g : gaugeManager.getCreatedGauges()) {
			if (!m_rainbowModel.hasGauge(g)) {
				GaugeInstanceDescription description = m_rainbowEnvironment.getRainbowMaster().gaugeDesc().instSpec
						.get(g.split("@")[0].split(":")[0]);
				RainbowArchGaugeModel model = new RainbowArchGaugeModel(description);
				model.addPropertyChangeListener(locationChange);

				IRainbowUIController ctrl = new RainbowGaugeController(model, m_selectionManager, m_uidb);
				JInternalFrame frame = ctrl.createView(m_desktopPane);
				m_rainbowModel.addGauge(model);

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
							RainbowArchProbeModel pm = m_rainbowModel.getProbe(pid);
							if (pm != null) {
								pm.addReport(data);
								m_probePanel.addToReports(pid);

							}

						}
					});
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ProbeDescription probes = m_rainbowEnvironment.getRainbowMaster().probeDesc();
		int i = 1;
		for (ProbeAttributes probe : probes.probes) {
			String probeId = probe.alias + "@" + probe.getLocation();
			if (!m_rainbowModel.hasProbe(probeId)) {
				RainbowArchProbeModel model = new RainbowArchProbeModel(probe);
				model.addPropertyChangeListener(locationChange);

				IRainbowUIController ctrl = new RainbowProbeController(model, m_selectionManager);
				JInternalFrame frame = ctrl.createView(m_desktopPane);
				m_rainbowModel.addProbe(model);
			}
//			if (m_probes.get(probeId) == null) {
//				JInternalFrame frame = addProbeFrame(probeId);
//				frame.setSize(100, 100);
//				frame.setLocation(WIDTH - i * 100, HEIGHT - 140);
//				i++;
//
//				m_desktopPane.getDesktopManager().iconifyFrame(frame);
//				frame.setVisible(true);
//				ProbeInfo info = new ProbeInfo();
//				info.description = probe;
//				info.frame = frame;
//				info.reports = new LinkedList<>();
//				info.hasError = false;
//
//				m_probes.put(probeId, info);
//				frame.addPropertyChangeListener(e -> {
//					System.out.println("Selected " + probeId);
//					if ("selection".equals(e.getPropertyName())) {
//						m_selectionManager.selectionChanged(info);
//					}
//				});
//			}
			m_createProbeReportingPortSubscriber.subscribeToProbe(probe.alias, probe.getLocation());

		}

	}

//	private JInternalFrame addProbeFrame(String probeId) {
//		JInternalFrame frame = new JInternalFrame(shortName(probeId));
//		frame.setFrameIcon(new ImageIcon(this.getClass().getResource("/probe.png"), shortName(probeId)));
//		frame.setResizable(true);
//		frame.setClosable(false);
//		frame.setIconifiable(true);
//		frame.setToolTipText(probeId);
//		JTextArea p = new JTextArea();
//		m_probeSections.put(probeId, p);
//		JScrollPane sp = new JScrollPane();
//		sp.setViewportView(p);
//		frame.add(sp, BorderLayout.CENTER);
//		m_desktopPane.add(frame);
//		frame.getDesktopIcon().setUI(new RainbowDesktopIconUI(frame.getFrameIcon()));
//		return frame;
//	}

	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
////					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//					RainbowWindoe window = new RainbowWindoe(null);
//					JInternalFrame frame = window.addProbeFrame("Test");
//					frame.setSize(100, 100);
//					frame.setLocation(100, 100);
//					frame.setVisible(true);
//					window.m_frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
	}

	private static Pattern ERROR_PATTERN = Pattern.compile("\\[\\[(.*)\\]\\]: (.*)", Pattern.DOTALL);

	@Override
	public void report(RainbowComponentT component, ReportType type, String message) {
		super.report(component, type, message);
		Matcher m = ERROR_PATTERN.matcher(message);
		if (component == RainbowComponentT.MODEL) {
			Collection<RainbowArchModelModel> values = m_rainbowModel.getModels();
			for (RainbowArchModelModel mi : values) {
				mi.getController().processReport(type, message);
			}
		} else if (component == RainbowComponentT.ANALYSIS && m.matches()) {
			RainbowArchAnalysisModel ma = m_rainbowModel.getAnalyzer(m.group(1));
			ma.getController().processReport(type, m.group(2));
		} else if (component == RainbowComponentT.ADAPTATION_MANAGER && m.matches()) {
			RainbowArchAdapationManagerModel mam = m_rainbowModel.getAdaptationManager(m.group(1));
			mam.getController().processReport(type, m.group(2));
		} else if (component == RainbowComponentT.EXECUTOR && m.matches()) {
			RainbowArchExecutorModel ex = m_rainbowModel.getExecutor(m.group(1));
			ex.getController().processReport(type, m.group(2));
		}

		if (type == ReportType.ERROR || type == ReportType.FATAL) {
			m_errorArea.append(message);
			m_errorArea.setCaretPosition(m_errorArea.getText().length());
			m_selectionPanel.setIconAt(2, ERROR_ICON);

			if (m.matches()) {
				m_errorArea.append("Error in component: " + m.group(1));
				switch (component) {
				case GAUGE:
					RainbowArchGaugeModel gi = m_rainbowModel.getGauge(m.group(1));
					if (gi != null) {
						gi.setError(message);
					}
					break;
				case PROBE:
					RainbowArchProbeModel pm = m_rainbowModel.getProbe(m.group(1));
					if (pm != null) {
						pm.setError(message);
					}
				}
			}

		}
	}
	
	@Override
	protected void createMenuBar() {
		super.createMenuBar();
		JMenu menu = new JMenu("Layout");
		menu.setMnemonic(KeyEvent.VK_L);
		createLayoutMenu(menu);
		m_menuBar.add(menu, 3);
	}

	private void createLayoutMenu(JMenu menu) {
		JMenuItem item;
		
		item = new JMenuItem("Dot");
		item.setMnemonic(KeyEvent.VK_D);
		item.setToolTipText("Layout desktop using the dot default layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("dot");
			}
		});
		menu.add(item);

		item = new JMenuItem("Circo");
		item.setMnemonic(KeyEvent.VK_C);
		item.setToolTipText("Layout desktop using the Circo layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("circo");
			}
		});
		menu.add(item);

		item = new JMenuItem("FDP");
		item.setMnemonic(KeyEvent.VK_F);
		item.setToolTipText("Layout desktop using the FDP layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("fdp");
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Neato");
		item.setMnemonic(KeyEvent.VK_N);
		item.setToolTipText("Layout desktop using the neato layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("neato");
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Twopi");
		item.setMnemonic(KeyEvent.VK_T);
		item.setToolTipText("Layout desktop using the twopi layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("twopi");
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Osage");
		item.setMnemonic(KeyEvent.VK_O);
		item.setToolTipText("Layout desktop using the osage layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("osage");
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Patchwork");
		item.setMnemonic(KeyEvent.VK_P);
		item.setToolTipText("Layout desktop using the patchwork layout");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutDOT("patchwork");
			}
		});
		menu.add(item);
		menu.add(new JSeparator());
		
		item = new JMenuItem("Reset user position");
		item.setMnemonic(KeyEvent.VK_N);
		item.setToolTipText("Remove the user defined locations");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				m_rainbowModel.visit(new IRainbowModelVisitor() {
					
					protected void rs(RainbowArchModelElement el) {
						el.setUserLocation(null);
					}
					
					@Override
					public void visitSystem(RainbowSystemModel model) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void visitProbe(RainbowArchProbeModel probe) {
						rs(probe);
					}
					
					@Override
					public void visitModel(RainbowArchModelModel gauge) {
						rs(gauge);
					}
					
					@Override
					public void visitGauge(RainbowArchGaugeModel gauge) {
						rs(gauge);
					}
					
					@Override
					public void visitExecutor(RainbowArchExecutorModel executor) {
						rs(executor);
					}
					
					@Override
					public void visitEffector(RainbowArchEffectorModel effector) {
						rs(effector);
					}
					
					@Override
					public void visitAnalysis(RainbowArchAnalysisModel analysis) {
						rs(analysis);
					}
					
					@Override
					public void visitAdaptationManager(RainbowArchAdapationManagerModel adaptationManager) {
						rs(adaptationManager);
					}
					
					@Override
					public void postVisitSystem(RainbowSystemModel model) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		});
		menu.add(item);
	}

}
