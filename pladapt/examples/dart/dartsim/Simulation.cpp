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
#include "Simulation.h"
#include <dartam/Sensor.h>
#include <dartam/DartDTMCEnvironment.h>
#include "pladapt/EnvironmentDTMCPartitioned.h"
#include <dartam/DartUtilityFunction.h>
#include <dartam/DartConfiguration.h>
#include <dartam/DartAdaptationManager.h>
#include <dartam/EnvironmentMonitor.h>
#include <exception>
#include <dartam/DeterministicThreat.h>
#include <dartam/DeterministicTargetSensor.h>
#include <iostream>
#include <pladapt/Utils.h>
#include <memory>
#include <chrono>
#include <boost/accumulators/accumulators.hpp>
#include <boost/accumulators/statistics/stats.hpp>
#include <boost/accumulators/statistics/mean.hpp>
#include <boost/accumulators/statistics/moment.hpp>

using namespace std;
using namespace pladapt;

using myclock = chrono::high_resolution_clock;
using Stats = boost::accumulators::accumulator_set<double,
        boost::accumulators::stats<boost::accumulators::tag::mean,
                boost::accumulators::tag::moment<2> > >;

namespace dart {
namespace am2 {

const string INC_ALTITUDE = "IncAlt";
const string DEC_ALTITUDE = "DecAlt";
const string INC_ALTITUDE2 = "IncAlt2";
const string DEC_ALTITUDE2 = "DecAlt2";
const string GO_TIGHT = "GoTight";
const string GO_LOOSE = "GoLoose";
const string ECM_ON = "EcmOn";
const string ECM_OFF = "EcmOff";

shared_ptr<Threat> Simulation::createThreatSim(const SimulationParams& simParams,
		const Params& adaptParams) {
	shared_ptr<Threat> pThreatSim;
	if (simParams.optimalityTest) {
		pThreatSim = make_shared<DeterministicThreat>(
				adaptParams.environmentModel.THREAT_RANGE,
				adaptParams.environmentModel.DESTRUCTION_FORMATION_FACTOR);
	} else {
		pThreatSim = make_shared<Threat>(
				adaptParams.environmentModel.THREAT_RANGE,
				adaptParams.environmentModel.DESTRUCTION_FORMATION_FACTOR);
	}
	return pThreatSim;
}

shared_ptr<TargetSensor> Simulation::createTargetSensor(const SimulationParams& simParams,
		const Params& adaptParams) {
	shared_ptr<TargetSensor> pTargetSensor;
	if (simParams.optimalityTest) {
		pTargetSensor = make_shared<DeterministicTargetSensor>(
				adaptParams.environmentModel.TARGET_SENSOR_RANGE,
				adaptParams.environmentModel.TARGET_DETECTION_FORMATION_FACTOR);
	} else {
		pTargetSensor = make_shared<TargetSensor>(
				adaptParams.environmentModel.TARGET_SENSOR_RANGE,
				adaptParams.environmentModel.TARGET_DETECTION_FORMATION_FACTOR);
	}
	return pTargetSensor;
}

DartConfiguration executeTactic(string tactic, const DartConfiguration& config,
		const TacticsParams& tacticsParams, const AdaptationManagerParams& adaptMgrParams) {
	auto changeAltitudePeriods =
			pladapt::tacticLatencyToPeriods(tacticsParams.changeAltitudeLatency,
					adaptMgrParams.adaptationPeriod);

	auto newConfig = config;
	cout << "executing tactic " << tactic << endl;
	if (tactic == INC_ALTITUDE) {
		if (changeAltitudePeriods > 0) {
			newConfig.setTtcIncAlt(changeAltitudePeriods);
		} else {
			newConfig.setAltitudeLevel(newConfig.getAltitudeLevel() + 1);
		}
	} else if (tactic == DEC_ALTITUDE) {
		if (changeAltitudePeriods > 0) {
			newConfig.setTtcDecAlt(changeAltitudePeriods);
		} else {
			newConfig.setAltitudeLevel(newConfig.getAltitudeLevel() - 1);
		}
	} else if (tactic == INC_ALTITUDE2) {
		if (changeAltitudePeriods > 0) {
			newConfig.setTtcIncAlt2(changeAltitudePeriods);
		} else {
			newConfig.setAltitudeLevel(newConfig.getAltitudeLevel() + 2);
		}
	} else if (tactic == DEC_ALTITUDE2) {
		if (changeAltitudePeriods > 0) {
			newConfig.setTtcDecAlt2(changeAltitudePeriods);
		} else {
			newConfig.setAltitudeLevel(newConfig.getAltitudeLevel() - 2);
		}
	} else if (tactic == GO_TIGHT) {
		newConfig.setFormation(DartConfiguration::Formation::TIGHT);
	} else if (tactic == GO_LOOSE) {
		newConfig.setFormation(DartConfiguration::Formation::LOOSE);
	} else if (tactic == ECM_ON) {
		newConfig.setEcm(true);
	} else if (tactic == ECM_OFF) {
		newConfig.setEcm(false);
	} else {
		throw std::runtime_error(string("unknown tactic ") + tactic);
	}
	return newConfig;
}



SimulationResults Simulation::run(const SimulationParams& simParams, const Params& params,
		const RealEnvironment& threatEnv, const RealEnvironment& targetEnv,
		const Route& route, DartAdaptationManager& adaptMgr) {
	SimulationResults results;

	/*
	 *  optimal testing stuff
	 *  This makes a single adaptation decision for the whole route, computing
	 *  the complete strategy along the route. It then uses that strategy for
	 *  the whole simulation.
	 */
	std::shared_ptr<Strategy> strategy;
	Strategy::iterator strategyIterator;
	bool gotStrategy = false;

	shared_ptr<TargetSensor> pTargetSensor = createTargetSensor(simParams,
			params);
	shared_ptr<Threat> pThreatSim = createThreatSim(simParams, params);

	/* create forward-looking sensors */
	EnvironmentMonitor envThreatMonitor(
					unique_ptr<Sensor>(
							new Sensor(params.longRangeSensor.THREAT_SENSOR_FPR,
									params.longRangeSensor.THREAT_SENSOR_FNR)));
	EnvironmentMonitor envTargetMonitor(
					unique_ptr<Sensor>(
							new Sensor(params.longRangeSensor.TARGET_SENSOR_FPR,
									params.longRangeSensor.TARGET_SENSOR_FNR)));


	/* prepare sim display */
	const int SCREEN_THREATS = params.configurationSpace.ALTITUDE_LEVELS;
	const int SCREEN_TARGETS = params.configurationSpace.ALTITUDE_LEVELS + 1;

	unsigned pathLength = route.size();
	char screen[pathLength][params.configurationSpace.ALTITUDE_LEVELS + 2];
	for (unsigned p = 0; p < pathLength; p++) {
		for (unsigned h = 0; h < params.configurationSpace.ALTITUDE_LEVELS; h++) {
			screen[p][h] = ' ';
		}
		if (threatEnv.isObjectAt(route.at(p))) {
			screen[p][SCREEN_THREATS] = '^';
		} else {
			screen[p][SCREEN_THREATS] = ' ';
		}
		if (targetEnv.isObjectAt(route.at(p))) {
			screen[p][SCREEN_TARGETS] = 'T';
		} else {
			screen[p][SCREEN_TARGETS] = ' ';
		}
	}


	Stats decisionTimeStats;

	/* initialize platform */
	DartConfiguration currentConfig(params.configurationSpace.ALTITUDE_LEVELS - 1, DartConfiguration::Formation::LOOSE, 0, 0, 0, 0);

	unsigned targetsDetected = 0;
	bool destroyed = false;

	unsigned screenPosition = 0;

	/* sim loop */
	Coordinate position;
	auto routeIt = route.begin();
	while (routeIt != route.end()) {
		position = *routeIt;

		cout << "current position: " << position << endl;

		/*
		 * collect monitoring info
		 *
		 * Here we're converting from discrete team config to continuous config
		 * because this is a discrete time simulation, but in a normal
		 * case, the team configuration would be already continuous
		 */
		DartMonitoringInfo monitoringInfo;
		monitoringInfo.position = position;
		monitoringInfo.altitude = params.configurationSpace.maxAltitude * currentConfig.getAltitudeLevel() / (params.configurationSpace.ALTITUDE_LEVELS - 1);

		routeIt++;
		if (routeIt != route.end()) {
			monitoringInfo.directionX = routeIt->x - position.x;
			monitoringInfo.directionY = routeIt->y - position.y;
		} else {
			monitoringInfo.directionX = 0;
			monitoringInfo.directionY = 0;
		}
		monitoringInfo.formation = (currentConfig.getFormation() == DartConfiguration::Formation::LOOSE) ? 0 : 1;
		monitoringInfo.ttcFormationChange = 0;
		monitoringInfo.ttcIncAlt = currentConfig.getTtcIncAlt() * params.adaptationManager.adaptationPeriod;
		monitoringInfo.ttcDecAlt = currentConfig.getTtcDecAlt() * params.adaptationManager.adaptationPeriod;
		monitoringInfo.ttcIncAlt2 = currentConfig.getTtcIncAlt2() * params.adaptationManager.adaptationPeriod;
		monitoringInfo.ttcDecAlt2 = currentConfig.getTtcDecAlt2() * params.adaptationManager.adaptationPeriod;
		monitoringInfo.ecm = currentConfig.getEcm();

		/* monitor environment */
		Route senseRoute(position, monitoringInfo.directionX, monitoringInfo.directionY, params.adaptationManager.HORIZON);
		envThreatMonitor.clear();
		envThreatMonitor.sense(threatEnv, params.longRangeSensor.THREAT_OBSERVATIONS_PER_CYCLE, senseRoute);
		envTargetMonitor.clear();
		envTargetMonitor.sense(targetEnv, params.longRangeSensor.TARGET_OBSERVATIONS_PER_CYCLE, senseRoute);
		monitoringInfo.threatSensing = envThreatMonitor.getResults(senseRoute);
    //		cout << "sensed threats:";
    //		for (const auto& r : monitoringInfo.threatSensing) {
    //			cout << " (" <<  r.cellPosition << ' ' << r.detections << '/' << r.observations << ')';
    //		}
    //		cout << endl;
		monitoringInfo.targetSensing = envTargetMonitor.getResults(senseRoute);


		TacticList tactics;
		if (!simParams.optimalityTest || (simParams.optimalityTest && !gotStrategy)) {

			/* invoke adaptation manager */
			auto startTime = myclock::now();
			tactics = adaptMgr.decideAdaptation(monitoringInfo);
			auto delta = myclock::now() - startTime;
			double deltaMsec = chrono::duration_cast<chrono::duration<double, std::milli>>(delta).count();
			decisionTimeStats(deltaMsec);

			if (simParams.optimalityTest) {
				gotStrategy = true;
				strategy = adaptMgr.getStrategy();
				strategyIterator = strategy->begin();
			}
		} else {
			strategyIterator++;
			assert(strategyIterator != strategy->end());
			tactics = *strategyIterator;
		}

		/*
		 * execute adaptation
		 *
		 * Here we have to execute instantaneous tactics.
		 * For tactics with latency (e.g., change altitude), they must be
		 * started, but we cannot wait until they complete.
		 */
		for (auto tactic : tactics) {
			currentConfig = executeTactic(tactic, currentConfig, params.tactics, params.adaptationManager);
		}

		/* update display */
		screen[screenPosition][currentConfig.getAltitudeLevel()] =
				(currentConfig.getFormation()
						== DartConfiguration::Formation::LOOSE) ?
						(currentConfig.getEcm() ? '@' : '#') :
						(currentConfig.getEcm() ? '0' : '*');

		/* simulate threats */
		destroyed = pThreatSim->isDestroyed(threatEnv, currentConfig, position);
		if (destroyed) {
			cout << "Team destroyed at position " << position << endl;
			break;
		}

		/* simulate target detection */
		if (pTargetSensor->sense(currentConfig, targetEnv.isObjectAt(position))) {
			cout << "Target detected at " << position << endl;
			targetsDetected++;
			screen[screenPosition][SCREEN_TARGETS] = 'X';
		}


		/* system evolution */
		// route position already advanced before
		screenPosition++;

		/* update tactic progress */
		auto ttcIncAlt = currentConfig.getTtcIncAlt();
		if (ttcIncAlt > 0) {
			currentConfig.setTtcIncAlt(--ttcIncAlt);
			if (ttcIncAlt == 0) {
				currentConfig.setAltitudeLevel(currentConfig.getAltitudeLevel() + 1);
			}
		}

		auto ttcDecAlt = currentConfig.getTtcDecAlt();
		if (ttcDecAlt > 0) {
			currentConfig.setTtcDecAlt(--ttcDecAlt);
			if (ttcDecAlt == 0) {
				currentConfig.setAltitudeLevel(currentConfig.getAltitudeLevel() - 1);
			}
		}

		auto ttcIncAlt2 = currentConfig.getTtcIncAlt2();
		if (ttcIncAlt2 > 0) {
			currentConfig.setTtcIncAlt2(--ttcIncAlt2);
			if (ttcIncAlt2 == 0) {
				currentConfig.setAltitudeLevel(currentConfig.getAltitudeLevel() + 2);
			}
		}

		auto ttcDecAlt2 = currentConfig.getTtcDecAlt2();
		if (ttcDecAlt2 > 0) {
			currentConfig.setTtcDecAlt2(--ttcDecAlt2);
			if (ttcDecAlt2 == 0) {
				currentConfig.setAltitudeLevel(currentConfig.getAltitudeLevel() - 2);
			}
		}
	}

	if (!destroyed) {
		cout << "Total targets detected: " << targetsDetected << endl;
	}

	for (unsigned h = params.configurationSpace.ALTITUDE_LEVELS; h > 0 ; h--) {
		for (unsigned p = 0; p < pathLength; p++) {
			cout << screen[p][h - 1];
		}
		cout << endl;
	}
	for (unsigned h = params.configurationSpace.ALTITUDE_LEVELS; h < params.configurationSpace.ALTITUDE_LEVELS + 2; h++) {
		for (unsigned p = 0; p < pathLength; p++) {
			cout << screen[p][h];
		}
		cout << endl;
	}

	results.destroyed = destroyed;
	results.targetsDetected = targetsDetected;
	results.whereDestroyed = position;
	results.missionSuccess = !destroyed && targetsDetected >= simParams.scenario.TARGETS / 2.0;
	results.decisionTimeAvg = boost::accumulators::mean(decisionTimeStats);
	results.decisionTimeVar = boost::accumulators::moment<2>(decisionTimeStats);

	return results;
}

Simulation::~Simulation() {
	// TODO Auto-generated destructor stub
}

} /* namespace am2 */
} /* namespace dart */
