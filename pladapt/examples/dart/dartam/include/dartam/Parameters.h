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

#ifndef PARAMETERS_H_
#define PARAMETERS_H_


#include <string>
#include "RealEnvironment.h"
#include "DartDTMCEnvironment.h"

namespace dart {
namespace am2 {


struct LongRangeSensorParams {
	double THREAT_SENSOR_FPR = 0.10;
	double THREAT_SENSOR_FNR = 0.15;
	double TARGET_SENSOR_FPR = 0.10;
	double TARGET_SENSOR_FNR = 0.15;

	int THREAT_OBSERVATIONS_PER_CYCLE = 4;
	int TARGET_OBSERVATIONS_PER_CYCLE = 4;
};


/**
 * Model for environment simulation, also used for utility function
 */
struct EnvironmentModelParams {
	// this is the parameter for the short range sensor
	double TARGET_DETECTION_FORMATION_FACTOR = 1.2;
	unsigned TARGET_SENSOR_RANGE = 4; // in altitude levels

	// this is the parameter for the threats
	double DESTRUCTION_FORMATION_FACTOR = 1.5;
	unsigned THREAT_RANGE = 3; // in altitude levels
};

struct ConfigurationSpaceParams {
	double maxAltitude = 3000;
	unsigned ALTITUDE_LEVELS = 4;
	bool hasEcm = false;
};

struct AdaptationManagerParams {

	std::string mgr = "sdpra"; /**< identifier for the adaptation manager to use */

	/**
	 * Period for the adaptation decision. It should be the time it takes for
	 * the team to traverse one cell.
	 *
	 * Any time unit can be used as long as it is consistent in all the time
	 * parameters (this one and tactic latencies).
	 */
	double adaptationPeriod = 60;
	unsigned HORIZON = 5;
	bool nonLatencyAware = false;
	DartDTMCEnvironment::DistributionApproximation distributionApproximation = DartDTMCEnvironment::DistributionApproximation::E_PT;
	std::string REACH_PATH = "../../../reach/reach.sh";
	std::string REACH_MODEL = "../../../reach/model/dart2";
	std::string REACH_PREFIX = "";
	double probabilityBound = 0.90; /**< lower bound on the probability of survival */
	double finalReward = 0.00001; // so that all else being equal, it'll favor surviving
	std::string PRISM_TEMPLATE = "../dartam/model/dart2";
	bool twoLevelTactics = false;

#if DART_USE_CE
	//-- ce solver parameters
	int ce_samples = 100;
	double ce_alpha = 0.3;
	double ce_precision = 0.01;
	int ce_maxIterations = 100;
	double ce_hintWeight = 0.0;
	bool ce_incremental = true;
#endif

};

struct TacticsParams {

	/**
	 * Latency of the change altitude tactics.
	 *
	 * Use the same unit as for AdaptationManagerParams.adaptationPeriod
	 */
	unsigned changeAltitudeLatency = 60;
};

struct Params {
	LongRangeSensorParams longRangeSensor;
	EnvironmentModelParams environmentModel;
	AdaptationManagerParams adaptationManager;
	ConfigurationSpaceParams configurationSpace;
	TacticsParams tactics;
};


} /* namespace am2 */
} /* namespace dart */



#endif /* PARAMETERS_H_ */
