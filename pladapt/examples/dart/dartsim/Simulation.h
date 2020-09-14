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

#ifndef SIMULATION_H_
#define SIMULATION_H_

#include <dartam/DartAdaptationManager.h>
#include <dartam/Route.h>

namespace dart {
namespace am2 {


struct ScenarioParams {
	unsigned MAP_SIZE = 40;
	unsigned THREATS = 6;
	unsigned TARGETS = 4;
	bool SQUARE_MAP = false;
};


struct SimulationParams {
	ScenarioParams scenario;
	bool optimalityTest = false;
};


struct SimulationResults {
	bool destroyed;
	Coordinate whereDestroyed;
	unsigned targetsDetected;
	bool missionSuccess;
	double decisionTimeAvg;
	double decisionTimeVar;
};

class Simulation {
public:
	static SimulationResults run(const SimulationParams& simParams, const Params& adaptParams,
			const RealEnvironment& threatEnv, const RealEnvironment& targetEnv,
			const Route& route, DartAdaptationManager& adaptMgr);

	static std::shared_ptr<Threat> createThreatSim(const SimulationParams& simParams,
			const Params& adaptParams);
	static std::shared_ptr<TargetSensor> createTargetSensor(const SimulationParams& simParams,
			const Params& adaptParams);

	virtual ~Simulation();
};

} /* namespace am2 */
} /* namespace dart */

#endif /* SIMULATION_H_ */
