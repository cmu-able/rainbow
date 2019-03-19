package org.sa.rainbow.gui.widgets;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class TimeSeriesPanel extends JPanel {
	
	public static interface ICommandProcessor {
		double process(IRainbowOperation command);
	}

	private XYDataset m_dataset;
	private ICommandProcessor m_processor;
	private TimeSeries m_series;
	private int m_sampleWindow = 100;
	
	public TimeSeriesPanel(String xLabel, String yLabel, ICommandProcessor processor) {
		m_processor = processor;
		setLayout(new BorderLayout(0, 0));

		m_dataset = createDataSet();
		JFreeChart chart = ChartFactory.createTimeSeriesChart("", xLabel, yLabel, m_dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
	}

	public void setSampleWindow(int i) {
		m_sampleWindow = i;
	}
	
	private XYDataset createDataSet() {
		m_series = new TimeSeries("");
		
		return new TimeSeriesCollection(m_series);
	}
	
	public void newCommand(IRainbowOperation cmd) {
		if (m_series.getItemCount() >= m_sampleWindow)
			m_series.delete(0, 0, false);
		m_series.add(new Second(), m_processor.process(cmd), true);
		
	}
}
