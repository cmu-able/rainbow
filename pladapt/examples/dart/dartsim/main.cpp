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
#include <iostream>
#include <getopt.h>
#include <cstdlib>
#include <dartam/RandomSeed.h>
#include <dartam/DartUtilityFunction.h>

// set this to 1 for testing
#define FIXED2DSPACE 0

using namespace dart::am2;
using namespace std;

enum ARGS {
	MAP_SIZE,
	SQUARE_MAP,
	NUM_TARGETS,
	NUM_THREATS,
	ALTITUDE_LEVELS,
	THREAT_RANGE,
	THREAT_SENSOR_FPR,
	THREAT_SENSOR_FNR,
	TARGET_SENSOR_FPR,
	TARGET_SENSOR_FNR,
	DL_TARGET_SENSOR_RANGE,
	AUTO_RANGE,
	LOOKAHEAD_HORIZON,
	REACH_PATH,
	REACH_MODEL,
	DISTRIB_APPROX,
	CHANGE_ALT_LATENCY,
	NON_LATENCY_AWARE,
	SEED,
	PROBABILITY_BOUND,
	STAY_ALIVE_REWARD,
	NO_FORMATION,
	ECM,
	TWO_LEVEL_TACTICS,
	ADAPT_MGR,
	PRISM_TEMPLATE,
	OPT_TEST
#if DART_USE_CE
	,
	CE_NONINCREMENTAL,
	CE_HINT_WEIGHT,
	CE_SAMPLES,
	CE_ALPHA,
	CE_PRECISION,
	CE_MAX_ITERATIONS
#endif
};

static struct option long_options[] = {
	{"map-size", required_argument, 0,  MAP_SIZE },
	{"square-map", no_argument, 0,  SQUARE_MAP },
	{"num-targets", required_argument, 0,  NUM_TARGETS },
	{"num-threats", required_argument, 0,  NUM_THREATS },
	{"altitude-levels", required_argument, 0,  ALTITUDE_LEVELS },
	{"threat-range", required_argument, 0,  THREAT_RANGE },
    {"threat-sensor-fpr", required_argument, 0,  THREAT_SENSOR_FPR },
    {"threat-sensor-fnr",  required_argument, 0,  THREAT_SENSOR_FNR },
    {"target-sensor-fpr", required_argument, 0,  TARGET_SENSOR_FPR },
    {"target-sensor-fnr",  required_argument, 0,  TARGET_SENSOR_FNR },
	{"dl-target-sensor-range", required_argument, 0,  DL_TARGET_SENSOR_RANGE },
	{"auto-range", no_argument, 0,  AUTO_RANGE },
    {"lookahead-horizon",  required_argument, 0,  LOOKAHEAD_HORIZON },
    {"reach-path",  required_argument, 0,  REACH_PATH },
    {"reach-model",  required_argument, 0,  REACH_MODEL },
	{"distrib-approx", required_argument, 0, DISTRIB_APPROX },
	{"change-alt-latency", required_argument, 0, CHANGE_ALT_LATENCY },
	{"non-latency-aware", no_argument, 0, NON_LATENCY_AWARE },
	{"seed", required_argument, 0, SEED },
	{"probability-bound", required_argument, 0, PROBABILITY_BOUND },
	{"stay-alive-reward", required_argument, 0, STAY_ALIVE_REWARD },
	{"no-formation", no_argument, 0, NO_FORMATION },
	{"ecm", no_argument, 0, ECM },
	{"two-level-tactics", no_argument, 0, TWO_LEVEL_TACTICS },
	{"adapt-mgr", required_argument, 0, ADAPT_MGR },
    {"prism-template",  required_argument, 0,  PRISM_TEMPLATE },
	{"opt-test", no_argument, 0, OPT_TEST },
#if DART_USE_CE
	{"ce-nonincremental", no_argument, 0, CE_NONINCREMENTAL },
	{"ce-hint-weight", required_argument, 0, CE_HINT_WEIGHT },
	{"ce-samples", required_argument, 0, CE_SAMPLES },
	{"ce-alpha", required_argument, 0, CE_ALPHA },
	{"ce-precision", required_argument, 0, CE_PRECISION },
	{"ce-max-iterations", required_argument, 0, CE_MAX_ITERATIONS },
#endif
    {0, 0, 0, 0 }
};

void usage() {
	cout << "valid options are:" << endl;
	int opt = 0;
	while (long_options[opt].name != 0) {
		cout << "\t--" << long_options[opt].name;
		if (long_options[opt].has_arg == required_argument) {
			cout << "=value";
		}
		cout << endl;
		opt++;
	}
	exit(EXIT_FAILURE);
}

int main(int argc, char** argv) {
	SimulationParams simParams;
	Params adaptParams;
	bool autoRange = false;

	while (1) {
		int option_index = 0;

		auto c = getopt_long(argc, argv, "", long_options, &option_index);

		if (c == -1) {
			break;
		}

		switch (c) {
		case MAP_SIZE:
			simParams.scenario.MAP_SIZE = atoi(optarg);
			break;
		case SQUARE_MAP:
			simParams.scenario.SQUARE_MAP = true;
			break;
		case NUM_TARGETS:
			simParams.scenario.TARGETS = atoi(optarg);
			break;
		case NUM_THREATS:
			simParams.scenario.THREATS = atoi(optarg);
			break;
		case ALTITUDE_LEVELS:
			adaptParams.configurationSpace.ALTITUDE_LEVELS = atoi(optarg);
			break;
		case THREAT_RANGE:
			adaptParams.environmentModel.THREAT_RANGE = atoi(optarg);
			break;
		case THREAT_SENSOR_FPR:
			adaptParams.longRangeSensor.THREAT_SENSOR_FPR = atof(optarg);
			break;
		case THREAT_SENSOR_FNR:
			adaptParams.longRangeSensor.THREAT_SENSOR_FNR = atof(optarg);
			break;
		case TARGET_SENSOR_FPR:
			adaptParams.longRangeSensor.TARGET_SENSOR_FPR = atof(optarg);
			break;
		case TARGET_SENSOR_FNR:
			adaptParams.longRangeSensor.TARGET_SENSOR_FNR = atof(optarg);
			break;
		case DL_TARGET_SENSOR_RANGE:
			adaptParams.environmentModel.TARGET_SENSOR_RANGE = atoi(optarg);
			break;
		case AUTO_RANGE:
			autoRange = true;
			break;
		case LOOKAHEAD_HORIZON:
			adaptParams.adaptationManager.HORIZON = atoi(optarg);
			break;
		case REACH_MODEL:
			adaptParams.adaptationManager.REACH_MODEL = optarg;
			break;
		case REACH_PATH:
			adaptParams.adaptationManager.REACH_PATH = optarg;
			break;
		case DISTRIB_APPROX:
			adaptParams.adaptationManager.distributionApproximation =
					(DartDTMCEnvironment::DistributionApproximation) atoi(optarg);
			break;
		case CHANGE_ALT_LATENCY:
			adaptParams.tactics.changeAltitudeLatency = atof(optarg);
			break;
		case NON_LATENCY_AWARE:
			adaptParams.adaptationManager.nonLatencyAware = true;
			break;
		case SEED:
			RandomSeed::seed(atoi(optarg));
			break;
		case PROBABILITY_BOUND:
			adaptParams.adaptationManager.probabilityBound = atof(optarg);
			break;
		case STAY_ALIVE_REWARD:
			adaptParams.adaptationManager.finalReward = atof(optarg);
			break;
		case NO_FORMATION:
			adaptParams.adaptationManager.REACH_MODEL += "-formation-disabled";
			break;
		case ECM:
			adaptParams.configurationSpace.hasEcm = true;
			break;
		case TWO_LEVEL_TACTICS:
			adaptParams.adaptationManager.twoLevelTactics = true;
			break;
		case ADAPT_MGR:
			adaptParams.adaptationManager.mgr = optarg;
			break;
		case PRISM_TEMPLATE:
			adaptParams.adaptationManager.PRISM_TEMPLATE = optarg;
			break;
		case OPT_TEST:
			simParams.optimalityTest = true;
			break;
#if DART_USE_CE
		case CE_NONINCREMENTAL:
			adaptParams.adaptationManager.ce_incremental = false;
			break;
		case CE_HINT_WEIGHT:
			adaptParams.adaptationManager.ce_hintWeight = atof(optarg);
			break;
		case CE_SAMPLES:
			adaptParams.adaptationManager.ce_samples = atoi(optarg);
			break;
		case CE_ALPHA:
			adaptParams.adaptationManager.ce_alpha = atof(optarg);
			break;
		case CE_PRECISION:
			adaptParams.adaptationManager.ce_precision = atof(optarg);
			break;
		case CE_MAX_ITERATIONS:
			adaptParams.adaptationManager.ce_maxIterations = atoi(optarg);
			break;
#endif
		default:
			usage();
		}
	}

	if (optind < argc) {
		usage();
	}

	if (autoRange) {
		adaptParams.environmentModel.TARGET_SENSOR_RANGE = adaptParams.configurationSpace.ALTITUDE_LEVELS;
		adaptParams.environmentModel.THREAT_RANGE = adaptParams.configurationSpace.ALTITUDE_LEVELS * 3 / 4;
	}

	if (adaptParams.adaptationManager.twoLevelTactics) {
		adaptParams.adaptationManager.REACH_MODEL += "-2l";
	}

	if (adaptParams.configurationSpace.hasEcm) {
		adaptParams.adaptationManager.REACH_MODEL += "-ecm";
	}


	// generate environment
#if FIXED2DSPACE
	RealEnvironment threatEnv;
	threatEnv.populate(Coordinate(10, 10), 0);

	RealEnvironment targetEnv;
	targetEnv.populate(Coordinate(10, 10), 0);

	threatEnv.setAt(Coordinate(2,2), true);
	threatEnv.setAt(Coordinate(3,2), true);
	threatEnv.setAt(Coordinate(6,6), true);
	threatEnv.setAt(Coordinate(7,7), true);

	targetEnv.setAt(Coordinate(5,2), true);
	targetEnv.setAt(Coordinate(7,2), true);
	targetEnv.setAt(Coordinate(7,5), true);
#else
	RealEnvironment threatEnv;
	RealEnvironment targetEnv;

	if (simParams.scenario.SQUARE_MAP) {

		/* generate true environment */
		threatEnv.populate(Coordinate(simParams.scenario.MAP_SIZE, simParams.scenario.MAP_SIZE), simParams.scenario.THREATS);
		targetEnv.populate(Coordinate(simParams.scenario.MAP_SIZE, simParams.scenario.MAP_SIZE), simParams.scenario.TARGETS);
	} else {

		/* generate true environment */
		threatEnv.populate(Coordinate(simParams.scenario.MAP_SIZE, 1), simParams.scenario.THREATS);
		targetEnv.populate(Coordinate(simParams.scenario.MAP_SIZE, 1), simParams.scenario.TARGETS);
	}
#endif


	// generate route
	Route route;

#if FIXED2DSPACE
	unsigned x = 2;
	unsigned y = 2;
	while (x < 7) {
		route.push_back(Coordinate(x,y));
		x++;
	}
	while (y <= 6) {
		route.push_back(Coordinate(x,y));
		y++;
	}
#else
	if (simParams.scenario.SQUARE_MAP) {
		for (unsigned y = 0; y < simParams.scenario.MAP_SIZE; y++) {
			if (y % 2) {
				for (unsigned x = simParams.scenario.MAP_SIZE; x > 0; x--) {
					route.push_back(Coordinate(x - 1, y));
				}
			} else {
				for (unsigned x = 0; x < simParams.scenario.MAP_SIZE; x++) {
					route.push_back(Coordinate(x, y));
				}
			}
		}
	} else {
		route = Route(Coordinate(0,0), 1.0, 0.0, simParams.scenario.MAP_SIZE);
	}
#endif

	// change parameters if doing optimality test
	if (simParams.optimalityTest) {
		adaptParams.adaptationManager.HORIZON = route.size();
		adaptParams.longRangeSensor.TARGET_SENSOR_FNR = 0;
		adaptParams.longRangeSensor.TARGET_SENSOR_FPR = 0;
		adaptParams.longRangeSensor.THREAT_SENSOR_FNR = 0;
		adaptParams.longRangeSensor.THREAT_SENSOR_FPR = 0;
		adaptParams.adaptationManager.distributionApproximation = DartDTMCEnvironment::DistributionApproximation::POINT;

		// autorange
		adaptParams.environmentModel.TARGET_SENSOR_RANGE = adaptParams.configurationSpace.ALTITUDE_LEVELS / 2.0;
		adaptParams.environmentModel.THREAT_RANGE = adaptParams.configurationSpace.ALTITUDE_LEVELS * 3.0 / 4;
		cout << "ranges sensor=" << adaptParams.environmentModel.TARGET_SENSOR_RANGE
				<< " threat=" << adaptParams.environmentModel.THREAT_RANGE << endl;
	}


	// instantiate adaptation manager
	shared_ptr<TargetSensor> pTargetSensor = Simulation::createTargetSensor(simParams,
			adaptParams);
	shared_ptr<Threat> pThreatSim = Simulation::createThreatSim(simParams, adaptParams);

	/* initialize adaptation manager */
	DartAdaptationManager adaptMgr;
	adaptMgr.initialize(adaptParams,
			unique_ptr<pladapt::UtilityFunction>(
					new DartUtilityFunction(pThreatSim, pTargetSensor,
							adaptParams.adaptationManager.finalReward)));

	if (simParams.optimalityTest && !adaptMgr.supportsStrategy()) {
		throw std::invalid_argument("selected adaptation manager does not support full strategies");
	}

	auto results = Simulation::run(simParams, adaptParams, threatEnv, targetEnv,
			route, adaptMgr);

	const std::string RESULTS_PREFIX = "out:";
	cout << RESULTS_PREFIX << "destroyed=" << results.destroyed << endl;
	cout << RESULTS_PREFIX << "targetsDetected=" << results.targetsDetected << endl;
	cout << RESULTS_PREFIX << "missionSuccess=" << results.missionSuccess << endl;

	cout << "csv," << results.targetsDetected << ',' << results.destroyed
			<< ',' << results.whereDestroyed.x
			<< ',' << results.missionSuccess
			<< ',' << results.decisionTimeAvg
			<< ',' << results.decisionTimeVar
			<<  endl;

	return 0;
}
