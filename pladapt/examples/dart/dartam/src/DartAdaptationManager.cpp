/*******************************************************************************
 * PLA Adaptation Manager
 *
 * Copyright 2017 Carnegie Mellon University. All Rights Reserved.
 * 
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS
 * TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE
 * MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND
 * WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Released under a BSD-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release
 * and unlimited distribution. Please see Copyright notice for non-US Government
 * use and distribution.
 ******************************************************************************/

#include <dartam/DartAdaptationManager.h>
#include <dartam/DartConfigurationManager.h>
#include <pladapt/Utils.h>
#include <pladapt/SDPRAAdaptationManager.h>
#include <pladapt/PMCRAAdaptationManager.h>
#include <dartam/DartPMCHelper.h>
#include <math.h>

#if DART_USE_CE
#include <pladapt/CEAdaptationManager.h>
#endif

using namespace std;

namespace dart {
namespace am2 {

const string ADAPT_MGR_SDP = "sdp";
const string ADAPT_MGR_SDPRA = "sdpra";
const string ADAPT_MGR_PMC = "pmc";
#if DART_USE_CE
const string ADAPT_MGR_CE = "ce";
#endif

void DartAdaptationManager::instantiateAdaptationMgr(const Params& params) {
	cout << "Initializing adapt mgr...";

	// initialize config manager
	configManager = std::make_shared<DartConfigurationManager>(
			params.configurationSpace.ALTITUDE_LEVELS,
			pladapt::tacticLatencyToPeriods(params.tactics.changeAltitudeLatency, params.adaptationManager.adaptationPeriod),
			params.configurationSpace.hasEcm, params.adaptationManager.twoLevelTactics);

	auto changeAltitudePeriods = pladapt::tacticLatencyToPeriods(params.tactics.changeAltitudeLatency, params.adaptationManager.adaptationPeriod);

	// instantiate and initialize appropriate adapt mgr
	if (params.adaptationManager.mgr == ADAPT_MGR_PMC) {
	    YAML::Node amParams;
	    amParams[pladapt::PMCAdaptationManager::NO_LATENCY] = (params.adaptationManager.nonLatencyAware || changeAltitudePeriods == 0);
	    amParams[pladapt::PMCAdaptationManager::TEMPLATE_PATH] = params.adaptationManager.PRISM_TEMPLATE;
	    amParams[pladapt::PMCRAAdaptationManager::PROBABILITY_BOUND] = params.adaptationManager.probabilityBound;

	    auto pAdaptMgr = new pladapt::PMCAdaptationManager;
		pAdaptMgr->initialize(configManager, amParams, std::make_shared<const DartPMCHelper>(params));
		adaptMgr.reset(pAdaptMgr);
	} else { // SDP or derived
	    YAML::Node amParams;
	    amParams[pladapt::SDPAdaptationManager::NO_LATENCY] = (params.adaptationManager.nonLatencyAware || changeAltitudePeriods == 0);
	    amParams[pladapt::SDPAdaptationManager::REACH_OPTIONS] = "-c ConfigDart2";
		amParams[pladapt::SDPAdaptationManager::REACH_PATH] = params.adaptationManager.REACH_PATH;
		if (!params.adaptationManager.REACH_PREFIX.empty()) {
			amParams[pladapt::SDPAdaptationManager::REACH_PREFIX] = params.adaptationManager.REACH_PREFIX;
		}
	    if (params.adaptationManager.nonLatencyAware && changeAltitudePeriods > 0) {
	    	amParams[pladapt::SDPAdaptationManager::REACH_MODEL] = params.adaptationManager.REACH_MODEL + "-nla";
	    } else {
	    	amParams[pladapt::SDPAdaptationManager::REACH_MODEL] = params.adaptationManager.REACH_MODEL;
	    }

	    stringstream scope;
	    scope << "A=" << params.configurationSpace.ALTITUDE_LEVELS;
	    scope << " F=2";
	    if (changeAltitudePeriods > 0) {
	        scope << " TPIA#=" <<  changeAltitudePeriods << " TPDA#=" <<  changeAltitudePeriods;
	        if (params.adaptationManager.twoLevelTactics) {
	        	scope << " TPIA2#=" <<  changeAltitudePeriods << " TPDA2#=" <<  changeAltitudePeriods;
	        }
	    }

	    amParams[pladapt::SDPAdaptationManager::REACH_SCOPE] = scope.str();

	    amParams[pladapt::SDPRAAdaptationManager::PROBABILITY_BOUND] = params.adaptationManager.probabilityBound;

#if DART_USE_CE
	    if (params.adaptationManager.mgr == ADAPT_MGR_CE) {
	    	amParams[pladapt::CEAdaptationManager::CE_INCREMENTAL] = params.adaptationManager.ce_incremental;
	    	amParams[pladapt::CEAdaptationManager::CE_HINT_WEIGHT] = params.adaptationManager.ce_hintWeight;
	    	amParams[pladapt::CEAdaptationManager::CE_SAMPLES] = params.adaptationManager.ce_samples;
			amParams[pladapt::CEAdaptationManager::CE_ALPHA] = params.adaptationManager.ce_alpha;
			amParams[pladapt::CEAdaptationManager::CE_PRECISION] = params.adaptationManager.ce_precision;
			amParams[pladapt::CEAdaptationManager::CE_MAX_ITERATIONS] = params.adaptationManager.ce_maxIterations;

			auto pAdaptMgr = new pladapt::CEAdaptationManager;
			pAdaptMgr->initialize(configManager, amParams);
			adaptMgr.reset(pAdaptMgr);
		} else
#endif
		if (params.adaptationManager.mgr == ADAPT_MGR_SDPRA) {
			auto pAdaptMgr = new pladapt::SDPRAAdaptationManager;
			pAdaptMgr->initialize(configManager, amParams);
			adaptMgr.reset(pAdaptMgr);
		} else if (params.adaptationManager.mgr == ADAPT_MGR_SDP) {
			auto pAdaptMgr = new pladapt::SDPAdaptationManager;
			pAdaptMgr->initialize(configManager, amParams);
			adaptMgr.reset(pAdaptMgr);
		} else {
			ostringstream msg;
			msg << "Error: adaptation manager ";
			msg << params.adaptationManager.mgr;
			msg << " not supported.";
			throw std::invalid_argument(msg.str());
		}
	}

	cout << "done" << endl;
}

void DartAdaptationManager::initialize(const Params& params, std::unique_ptr<pladapt::UtilityFunction> utilityFunction) {
	this->params = params;
	pEnvThreatMonitor.reset(
			new EnvironmentMonitor(
					unique_ptr<Sensor>(
							new Sensor(params.longRangeSensor.THREAT_SENSOR_FPR,
									params.longRangeSensor.THREAT_SENSOR_FNR))));
	pEnvTargetMonitor.reset(
			new EnvironmentMonitor(
					unique_ptr<Sensor>(
							new Sensor(params.longRangeSensor.TARGET_SENSOR_FPR,
									params.longRangeSensor.TARGET_SENSOR_FNR))));

	instantiateAdaptationMgr(params);

	pUtilityFunction = std::move(utilityFunction);
}

pladapt::TacticList DartAdaptationManager::decideAdaptation(
		const DartMonitoringInfo& monitoringInfo) {

	/* update environment */
	pEnvThreatMonitor->update(monitoringInfo.threatSensing);
	pEnvTargetMonitor->update(monitoringInfo.targetSensing);

	/* build env model with information collected so far */
	Route senseRoute(monitoringInfo.position, monitoringInfo.directionX, monitoringInfo.directionY, params.adaptationManager.HORIZON);
	DartDTMCEnvironment threatDTMC(*pEnvThreatMonitor, senseRoute, params.adaptationManager.distributionApproximation);
	DartDTMCEnvironment targetDTMC(*pEnvTargetMonitor, senseRoute, params.adaptationManager.distributionApproximation);
	pladapt::EnvironmentDTMCPartitioned jointEnv = pladapt::EnvironmentDTMCPartitioned::createJointDTMC(threatDTMC, targetDTMC);

	/* make adaptation decision */
	//adaptMgr->setDebug(monitoringInfo.position.x == 4);
	return adaptMgr->evaluate(convertToDiscreteConfiguration(monitoringInfo), jointEnv, *pUtilityFunction, params.adaptationManager.HORIZON);
}

DartConfiguration DartAdaptationManager::convertToDiscreteConfiguration(const DartMonitoringInfo& info) const {
	unsigned altitudeLevel = round(info.altitude * (params.configurationSpace.ALTITUDE_LEVELS - 1) / params.configurationSpace.maxAltitude);
	DartConfiguration::Formation formation =
			(info.formation == 0) ?
					DartConfiguration::Formation::LOOSE :
					DartConfiguration::Formation::TIGHT;
	unsigned ttcIncAlt = round(info.ttcIncAlt / params.adaptationManager.adaptationPeriod);
	unsigned ttcDecAlt = round(info.ttcDecAlt / params.adaptationManager.adaptationPeriod);
	unsigned ttcIncAlt2 = round(info.ttcIncAlt2 / params.adaptationManager.adaptationPeriod);
	unsigned ttcDecAlt2 = round(info.ttcDecAlt2 / params.adaptationManager.adaptationPeriod);

	// TODO formation change assumed instantaneous for now

	return DartConfiguration(altitudeLevel, formation, ttcIncAlt, ttcDecAlt, ttcIncAlt2, ttcDecAlt2, info.ecm);
}

void DartAdaptationManager::printEnvironment() {
	if (threatEnv.getSize().y > 1) {
		cout << "DartAdaptationManager::printEnvironment(): warning environment is bidimensional" << endl;
	}
	for (int i = 0; i < threatEnv.getSize().x; ++i) {
		cout << char((threatEnv.isObjectAt(i)) ? '^' : ' ');
	}
	cout << endl;
	for (int i = 0; i < targetEnv.getSize().x; ++i) {
		cout << char((targetEnv.isObjectAt(i)) ? 'T' : ' ');
	}
	cout << endl;
	for (int i = 0; i < targetEnv.getSize().x; ++i) {
		cout << '!';
	}
	cout << endl;
}

bool DartAdaptationManager::supportsStrategy() const {
	return adaptMgr->supportsStrategy();
}

std::shared_ptr<pladapt::Strategy> DartAdaptationManager::getStrategy() {
	return adaptMgr->getStrategy();
}


DartAdaptationManager::~DartAdaptationManager() {
	// TODO Auto-generated destructor stub
}


} /* namespace am2 */
} /* namespace dart */
