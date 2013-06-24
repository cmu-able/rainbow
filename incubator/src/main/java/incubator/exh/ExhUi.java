package incubator.exh;

import incubator.obscol.ObservableList;
import incubator.obscol.WrapperObservableList;
import incubator.pval.Ensure;
import incubator.scb.ScbAggregateContainer;
import incubator.scb.ui.ScbTable;
import incubator.scb.ui.ScbTableScrollable;
import incubator.ui.AutoUpdateJComboBox;
import incubator.ui.AutoUpdateJComboBox.NullSupport;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.renderer.StringValue;

/**
 * GUI component showing information of the <code>exh</code> package. It
 * shows one or more collectors and allows browsing the exceptions.
 */
@SuppressWarnings("serial")
public class ExhUi extends JPanel {
	/**
	 * All collectors, sorted by name. <code>null</code> is at the top
	 * meaning all collectors.
	 */
	private ObservableList<ThrowableCollector> m_collectors;
	
	/**
	 * Throwable selector user interface.
	 */
	private AutoUpdateJComboBox<ThrowableCollector> m_selector_ui;
	
	/**
	 * Model with throwable contexts.
	 */
	private ThrowableContextScbTableModel m_model;
	
	/**
	 * The SCB aggregator.
	 */
	private ScbAggregateContainer<ThrowableContext> m_aggregator;
	
	/**
	 * The SCB table.
	 */
	private ScbTable<ThrowableContext> m_table;
	
	/**
	 * The trace area.
	 */
	private JTextArea m_trace;
	
	/**
	 * Creates a new user interface.
	 */
	public ExhUi() {
		m_collectors = new WrapperObservableList<>(
				new ArrayList<ThrowableCollector>());
		m_selector_ui = new AutoUpdateJComboBox<>(m_collectors,
				new StringValue() {
			@Override
			public String getString(Object val) {
				return ((ThrowableCollector) val).name();
			}
		});
		
		m_selector_ui.setNullSupport(NullSupport.NULL_AT_BEGINING, "<All>");
		m_selector_ui.setSelectedIndex(0);
		
		m_aggregator = new ScbAggregateContainer<>();
		m_model = new ThrowableContextScbTableModel(m_aggregator);
		
		ScbTableScrollable<ThrowableContext> scroll =
				new ScbTableScrollable<>(m_model);
		m_table = scroll.table();
		
		m_trace = new JTextArea();
		m_trace.setLineWrap(true);
		m_trace.setWrapStyleWord(true);
		m_trace.setEditable(false);
		
		/*
		 * Build the UI.
		 */
		setLayout(new BorderLayout());
		
		/*
		 * Build the top panel.
		 */
		JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(top_panel, BorderLayout.NORTH);
		
		top_panel.add(new JLabel("Collector:"));
		top_panel.add(m_selector_ui);
		
		/*
		 * Build the center panel with the divider.
		 */
		JScrollPane trace_scroll = new JScrollPane();
		trace_scroll.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		trace_scroll.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		trace_scroll.setViewportView(m_trace);
		
		JSplitPane central_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		central_split.add(scroll);
		central_split.add(trace_scroll);
		add(central_split, BorderLayout.CENTER);
		central_split.setDividerLocation(0.5);
		
		/*
		 * Add event listeners.
		 */
		m_selector_ui.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				update_collector_selection();
			}
		});
		
		m_table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				update_current_trace();
			}
		});
	}
	
	/**
	 * Adds a collector to the user interface.
	 * @param collector the collector to add; if the collector is already
	 * in the user interface, this call is ignored
	 */
	public void add_collector(ThrowableCollector collector) {
		Ensure.notNull(collector);
		if (m_collectors.contains(collector)) {
			return;
		}
		
		int idx;
		for (idx = 0; idx < m_collectors.size()
				&& m_collectors.get(idx).name().compareTo(collector.name())
				<= 0; idx++) ;
		m_collectors.add(idx, collector);
		m_aggregator.add_container(collector);
	}
	
	/**
	 * Removes a previously added collector from the user interface.
	 * @param collector the collector to remove
	 */
	public void remove_collector(ThrowableCollector collector) {
		Ensure.notNull(collector);
		Ensure.stateCondition(m_collectors.remove(collector) == true);
		m_aggregator.remove_container(collector);
	}
	
	/**
	 * Invoked when the combo box selection may have changed.
	 */
	private void update_collector_selection() {
		ThrowableCollector sel = m_selector_ui.getSelected();
		if (sel != null) {
			m_model.switch_container(sel);
		} else {
			m_model.switch_container(m_aggregator);
		}
	}
	
	/**
	 * Invoked when the table's selection may have changed.
	 */
	private void update_current_trace() {
		ThrowableContext ctx = m_table.selected();
		String trace;
		if (ctx == null) {
			trace = "";
		} else {
			StringWriter sw = new StringWriter();
			ctx.throwable().printStackTrace(new PrintWriter(sw));
			trace = sw.toString();
		}
		
		m_trace.setText(trace);
	}
}
