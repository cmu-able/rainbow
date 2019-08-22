package org.sa.rainbow.gui.widgets;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.Range;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class TimeSeriesPanel extends ChartPanelContainer implements ICommandUpdate {
	
	public static interface ICommandProcessor<T> {
		T process(IRainbowOperation command);
	}

	private XYDataset m_dataset;
	private ICommandProcessor<Double> m_processor;
	private TimeSeries m_series;
	private int m_sampleWindow = 100;
	private JFreeChart m_chart;
	private Double m_upper;
	private Double m_lower;
	public TimeSeriesPanel(String xLabel, String yLabel, Double upper, Double lower, ICommandProcessor<Double> processor) {
		m_upper = upper;
		m_lower = lower;
		m_processor = processor;
		setLayout(new BorderLayout(0, 0));

		m_dataset = createDataSet();
		m_chart = ChartFactory.createTimeSeriesChart(null, null, null, m_dataset, false, false, false);
		m_chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(6.0f));
		m_chart.getXYPlot().getRangeAxis().setVisible(false);
		m_chart.getXYPlot().getRangeAxis().setRange(new Range(lower, upper));
		m_chart.getXYPlot().getDomainAxis().setVisible(false);
		m_chartPanel = new ChartPanel(m_chart);
		m_chartPanel.setMinimumSize(new Dimension(80, 50));
		m_chartPanel.setSize(100, 50);
		add(m_chartPanel, BorderLayout.CENTER);
	}

	public void setSampleWindow(int i) {
		m_sampleWindow = i;
		m_series.setMaximumItemCount(i);
//		m_chart.getXYPlot().getDomainAxis().setRange(0,m_sampleWindow);
//		m_chart.getXYPlot().getDomainAxis().setAutoRange(false);
	}
	
	private XYDataset createDataSet() {
		m_series = new TimeSeries("");
//		if (m_upper != null) m_series.getsetMaxY(m_upper);
//		if (m_lower != null) m_series.setMinY(m_upper);
		return new TimeSeriesCollection(m_series);
	}
	
	@Override
	public void newCommand(IRainbowOperation cmd) {
		if (m_series.getItemCount() >= m_sampleWindow)
			m_series.delete(0, 0, false);
		m_series.add(new Second(), m_processor.process(cmd), true);
		
	}
}
