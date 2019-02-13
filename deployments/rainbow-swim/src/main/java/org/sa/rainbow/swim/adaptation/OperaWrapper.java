package org.sa.rainbow.swim.adaptation;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import opera.OperaModel;
import opera.KalmanFilter.EstimationResults;
import opera.KalmanFilter.KalmanConfiguration;
import opera.KalmanFilter.KalmanEstimator;
import Application.Configuration.ConfigurationManager;
import Application.Configuration.MetricMappingsConfigurationSection;
import Framework.Diagnostics.Trace;
import Framework.Monitoring.Data.MetricValues;


public class OperaWrapper {

	private static final String BALANCER = "[Balancer]";
	private static final String WEB_CLUSTER = "WebCluster";
	/**
	 *  Data for the builder.
	 */
	private File m_buildSModelFile = null;
	private File m_buildSFilterFile = null;
	
	OperaModel theModel = null;
	KalmanEstimator theEstimator = null;
	
	// contains metrics that need to be inserted into the model from the monitoring component
	HashMap<String, String> mapMetricsToModel;
	
	HashMap<String, Integer> mapMetricsToFilter;
	
	public OperaWrapper() {
		System.out.println("OperaWrapper()");
	}
	
	public boolean configure(File configFile)
	{
		System.out.println("configure(" + configFile + ")");
		try {
			Trace.WriteLine("trying trace");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("after Trace");
		ConfigurationManager.Configure(configFile.getPath());
		m_buildSModelFile = new File(configFile.getParentFile(), ConfigurationManager.GetSetting("OperaModel"));
		m_buildSFilterFile = new File(configFile.getParentFile(), ConfigurationManager.GetSetting("FilterConfig"));
		System.out.println("OperaModel=" + m_buildSModelFile + " FilterConfig=" + m_buildSFilterFile);
		
		if (false == this.Validate())
		{
			System.out.println("HognaEngine doesn't have all the components it needs. " +
					"Make sure that the model " +
					"is corectly specified and the files exist and are accesible.");
			return false;
		}
		System.out.println("OperaWrapper configured");
		return true;
	}
	
	public void updateModel(int numServers, double avgCpuUtilization,
			double arrivalRate, double responseTime, double throughput,
			double lowArrivalRate, double lowResponseTime, double lowThroughput) {
		MetricValues metricValues = new MetricValues();
		
		theModel.SetNodeMultiplicity("WebHost", numServers);
/*
 * 		<metricsToModel>
			<metric monitorPath="/WebCluster/[Balancer]/Users insert"   modelPath="/Model/Workloads/WorkloadMixes/Mix[@scenario='insert']/@load" />
			<metric monitorPath="/WebCluster/[Balancer]/Users update"   modelPath="/Model/Workloads/WorkloadMixes/Mix[@scenario='update']/@load" />
		</metricsToModel>

		<metricsToFilter>
			<metric monitorPath="/WebCluster/Average/CPUUtilization"            filterIdx="0"/>
			<metric monitorPath="/WebCluster/[Balancer]/Response Time insert"   filterIdx="2"/>
			<metric monitorPath="/WebCluster/[Balancer]/Response Time update"   filterIdx="3"/>
			<metric monitorPath="/WebCluster/[Balancer]/Throughput insert"      filterIdx="8"/>
			<metric monitorPath="/WebCluster/[Balancer]/Throughput update"      filterIdx="9"/>
		</metricsToFilter>
		
 */
		metricValues.AddMetricValue(WEB_CLUSTER, BALANCER, "Users normal", arrivalRate);
		metricValues.AddMetricValue(WEB_CLUSTER, BALANCER, "Users low", lowArrivalRate);
		
		metricValues.AddMetricValue(WEB_CLUSTER, "fakeSingle", "CPUUtilization", avgCpuUtilization);
		metricValues.AddMetricValue(WEB_CLUSTER, BALANCER, "Response Time normal", responseTime);
		metricValues.AddMetricValue(WEB_CLUSTER, BALANCER, "Response Time low", lowResponseTime);
		metricValues.AddMetricValue(WEB_CLUSTER, BALANCER, "Throughput normal", throughput);
		metricValues.AddMetricValue(WEB_CLUSTER, BALANCER, "Throughput low", lowThroughput);
		
		UpdateModel(metricValues);
	}
	
	public double[] getParamEstimates() {
		double[] results = new double[2];
		results[0] = theModel.GetCpuDemand("normal", "WebServer");
		results[1] = theModel.GetCpuDemand("low", "WebServer");
		return results;
	}
	
	/**
	 * Solves the performance model and returns the average response time
	 * 
	 * @param servers
	 * @param arrivalRate 
	 * @param brownoutFactor
	 * @return average response time
	 */
	public double solveModel(int servers, double arrivalRate, double brownoutFactor) {
		if (arrivalRate == 0) {
			return 0.0;
		}
		
		// clone model
		StringWriter modelString = new StringWriter();
		theModel.SaveModel(modelString);
		
		OperaModel tempModel = new OperaModel();
		tempModel.setModelFromString(modelString.toString());
		
		
		tempModel.SetNodeMultiplicity("WebHost", servers);
		tempModel.SetXPathModelNodesValue("/Model/Workloads/WorkloadMixes/Mix[@scenario='normal']/@load",
				String.valueOf(arrivalRate * (1 - brownoutFactor)));
		tempModel.SetXPathModelNodesValue("/Model/Workloads/WorkloadMixes/Mix[@scenario='low']/@load",
				String.valueOf(arrivalRate * brownoutFactor));
		
		double avgResponseTime = Double.MAX_VALUE;
		try {
			tempModel.solve();
			double throughputLow = tempModel.GetThroughput("low");
			double throughputNormal = tempModel.GetThroughput("normal");
			double responseTimeLow = tempModel.GetResponseTimeScenario("low");
			double responseTimeNormal = tempModel.GetResponseTimeScenario("normal");
			
			avgResponseTime = (throughputLow * responseTimeLow + throughputNormal * responseTimeNormal) 
					/ (throughputLow + throughputNormal);
		}
		catch (Exception e)
		{
			System.out.println("OperaWrapper.solve(): " + e.toString());
		}

		return avgResponseTime;
	}
	
	void UpdateModel(MetricValues theMetrics)
	{
		// put the extracted workload into the model
		for (Entry<String, String> entry : this.mapMetricsToModel.entrySet())
		{
			String[] strMetric = entry.getKey().split("/");
			double value = 0;
			if (strMetric[2].equals("Average"))
			{
				value = theMetrics.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				value = theMetrics.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}

			this.theModel.SetXPathModelNodesValue(entry.getValue(), String.valueOf(value));
		}
		
		
		// put the extracted performance metrics into kalman
		double[] trackedMetricValues = new double[this.mapMetricsToFilter.size()];
		for (Entry<String, Integer> entry : this.mapMetricsToFilter.entrySet())
		{
			String[] strMetric = entry.getKey().split("/");
			double value = 0;
			if (strMetric[2].equals("Average"))
			{
				value = theMetrics.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				value = theMetrics.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}

			trackedMetricValues[entry.getValue()] = value;
		}
		

		// run kalman filter to fix the model
		try
		{
			EstimationResults results = this.theEstimator.EstimateModelParameters(trackedMetricValues);
			System.out.println(results.toString());
		}
		catch (Exception e)
		{
			System.out.println("Singular matrix -- cannot calibrate the model (most likely this means there is a problem with the metrics).");
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		// out: a model that is properly calibrated

//		long start = System.currentTimeMillis();
//		theModel.solve();
//		long end = System.currentTimeMillis();
//		System.out.println("solve took " + Long.toString(end - start) + "ms");
		
//		System.out.println("saving models");
//		theModel.SaveModelToXmlFile("operamodel.xml");
//		theModel.SaveResultsToXmlFile("operaresults.xml");
//		System.out.println("saved models");
	}

	private void ValidateModel()
	{
		theModel = new OperaModel();
		theModel.setModel(m_buildSModelFile.getPath());
	}
	
	private void ValidateFilter()
	{
		KalmanConfiguration kalmanConfig = new KalmanConfiguration();
		kalmanConfig.withConfigFile(m_buildSFilterFile.getPath())
					.withModel(theModel)
					.withSetting(KalmanConfiguration.ITERATIONS_MAX, "20");
		theEstimator = new KalmanEstimator(kalmanConfig);
	}
	
	private void ValidateMetricsMappings()
	{
		MetricMappingsConfigurationSection secMetricMappings = (MetricMappingsConfigurationSection)ConfigurationManager.GetSection("metricMappings");
		
		if (secMetricMappings != null)
		{
			this.mapMetricsToModel = secMetricMappings.GetMetricsToModelMappings();
			this.mapMetricsToFilter = secMetricMappings.GetMetricsToFilterMappings();
		}
	}
	
	private boolean Validate()
	{
		// validate the model
		try
		{
			this.ValidateModel();
			this.ValidateFilter();
			this.ValidateMetricsMappings();
		}
		catch (Exception ex)
		{
			// reset the object to un-usable state
			System.out.println("OperaWrapper.Validate():" + ex.toString());
			ex.printStackTrace();
			this.theModel = null;
			this.theEstimator = null;
			throw ex;
		}
		finally
		{
		}
		
		return true;
	}

}
