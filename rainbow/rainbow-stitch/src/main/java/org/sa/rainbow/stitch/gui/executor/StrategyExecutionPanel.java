package org.sa.rainbow.stitch.gui.executor;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.util.Util;

public class StrategyExecutionPanel extends JPanel implements IRainbowModelChangeCallback {

	public static Color bleach(Color color, double amount) {
		int red = (int) ((color.getRed() * (1 - amount) / 255 + amount) * 255);
		int green = (int) ((color.getGreen() * (1 - amount) / 255 + amount) * 255);
		int blue = (int) ((color.getBlue() * (1 - amount) / 255 + amount) * 255);
		return new Color(red, green, blue);
	}

	public static final Color EXECUTING_COLOR = bleach(Color.GREEN, .5);
	public static final Color SETTLING_COLOR = bleach(Color.YELLOW, 0.5);
	public static final Color ERROR_COLOR = bleach(Color.RED, 0.5);

	public class StrategyComboRenderer extends JLabel implements ListCellRenderer<Strategy> {

		public StrategyComboRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Strategy> list, Strategy value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (value == null)
				return this;
			setText(value.getName());

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}

	}

	static public class StrategyInstanceDataRenderer extends JLabel implements ListCellRenderer<StrategyInstanceData> {

		private Font italicFont;
		private Font normalFont;
		private static final Color ERROR_COLOR = Color.RED;
		private static final Color DARK_ERROR_COLOR = new Color(139, 0, 0);

		@Override
		public Component getListCellRendererComponent(JList<? extends StrategyInstanceData> list,
				StrategyInstanceData value, int index, boolean isSelected, boolean cellHasFocus) {
			setOpaque(true);
			setText(value.strategyData.name);
			if (value.currentState != ExecutionStateT.STRATEGY_DONE && value.currentState != ExecutionStateT.NOT_EXECUTING) {
				if (italicFont == null) {
					normalFont = this.getFont();
					italicFont = new Font(this.getFont().getName(), Font.ITALIC, this.getFont().getSize());
				}
				setFont(italicFont);
			} else {
				if (normalFont == null)
					normalFont = this.getFont();
				setFont(normalFont);
			}
			if (value.outcome != null && value.outcome != Outcome.SUCCESS) {
				this.setBackground(isSelected ? DARK_ERROR_COLOR : ERROR_COLOR);
				if (isSelected) this.setForeground(Color.WHITE);
			} else if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}

	}

	/*
	 * Note, this class only works when there is one strategy executor, which is the
	 * normal case in Rainbow.
	 */
	class StrategyData {
		StitchScript script;
		int numberOfRuns = 1;
		int numberOfSuccesses = 0;
		int numberOfFailures = 0;
		String name;

		List<StrategyInstanceData> runs = new LinkedList<StrategyInstanceData>();
		public Strategy strategy;

		public StrategyInstanceData addNewRun() {
			StrategyInstanceData sid = new StrategyInstanceData();
			sid.strategyData = this;
			runs.add(0, sid);
			return sid;
		}

		public StrategyInstanceData getCurrentRun() {
			if (!runs.isEmpty()) {
				return runs.get(0);
			}
			return null;
		}
	}

	class TraceData {
		String label;
		ExecutionStateT state;
	}

	class StrategyInstanceData {
		StrategyData strategyData;
		ExecutionStateT currentState = ExecutionStateT.NOT_EXECUTING;
		List<TraceData> traces = new LinkedList<TraceData>();
		Outcome outcome;

		public void setStatus(ExecutionStateT eventType) {
			currentState = eventType;
		}

		public void addTraceElement(String nodeLabel, ExecutionStateT eventType) {
			TraceData td = new TraceData();
			td.label = nodeLabel;
			td.state = eventType;
			traces.add(0, td);
		}

		public void setTraceStatus(String target, ExecutionStateT eventType) {
			for (TraceData i : traces) {
				if (i.label.equals(target)) {
					i.state = eventType;
					break;
				}
			}
		}

	}

	private JTextField m_numberOfRunsFields;
	private JTextField m_successesField;
	private JTextField m_failuresField;
	private Map<String, StrategyData> m_strategyData = new HashMap<>();
	private JList<StrategyInstanceData> m_strategiesExecuted;

	private final IRainbowChangeBusSubscription m_strategyExecutionSubscriber=new IRainbowChangeBusSubscription(){

	@Override public boolean matches(IRainbowMessage message){String type=(String)message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP);if(type!=null&&ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE.equals(type)){return true;}return false;}

	};
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private DefaultListModel<StrategyInstanceData> m_listModel;
	private Map<String, String> m_tacticToStrategy = new HashMap<>();
	private RSyntaxTextArea m_strategyText;
	protected Map<String, String> m_pathToText = new HashMap<>();
	private JComboBox<Strategy> m_comboBox;
	private JButton m_executeBtn;
	private IRainbowAdaptationEnqueuePort<IEvaluable> m_strategyEnqPort;
	private Color m_defaultHighlightColor;

	/**
	 * Create the panel.
	 */
	public StrategyExecutionPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
//		add(scrollPane, gbc_scrollPane);

		JLabel lblNewLabel = new JLabel("Number of Runs:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);

		m_numberOfRunsFields = new JTextField();
		GridBagConstraints gbc_numberOfRunsFields = new GridBagConstraints();
		gbc_numberOfRunsFields.insets = new Insets(0, 0, 5, 5);
		gbc_numberOfRunsFields.fill = GridBagConstraints.HORIZONTAL;
		gbc_numberOfRunsFields.gridx = 2;
		gbc_numberOfRunsFields.gridy = 0;
		add(m_numberOfRunsFields, gbc_numberOfRunsFields);
		m_numberOfRunsFields.setColumns(10);

		m_strategyText = new RSyntaxTextArea();
		RTextScrollPane sp = new RTextScrollPane(m_strategyText);
		m_strategyText.setCodeFoldingEnabled(true);
		m_strategyText.setSyntaxEditingStyle("text/stitch");
		m_strategyText.setEditable(false);

		m_defaultHighlightColor = m_strategyText.getSelectionColor();

		GridBagConstraints gbc_m_strategyText = new GridBagConstraints();
		gbc_m_strategyText.gridheight = 4;
		gbc_m_strategyText.fill = GridBagConstraints.BOTH;
		gbc_m_strategyText.gridx = 3;
		gbc_m_strategyText.gridy = 0;
		add(sp, gbc_m_strategyText);

		JLabel lblSuccess = new JLabel("Success:");
		GridBagConstraints gbc_lblSuccess = new GridBagConstraints();
		gbc_lblSuccess.anchor = GridBagConstraints.EAST;
		gbc_lblSuccess.insets = new Insets(0, 0, 5, 5);
		gbc_lblSuccess.gridx = 1;
		gbc_lblSuccess.gridy = 1;
		add(lblSuccess, gbc_lblSuccess);

		m_successesField = new JTextField();
		GridBagConstraints gbc_successesField = new GridBagConstraints();
		gbc_successesField.insets = new Insets(0, 0, 5, 5);
		gbc_successesField.fill = GridBagConstraints.HORIZONTAL;
		gbc_successesField.gridx = 2;
		gbc_successesField.gridy = 1;
		add(m_successesField, gbc_successesField);
		m_successesField.setColumns(10);

		JLabel lblFailures = new JLabel("Failures:");
		GridBagConstraints gbc_lblFailures = new GridBagConstraints();
		gbc_lblFailures.anchor = GridBagConstraints.EAST;
		gbc_lblFailures.insets = new Insets(0, 0, 5, 5);
		gbc_lblFailures.gridx = 1;
		gbc_lblFailures.gridy = 2;
		add(lblFailures, gbc_lblFailures);

		m_failuresField = new JTextField();
		GridBagConstraints gbc_failuresField = new GridBagConstraints();
		gbc_failuresField.insets = new Insets(0, 0, 5, 5);
		gbc_failuresField.fill = GridBagConstraints.HORIZONTAL;
		gbc_failuresField.gridx = 2;
		gbc_failuresField.gridy = 2;
		add(m_failuresField, gbc_failuresField);
		m_failuresField.setColumns(10);

		m_numberOfRunsFields.setEnabled(false);
		m_failuresField.setEnabled(false);
		m_successesField.setEnabled(false);

		m_strategiesExecuted = new JList<>();
		m_listModel = new DefaultListModel<>();
		m_strategiesExecuted.setModel(m_listModel);
//		scrollPane.setViewportView(m_strategiesExecuted);
		add(m_strategiesExecuted, gbc_scrollPane);

		try {
			m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
			m_modelChangePort.subscribe(m_strategyExecutionSubscriber, this);
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_strategiesExecuted.setCellRenderer(new StrategyInstanceDataRenderer());
		m_strategiesExecuted.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		m_executeBtn = new JButton("Execute:");
		m_executeBtn.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_label.gridx = 1;
		gbc_label.gridy = 3;
		add(m_executeBtn, gbc_label);

		m_comboBox = new JComboBox<Strategy>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.NORTH;
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 3;
		add(m_comboBox, gbc_comboBox);

		m_strategyEnqPort = RainbowPortFactory.createAdaptationEnqueuePort(new ModelReference("SwimSys", "Acme"));

		m_comboBox.setRenderer(new StrategyComboRenderer());
		m_executeBtn.addActionListener((e) -> {
			Strategy s = (Strategy) m_comboBox.getSelectedItem();
			m_strategyEnqPort.offerAdaptation(new AdaptationTree<IEvaluable>(s), new Object[0]);
		});

		m_strategiesExecuted.addListSelectionListener((e) -> {
			JList ls = (JList) e.getSource();

			if (!ls.isSelectionEmpty() && !e.getValueIsAdjusting()) {
				StrategyInstanceData sid = (StrategyInstanceData) m_strategiesExecuted.getSelectedValue();
				m_numberOfRunsFields.setText(Integer.toString(sid.strategyData.numberOfRuns));
				m_failuresField.setText(Integer.toString(sid.strategyData.numberOfFailures));
				m_successesField.setText(Integer.toString(sid.strategyData.numberOfSuccesses));
				updateStrategyText(sid);
			} else {
				m_numberOfRunsFields.setText("");
				m_failuresField.setText("");
				m_successesField.setText("");
				m_strategyText.setText("");
			}
		});

	}

	protected void updateStrategyText(StrategyInstanceData sid) {
		String path = sid.strategyData.strategy.m_stitch.path;
		String stitchText = m_pathToText.get(path);
		if (stitchText == null) {
			try {
				File stitchPath = Util.getRelativeToPath(Rainbow.instance().getTargetPath(),
						Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCRIPT_PATH));
				stitchText = new String(Files.readAllBytes(new File(path).toPath()));
				m_pathToText.put(path, stitchText);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		m_strategyText.setText(stitchText);
		
		Pattern p = Pattern.compile("strategy.*" + sid.strategyData.name);
		Matcher m = p.matcher(stitchText);
		if (m.find()) {
			final int location = m.start();
			m_strategyText.setCaretPosition(location);
			m_strategyText.setSelectionColor(m_defaultHighlightColor);
			
			EventQueue.invokeLater(() -> {
				try {
					Rectangle vr = m_strategyText.modelToView(location);
					int componentHeight = getVisibleRect().height;
					int loc = location;
					m_strategyText.scrollRectToVisible(new Rectangle (vr.x, vr.y, vr.width,componentHeight));
					int lineOfOffset = m_strategyText.getLineOfOffset(location);
//							((JViewport) m_strategyText.getParent()).setViewPosition(
//									new Point(0, m_strategyText.yForLine(lineOfOffset)));
					Color highlightColor = Color.LIGHT_GRAY;
					m_strategyText.removeAllLineHighlights();
					m_strategyText.addLineHighlight(lineOfOffset, highlightColor);
					for (TraceData trace : sid.traces) {
						Pattern pa = Pattern.compile(trace.label + "\\s*:");
						if (trace.state == ExecutionStateT.TACTIC_EXECUTING
								|| trace.state == ExecutionStateT.TACTIC_SETTLING) {
							pa = Pattern.compile(trace.label + "\\s*\\(");
						}
						Matcher ma = pa.matcher(m_strategyText.getText());
						if (ma.find(loc)) {
							loc = ma.start();
							switch (trace.state) {
							case NODE_EXECUTING:
							case NODE_DONE:
								highlightColor = EXECUTING_COLOR;
								m_strategyText.addLineHighlight(m_strategyText.getLineOfOffset(ma.start()),
										highlightColor);
								break;
							case STRATEGY_EXECUTING:
							case STRATEGY_DONE:
								highlightColor = EXECUTING_COLOR;
								m_strategyText.addLineHighlight(m_strategyText.getLineOfOffset(ma.start()),
										highlightColor);
								break;
							case STRATEGY_SETTLING:
								highlightColor = SETTLING_COLOR;
								m_strategyText.addLineHighlight(m_strategyText.getLineOfOffset(ma.start()),
										highlightColor);
								break;
							case TACTIC_EXECUTING:
//								m_strategyText.setSelectionColor(EXECUTING_COLOR);
								m_strategyText.requestFocusInWindow();
								m_strategyText.setCaretPosition(ma.start());
								m_strategyText.moveCaretPosition(ma.start() + trace.label.length());
								break;
							case TACTIC_SETTLING:
								m_strategyText.setSelectionColor(SETTLING_COLOR);
								m_strategyText.replaceRange(trace.label + "\029D6", m.start(), m.start() + trace.label.length());
								m_strategyText.requestFocusInWindow();
								m_strategyText.setCaretPosition(ma.start());
								m_strategyText.moveCaretPosition(ma.start() + trace.label.length());
								break;
							case TACTIC_DONE:
								m_strategyText.setCaretPosition(ma.start());
								m_strategyText.replaceRange(trace.label, m.start(), m.start() + trace.label.length()+1);

								break;
							}

						}
					}

				
				} catch (BadLocationException e) {
				}
			});

		}
	}


	public void initBinding(RainbowArchExecutorModel model) {
		ArrayList<Strategy> sArray = new ArrayList<>();
		for (Stitch s : Ohana.instance().listStitches()) {
			for (Strategy t : s.script.strategies) {
				sArray.add(t);
			}
		}

		DefaultComboBoxModel<Strategy> sModel = new DefaultComboBoxModel<>(sArray.toArray(new Strategy[0]));
		m_comboBox.setModel(sModel);
	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		ExecutionStateT eventType = ExecutionStateT
				.valueOf((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "2"));
		String target = (String) message.getProperty(IModelChangeBusPort.TARGET_PROP);
		String[] targetConstituents = target.split("\\.");
		switch (eventType) {
		case STRATEGY_EXECUTING: {
			String strategyName = targetConstituents[targetConstituents.length - 1];

			StrategyData sd = getStrategyData(strategyName);
			sd.numberOfRuns++;
			StrategyInstanceData sid = sd.addNewRun();
			sid.setStatus(eventType);
			m_listModel.addElement(sid);
			m_strategiesExecuted.setModel(m_listModel);
			break;
		}
		case STRATEGY_SETTLING: {
			String nodeLabel = targetConstituents[targetConstituents.length - 1];
			String strategyName = targetConstituents[targetConstituents.length - 2];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(nodeLabel, eventType);
			m_strategiesExecuted.repaint();
			break;
		}
		case STRATEGY_DONE: {
			String strategyName = targetConstituents[targetConstituents.length - 1];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setStatus(eventType);
			Outcome outcome = Outcome.valueOf((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "3"));
			sd.getCurrentRun().outcome = outcome;
			switch (outcome) {
			case SUCCESS:
				sd.numberOfSuccesses++;
				break;
			case FAILURE:
				sd.numberOfFailures++;
				break;
			}

			m_strategiesExecuted.repaint();
			break;
		}
		case NODE_EXECUTING: {
			String nodeLabel = targetConstituents[targetConstituents.length - 1];
			String strategyName = targetConstituents[targetConstituents.length - 2];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().addTraceElement(nodeLabel, eventType);
			m_strategiesExecuted.repaint();
			if (m_comboBox.getSelectedItem() == sd.strategy)
				updateStrategyText(sd.getCurrentRun());
			break;
		}
		case TACTIC_EXECUTING: {
			String tacticName = targetConstituents[targetConstituents.length - 1];
			targetConstituents = ((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "3")).split("\\.");
			String strategyName = targetConstituents[targetConstituents.length - 1];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().addTraceElement(tacticName, eventType);
			m_tacticToStrategy.put(tacticName, strategyName);
			if (m_comboBox.getSelectedItem() == sd.strategy)
				updateStrategyText(sd.getCurrentRun());
			break;
		}
		case TACTIC_SETTLING: {
			String tacticName = targetConstituents[targetConstituents.length - 1];

			String strategyName = m_tacticToStrategy.get(tacticName);
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(tacticName, eventType);
			if (m_comboBox.getSelectedItem() == sd.strategy)
				updateStrategyText(sd.getCurrentRun());
			break;
		}
		case TACTIC_DONE: {
			String tacticName = targetConstituents[targetConstituents.length - 1];
			String strategyName = m_tacticToStrategy.get(tacticName);
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(tacticName, eventType);
			m_tacticToStrategy.remove(tacticName);
			if (m_comboBox.getSelectedItem() == sd.strategy)
				updateStrategyText(sd.getCurrentRun());
			break;
		}
		case NODE_DONE: {
			String strategyName = targetConstituents[targetConstituents.length - 2];
			String nodeLabel = targetConstituents[targetConstituents.length - 1];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(nodeLabel, eventType);
			m_strategiesExecuted.repaint();
			if (m_comboBox.getSelectedItem() == sd.strategy)
				updateStrategyText(sd.getCurrentRun());

			break;
		}
		}
	}

	private StrategyData getStrategyData(String strategyName) {
		StrategyData strategyData = m_strategyData.get(strategyName);
		if (strategyData == null) {
			strategyData = new StrategyData();
			strategyData.name = strategyName;
			m_strategyData.put(strategyName, strategyData);
			List<Stitch> stitches = Ohana.instance().listStitches();
			OUTER: for (Stitch stitch : stitches) {
				List<Strategy> strategies = stitch.script.strategies;
				for (Strategy s : strategies) {
					if (s.getName().equals(strategyName)) {
						strategyData.script = stitch.script;
						strategyData.strategy = s;
						break OUTER;
					}
				}
			}
		}

		return strategyData;

	}

}
