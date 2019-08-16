package org.sa.rainbow.swim.adaptation;

import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.swim.SwimModelHelper;

import pladapt.GenericConfiguration;
import pladapt.GenericEnvironment;
import pladapt.GenericUtilityFunction;

public class SwimUtilityFunction extends GenericUtilityFunction {
	
	private SwimModelHelper m_modelHelper = null;
	
	SwimUtilityFunction(AcmeModelInstance model, SwimModelHelper swimModel) {
		m_modelHelper = swimModel;
	}

	//*****************************************************************
	// Queuing model G/G/c LPS with round-robin allocation to servers
	//*****************************************************************
	private double responseTime(int servers, int threads, double arrivalRateMean, double arrivalRateVariance,
			double brownoutFactor, double lowServiceTimeMean, double lowServiceTimeVariance, double serviceTimeMean,
			double serviceTimeVariance) {
		if (arrivalRateMean == 0) {
			return 0;
		}
	    double lambda = arrivalRateMean / servers;
	    double beta = brownoutFactor * lowServiceTimeMean + (1-brownoutFactor) * serviceTimeMean;
	    double rho = lambda * beta;
	    double ca2 = servers / (arrivalRateVariance * Math.pow(servers / arrivalRateMean, 2));
		double cs2 = (brownoutFactor * lowServiceTimeVariance + (1 - brownoutFactor) * serviceTimeVariance)
				/ Math.pow(beta, 2);
	    double dp = Math.pow(rho, threads * (1+cs2)/(ca2 + cs2));
	    double rb = ((ca2 + cs2) / 2) * dp * beta / (1 - rho);
	    double rz = ((ca2 + cs2) / (1 + cs2)) * (1 - dp) * beta / (1 - rho);
	    double rt= (rho > 1 ? Double.MAX_VALUE : (rb + rz));

	    return rt;
	}

	public double getGenAdditiveUtility(GenericConfiguration config, GenericEnvironment env, int time) {
		double predictedResponseTime = responseTime(
				m_modelHelper.getNumActiveServers(config),
				m_modelHelper.getNumThreadsPerServer(),
	            env.getDouble(SwimModelHelper.ENV_ARRIVAL_RATE_MEAN),
	            env.getDouble(SwimModelHelper.ENV_ARRIVAL_RATE_VARIANCE),
	            (1 - m_modelHelper.getDimmer(config)),
	            m_modelHelper.getEstimatedBasicServiceTime(),
	            m_modelHelper.getEstimatedBasicServiceTimeVariance(),
	            m_modelHelper.getEstimatedOptServiceTime(),
	            m_modelHelper.getEstimatedOptServiceTimeVariance());

//		System.out.println("getGenAdditiveUtility(" + config + ", " + env + ") -> rt=" + predictedResponseTime);
		return getUtility(config, env, predictedResponseTime);
	}	
	

	/**
	 * returns utility per unit of time;
	 *
	 * This function is used to drive decisions, so it may give results that force decisions in certain
	 * way, but are not actual utility accrued (e.g., penalizing not adding servers when the system is overloaded)
	 */
	private double getUtility(GenericConfiguration config, GenericEnvironment env, double avgResponseTime)
	{
		int numServers = m_modelHelper.getNumServers(config);
	    double cost = numServers;
	    double brownoutRevenue = 1;
	    double normalRevenue = 1.5;
	    double spacing = m_modelHelper.getMaxServers() + 1; // to spread apart utility values to rank by number of servers
	    double maxThroughput = m_modelHelper.getMaxServers() * m_modelHelper.getMaximumCapacity();
	    double latePenalty = maxThroughput * normalRevenue * spacing;

	    // TODO this is arrival rate, not really throughput
	    // Using arrival rate is just an approximation. Fixing it would require computing the throughput with a perf model
	    double arrivalRateMean = env.asDouble();
	    double throughput = arrivalRateMean;

	    double brownoutFactor = 1 - m_modelHelper.getDimmer(config);

	    double positiveUtility = Math.round((throughput * (brownoutFactor * brownoutRevenue + (1 - brownoutFactor) * normalRevenue)))
	            * spacing;

	    double utility = ((avgResponseTime > m_modelHelper.getRTThreshold() || avgResponseTime < 0) ? Math.min(0.0, throughput * normalRevenue * spacing - latePenalty) : positiveUtility) - cost;
	    if (avgResponseTime == Double.MAX_VALUE || avgResponseTime < 0) {
	        utility = (-latePenalty - 2 * spacing + numServers + brownoutFactor);
	    }

	    return utility;
	}

}
