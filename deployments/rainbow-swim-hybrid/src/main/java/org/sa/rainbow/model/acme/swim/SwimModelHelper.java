package org.sa.rainbow.model.acme.swim;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.lib.SwimUtils;

import pladapt.GenericConfiguration;

/**
 * Helper to query SWIM ACME model
 * 
 * @author gmoreno
 */
public class SwimModelHelper {
  private static final String LOAD_BALANCER = "LB0";
	public static final String CONFIG_ADD_SERVER_PROGRESS = "addServerProgress";
	public static final String CONFIG_DIMMER = "d";
	public static final String CONFIG_SERVERS = "s";
	public static final String ENV_ARRIVAL_RATE_MEAN = "m"; // as defined in pladapt.i (libadaptmgr)
	public static final String ENV_ARRIVAL_RATE_VARIANCE = "v"; // as defined in pladapt.i (libadaptmgr)
	//Might need to duplicate, look more into syntax
  private static final String PROP_NUM_SERVERS = "[EXPR]size(/self/components:ServerT[isArchEnabled])";
	private static final String PROP_NUM_ACTIVE_SERVERS = "[EXPR]size(/self/components:ServerT[isArchEnabled and isActive])";
	private static final String PROP_MAX_SERVERS = "[EXPR]size(/self/components:ServerT)";
	private static final String PROP_MAX_SERVER_ACTIVATION_TIME = "[Max]ServerT.expectedActivationTime";
	private static final String PROP_ARRIVAL_RATE = "arrivalRate";
	private static final String PROP_AVG_TRAFFIC = "[EXPR]seqAverage(</self/components:ServerT[isArchEnabled and isActive]/traffic>)";

	private AcmeModelInstance m_model = null;
	private double m_estimatedBasicServiceTime;
	private double m_estimatedBasicServiceTimeVariance;
	private double m_estimatedOptServiceTime;
	private double m_estimatedOptServiceTimeVariance;
   
	public double getEstimatedBasicServiceTime() {
		return m_estimatedBasicServiceTime;
	}

	public void setEstimatedBasicServiceTime(double mean, double variance) {
		m_estimatedBasicServiceTime = mean;
		m_estimatedBasicServiceTimeVariance = variance;
	}

	public double getEstimatedOptServiceTime() {
		return m_estimatedOptServiceTime;
	}

	public void setEstimatedOptServiceTime(double mean, double variance) {
		m_estimatedOptServiceTime = mean;
		m_estimatedOptServiceTimeVariance = variance;
	}

	public double getEstimatedBasicServiceTimeVariance() {
		return m_estimatedBasicServiceTimeVariance;
	}

	public double getEstimatedOptServiceTimeVariance() {
		return m_estimatedOptServiceTimeVariance;
	}

	public SwimModelHelper(AcmeModelInstance model) {
		m_model = model;
	}
	
    public double getDoubleProperty(String expr, double defaultValue) {
    	double val = defaultValue;
    	Object propValue = m_model.getProperty(expr);
        if (propValue != null) {
        	if (propValue instanceof Float) {
        		val = ((Float) propValue).doubleValue();
        	} else {
        		val = ((Double) propValue).doubleValue();
        	}
        }
        return val; 
    }

    public int getIntegerProperty(String expr, int defaultValue) {
    	int val = defaultValue;
    	Integer propValue = (Integer) m_model.getProperty(expr);
        if (propValue != null) { 
        	val = propValue.intValue();
        }
        return val; 
    }
    

    public int getAddServerLatencySec() {
    	IAcmeProperty prop = m_model.getModelInstance().getProperty("ADD_SERVER_LATENCY_SEC");
    	return ((Integer) PropertyHelper.toJavaVal(prop.getValue())).intValue();
    }
    
	public int getAddServerLatencyPeriods() {
    	int latencySec = getAddServerLatencySec();
    	double periodMsec = Double.parseDouble(Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD));
    	return (int) Math.ceil(latencySec * 1000 / periodMsec);
	}

	public int getDimmerLevels() {
    	IAcmeProperty prop = m_model.getModelInstance().getProperty("DIMMER_LEVELS");
    	return ((Integer) PropertyHelper.toJavaVal(prop.getValue())).intValue();
    }

	public double getDimmerMargin() {
    	IAcmeProperty prop = m_model.getModelInstance().getProperty("DIMMER_MARGIN");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }

	public double getMaximumCapacity() {
    	IAcmeProperty prop = m_model.getModelInstance().getProperty("MAX_ARRIVAL_CAPACITY");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }

	public double getRTThreshold() {
    	IAcmeProperty prop = m_model.getModelInstance().getProperty("RT_THRESHOLD");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }
	
	public double getCurrentDimmer() {
    	IAcmeProperty prop = m_model.getModelInstance().getComponent(LOAD_BALANCER).getProperty("dimmer");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }
	
	public int getCurrentDimmerLevel() {
    	double dimmer = getCurrentDimmer();
    	int dimmerLevels = getDimmerLevels();
    	double dimmerMargin = getDimmerMargin();
        int level = SwimUtils.dimmerFactorToLevel(dimmer, dimmerLevels, dimmerMargin);
        return level;
    }
	
	public int getAddServerTacticProgress() {
		
		/*
		 * This assumes that only one server can be added at a time
		 */
		int latencyPeriods = getAddServerLatencyPeriods();
		if (getNumActiveServers() == getNumServers()) {
			return latencyPeriods; // add completed already
		}
		
    	long expectedActivationTime = (long) getDoubleProperty(PROP_MAX_SERVER_ACTIVATION_TIME, 0);
    	long timeToCompletionMsec = expectedActivationTime - System.currentTimeMillis();
    	if (timeToCompletionMsec <= 0) {
    		return latencyPeriods - 1; // because it hasn't finished, so we assume it will have by the next period
    	}
    	int remainingPeriods = (int) Math.ceil(timeToCompletionMsec / (1000 * getAddServerLatencySec()));
    	return latencyPeriods - remainingPeriods;
	}
	
	public int getNumServers() {
		return (int) getDoubleProperty(PROP_NUM_SERVERS, 1);
	}
	
	public int getNumActiveServers() {
		return (int) getDoubleProperty(PROP_NUM_ACTIVE_SERVERS, 1);
	}
	
	public int getMaxServers() {
		return (int) getDoubleProperty(PROP_MAX_SERVERS, 1);
	}
	
	public double getArrivalRate() {
    	IAcmeProperty prop = m_model.getModelInstance().getComponent(LOAD_BALANCER).getProperty(PROP_ARRIVAL_RATE);
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
	}

	public int getNumServers(GenericConfiguration config) {
		int numServers = getNumActiveServers(config);
		if (config.getInt(CONFIG_ADD_SERVER_PROGRESS) < getAddServerLatencyPeriods()) {

			// there's a server booting
			numServers++;
		}
		return numServers;
	}
	
	public int getNumActiveServers(GenericConfiguration config) {
		return config.getInt(CONFIG_SERVERS) + 1;
	}
	
	public double getDimmer(GenericConfiguration config) {
		int level = config.getInt(CONFIG_DIMMER) + 1;
    	int dimmerLevels = getDimmerLevels();
    	double dimmerMargin = getDimmerMargin();
		return SwimUtils.dimmerLevelToFactor(level, dimmerLevels, dimmerMargin);
	}
	
	public double getAverageResponseTime() {
    	IAcmeProperty prop = m_model.getModelInstance().getComponent(LOAD_BALANCER).getProperty("averageResponseTime");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }

	public double getBasicThroughput() {
    	IAcmeProperty prop = m_model.getModelInstance().getComponent(LOAD_BALANCER).getProperty("basicThroughput");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }


	public double getOptResponseTime() {
    	IAcmeProperty prop = m_model.getModelInstance().getComponent(LOAD_BALANCER).getProperty("optResponseTime");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }

	public double getOptThroughput() {
    	IAcmeProperty prop = m_model.getModelInstance().getComponent(LOAD_BALANCER).getProperty("optThroughput");
    	return ((Double) PropertyHelper.toJavaVal(prop.getValue())).doubleValue();
    }

	public int getNumThreadsPerServer() {
    	IAcmeProperty prop = m_model.getModelInstance().getProperty("THREADS_PER_SERVER");
    	return ((Integer) PropertyHelper.toJavaVal(prop.getValue())).intValue();
	}
	
	public double getAvgTraffic() {
		
		/*
		 * PROP_AVG_TRAFFIC gets the average over all the servers in the model
		 * The load for inactive servers will be 0, so we have to compensate to get
		 * the average over the active servers
		 */
		return getDoubleProperty(PROP_AVG_TRAFFIC, 0.0);
	}
}
