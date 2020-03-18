package org.sa.rainbow.gui.utility;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.acmestudio.acme.element.IAcmeSystem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindoe.SelectionManager;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.utility.UtilityModelInstance;

public class UtilityModelController extends org.sa.rainbow.gui.arch.controller.RainbowModelController {

	private static final String UTILITY_KW = "utility";
	private Map<String, Object> m_configParams;
	private IModelsManagerPort m_modelPort;
	private AcmeModelInstance m_acme;

	private int m_sampleWindow = 100;

	protected Map<String, TimeSeries> m_valueSeries = new HashMap<>();
	protected Map<String, TimeSeries> m_utilitySeries = new HashMap<>();
	protected Map<String, ChartPanel> m_panels = new HashMap<>();

	private UtilityPreferenceDescription m_utilityModel;
	private Thread m_updateThread;
	private Object m_object;
	private boolean m_running = true;

	public UtilityModelController(RainbowArchModelModel model, SelectionManager m) {
		super(model, m);
	}

	@Override
	public void configure(Map<String, Object> configs) {
		m_configParams = configs;
	}

	@Override
	public JInternalFrame createView(JDesktopPane parent) {
		JInternalFrame frame = new JInternalFrame(getModel().getModelRef().getModelName(), true, false, false);
		frame.setVisible(true);
		try {
			m_modelPort = RainbowPortFactory.createModelsManagerRequirerPort();
			UtilityModelInstance modelInstance = (UtilityModelInstance) m_modelPort
					.<UtilityPreferenceDescription>getModelInstance(getModel().getModelRef());
			m_utilityModel = modelInstance.getModelInstance();
			m_acme = (AcmeModelInstance) m_modelPort
					.<IAcmeSystem>getModelInstance(new ModelReference(getModel().getModelRef().getModelName(), "Acme"));
			Collection<String> utilityKeys = m_utilityModel.getUtilities().keySet();
			JPanel grid = new JPanel();

			int rows = (int) Math.round(Math.sqrt(utilityKeys.size()+1));
			int columns = (int) Math.ceil(Math.sqrt(utilityKeys.size()+1));
			grid.setLayout(new GridLayout(rows, columns));
			Iterator<String> utility = utilityKeys.iterator();
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < columns; c++) {
					if (utility.hasNext()) {
						String u = utility.next();
						Map<String, Object> config = (Map<String, Object>) m_configParams.get(u);
						double upper = 10.0;
						double lower = 0.0;
						if (config.containsKey("upper")) {
							upper = extractDouble(config.get("upper"));
						}
						if (config.containsKey("lower"))
							lower = extractDouble(config.get("lower"));
						TimeSeries series = new TimeSeries(u);
						m_valueSeries.put(u, series);
						TimeSeriesCollection dataset = new TimeSeriesCollection(series);
						TimeSeries utilitySeries = new TimeSeries(UtilityModelController.UTILITY_KW);
						TimeSeriesCollection udataset = new TimeSeriesCollection(utilitySeries);
						m_utilitySeries.put(u, utilitySeries);

						JFreeChart chart = ChartFactory.createTimeSeriesChart(u, null, null, null, false, true,
								false);
						XYPlot xyPlot = chart.getXYPlot();

						NumberAxis vAxis = new NumberAxis();
						xyPlot.setRangeAxis(0, vAxis);
						xyPlot.getRangeAxis(0).setVisible(true);
						xyPlot.getRangeAxis(0).setRange(new Range(lower, upper));
						xyPlot.setDataset(0, dataset);

						NumberAxis uAxis = new NumberAxis();
						xyPlot.setRangeAxis(1, uAxis);
						xyPlot.getRangeAxis(1).setVisible(true);
						xyPlot.getRangeAxis(1).setRange(new Range(0, 1));
						xyPlot.setDataset(1, udataset);

						XYLineAndShapeRenderer r0 = new XYLineAndShapeRenderer();
						XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
						xyPlot.setRenderer(0, r0);
						xyPlot.setRenderer(1, r1);
						
						xyPlot.getRenderer(0).setSeriesStroke(0, new BasicStroke(3.0f));
						xyPlot.getRenderer(1).setSeriesStroke(0, new BasicStroke(3.0f));
						xyPlot.getRenderer(0).setSeriesPaint(0, Color.black);
						xyPlot.getRenderer(1).setSeriesPaint(0, Color.green);
						xyPlot.mapDatasetToRangeAxis(0, 0);
						xyPlot.mapDatasetToRangeAxis(1, 1);

//						xyPlot.getRangeAxis().setVisible(false);
//						xyPlot.getRangeAxis().setRange(new Range(lower, upper));
						xyPlot.getDomainAxis().setVisible(false);
						ChartPanel panel = new ChartPanel(chart);
						m_panels.put(u, panel);
						panel.setMinimumSize(new Dimension(80, 50));
						panel.setSize(100, 50);
						grid.add(panel);
					}
				}
			}
			TimeSeries utilitySeries = new TimeSeries(UtilityModelController.UTILITY_KW);
			TimeSeriesCollection udataset = new TimeSeriesCollection(utilitySeries);
			m_utilitySeries.put(UtilityModelController.UTILITY_KW, utilitySeries);
			
			JFreeChart chart = ChartFactory.createTimeSeriesChart(UtilityModelController.UTILITY_KW, null, null, null, false, true,
					false);
			XYPlot xyPlot = chart.getXYPlot();
			NumberAxis vAxis = new NumberAxis();
			xyPlot.setRangeAxis(0, vAxis);
			xyPlot.getRangeAxis(0).setVisible(true);
			xyPlot.getRangeAxis(0).setRange(new Range(0, 1));
			xyPlot.setDataset(0, udataset);
			xyPlot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
			xyPlot.getRenderer().setSeriesPaint(0, Color.green);
			frame.setSize(100 * columns, 100 * rows);
			ChartPanel panel = new ChartPanel(chart);
			m_panels.put(UtilityModelController.UTILITY_KW, panel);
			panel.setMinimumSize(new Dimension(80, 50));
			panel.setSize(100, 50);
			grid.add(panel);
			frame.add(grid);
		} catch (RainbowException e) {
		}
		m_frame = frame;
		parent.add(frame);
		attachControllerToFrame(frame);

		m_updateThread = new Thread(target);
		m_updateThread.start();
		return m_frame;
	}
	
	@Override
	public void processReport(ReportType type, String message) {
		
	}

	protected double extractDouble(Object param) {
		return param instanceof Number?((Number )param).doubleValue():param instanceof String?Double.parseDouble((String )param):0.0;
	}

	Runnable target = () -> {
		String scenario = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCENARIO);
		Map<String, Double> weights = m_utilityModel.weights.get(scenario);
		while (m_running) {
			if (m_frame.isVisible()) {
				double currentUtility = 0.0;
				Second tick = new Second();
				for (Map.Entry<String, TimeSeries> kv : m_valueSeries.entrySet()) {
					if (kv.getValue().getItemCount() >= m_sampleWindow) {
						kv.getValue().delete(0, 0, false);
					}
					Object property = m_acme.getProperty(m_utilityModel.getUtilities().get(kv.getKey()).mapping);
					Double value = (property instanceof Number) ? ((Number) property).doubleValue() : 0.0;
					kv.getValue().add(tick, value, true);
					if (m_utilitySeries.get(kv.getKey()).getItemCount() >= m_sampleWindow) {
						m_utilitySeries.get(kv.getKey()).delete(0, 0, false);
					}
					double f = m_utilityModel.getUtilityFunctions().get(kv.getKey()).f(value);
					m_utilitySeries.get(kv.getKey()).add(tick, f);
					m_panels.get(kv.getKey()).setToolTipText(MessageFormat.format("<html><b>{0}</b>: {1,number,#.##}<br/><b>{2}</b>: {3,number,##.##}</html>", kv.getKey(), value, "Utility", f));
					currentUtility += weights.get(kv.getKey()) * f;
				}
				
				TimeSeries utilitySeries = m_utilitySeries.get(UtilityModelController.UTILITY_KW);
				if (utilitySeries.getItemCount() >= m_sampleWindow) {
					utilitySeries.delete(0,0, false);
				}
				utilitySeries.add(tick, currentUtility);
				m_panels.get(UTILITY_KW).setToolTipText(MessageFormat.format("<html><b>System Utility: </b>: {1,number,#.##}</html>", currentUtility));
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

}
