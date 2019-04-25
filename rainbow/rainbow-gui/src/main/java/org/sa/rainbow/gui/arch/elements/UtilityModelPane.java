package org.sa.rainbow.gui.arch.elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;

public class UtilityModelPane extends JPanel {
	public class UtilityFunctionListRenderer extends JLabel implements ListCellRenderer<UtilityFunction> {

		public UtilityFunctionListRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends UtilityFunction> list, UtilityFunction value,
				int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.label());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}

	}

	private JTextField m_funcLabel;
	private JTextField m_mappingText;
	private UtilityPreferenceDescription m_upd;
	private JList m_utilityFunctions;
	private JTextArea m_descriptionText;
	private JPanel m_graphPanel;

	/**
	 * Create the panel.
	 */
	public UtilityModelPane() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{150, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 75, 0};
		gridBagLayout.columnWeights = new double[]{0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblUtilityFunctions = new JLabel("Utility Functions:");
		GridBagConstraints gbc_lblUtilityFunctions = new GridBagConstraints();
		gbc_lblUtilityFunctions.anchor = GridBagConstraints.EAST;
		gbc_lblUtilityFunctions.insets = new Insets(0, 0, 5, 5);
		gbc_lblUtilityFunctions.gridx = 0;
		gbc_lblUtilityFunctions.gridy = 0;
		add(lblUtilityFunctions, gbc_lblUtilityFunctions);
		
		m_utilityFunctions = new JList();
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.gridheight = 3;
		gbc_list.insets = new Insets(0, 0, 0, 5);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 1;
		add(m_utilityFunctions, gbc_list);
		m_utilityFunctions.setCellRenderer(new UtilityFunctionListRenderer());
		
		JLabel lblLabel = new JLabel("Label:");
		lblLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblLabel = new GridBagConstraints();
		gbc_lblLabel.anchor = GridBagConstraints.EAST;
		gbc_lblLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblLabel.gridx = 1;
		gbc_lblLabel.gridy = 1;
		add(lblLabel, gbc_lblLabel);
		
		m_funcLabel = new JTextField();
		m_funcLabel.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 1;
		add(m_funcLabel, gbc_textField);
		m_funcLabel.setColumns(50);
		
		JLabel lblMapping = new JLabel("Mapping:");
		lblMapping.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblMapping = new GridBagConstraints();
		gbc_lblMapping.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblMapping.insets = new Insets(0, 0, 5, 5);
		gbc_lblMapping.gridx = 1;
		gbc_lblMapping.gridy = 2;
		add(lblMapping, gbc_lblMapping);
		
		m_mappingText = new JTextField();
		m_mappingText.setEditable(false);
		GridBagConstraints gbc_mappingText = new GridBagConstraints();
		gbc_mappingText.anchor = GridBagConstraints.NORTH;
		gbc_mappingText.insets = new Insets(0, 0, 5, 5);
		gbc_mappingText.fill = GridBagConstraints.HORIZONTAL;
		gbc_mappingText.gridx = 2;
		gbc_mappingText.gridy = 2;
		add(m_mappingText, gbc_mappingText);
		m_mappingText.setColumns(10);
		
		
		
		JLabel lblDescription = new JLabel("Description:");
		lblDescription.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDescription.insets = new Insets(0, 0, 0, 5);
		gbc_lblDescription.gridx = 1;
		gbc_lblDescription.gridy = 3;
		add(lblDescription, gbc_lblDescription);
		
		m_descriptionText = new JTextArea();
		m_descriptionText.setEditable(false);
		GridBagConstraints gbc_descriptionText = new GridBagConstraints();
		gbc_descriptionText.insets = new Insets(0, 0, 0, 5);
		gbc_descriptionText.fill = GridBagConstraints.BOTH;
		gbc_descriptionText.gridx = 2;
		gbc_descriptionText.gridy = 3;
		add(m_descriptionText, gbc_descriptionText);
		
		JLabel lblFunction = new JLabel("Function:");
		lblFunction.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblFunction = new GridBagConstraints();
		gbc_lblFunction.insets = new Insets(0, 0, 5, 0);
		gbc_lblFunction.anchor = GridBagConstraints.WEST;
		gbc_lblFunction.gridx = 4;
		gbc_lblFunction.gridy = 1;
		add(lblFunction, gbc_lblFunction);
		
		
		
	}
	
	public void initBindings(UtilityPreferenceDescription upd) {
		m_upd = upd;
		m_utilityFunctions.removeAll();
		DefaultListModel<Object> model = new DefaultListModel<>();
		Map<String, UtilityFunction> utilityFunctions = upd.getUtilityFunctions();
		for (UtilityFunction function : utilityFunctions.values()) {
			model.addElement(function);
			
		}
		m_utilityFunctions.setModel(model);
		m_utilityFunctions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_utilityFunctions.addListSelectionListener(e->{
			if (!e.getValueIsAdjusting()) {
				UtilityFunction f = (UtilityFunction )m_utilityFunctions.getSelectedValue();
				m_funcLabel.setText(f.label());
				m_descriptionText.setText(f.description());
				m_mappingText.setText(f.mapping());
				if (m_graphPanel != null) m_graphPanel.getParent().remove(m_graphPanel);
				XYSeries series = new XYSeries("Function");
				for (Entry<Double,Double> xy : f.values().entrySet()) {
					series.add(xy.getKey(), xy.getValue());
				}
				XYSeriesCollection data = new XYSeriesCollection(series);
				JFreeChart chart = ChartFactory.createXYLineChart(f.label(), "X", "Utility", data,PlotOrientation.VERTICAL, false, true, false);
				m_graphPanel = new ChartPanel(chart);
				m_graphPanel.setSize(new Dimension(175,175));
				m_graphPanel.setMinimumSize(new Dimension(175,175));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.gridheight = 2;
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 4;
				gbc_panel.gridy = 2;
				add(m_graphPanel, gbc_panel);
			}
		});
		
		
	}

}
