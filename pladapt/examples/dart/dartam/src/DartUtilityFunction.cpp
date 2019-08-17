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

#include <dartam/DartUtilityFunction.h>
#include <dartam/DartConfiguration.h>
#include <dartam/DartEnvironment.h>
#include <dartam/Threat.h>
#include <dartam/TargetSensor.h>
#include <algorithm>
#include <iostream>
#include <random>
#include <memory>
#include <dartam/RandomSeed.h>

using namespace pladapt;
using namespace std;

namespace dart {
namespace am2 {

static const double INJECTED_RMSE = 0; //0.3 * 0.6;
std::unique_ptr<std::normal_distribution<>> pDist;
std::default_random_engine randomGenerator;

DartUtilityFunction::DartUtilityFunction(std::shared_ptr<Threat> threat, std::shared_ptr<TargetSensor> targetSensor,
		double finalReward)
	: threat(threat), targetSensor(targetSensor), finalReward(finalReward)
{
	if (INJECTED_RMSE > 0) {
		randomGenerator.seed(RandomSeed::getNextSeed());
		pDist.reset(new normal_distribution<>(0, INJECTED_RMSE));
		//pDist.reset(new normal_distribution<>(INJECTED_RMSE / 2, INJECTED_RMSE));
	}
}


/**
 * computes g(c_t) in the paper
 */
double DartUtilityFunction::getAdditiveUtility(
		const pladapt::Configuration& config, const pladapt::Environment& env,
		int time) const {
    auto& dartConfig = dynamic_cast<const DartConfiguration&>(config);
    auto dartEnv = DartEnvironment(dynamic_cast<const JointEnvironment&>(env));

	double probOfDetection = targetSensor->getProbabilityOfDetection(dartConfig);

	if (INJECTED_RMSE > 0) {
    	double error = pDist->operator ()(randomGenerator);
    	probOfDetection += error;
    	if (probOfDetection < 0) {
    		probOfDetection = 0;
    	} else if (probOfDetection > 1) {
    		probOfDetection = 1;
    	}
    }

	double utility = dartEnv.getProbOfTarget() * probOfDetection;

    return utility;
}

/**
 * computes s(c_t) in the paper
 */
double DartUtilityFunction::getMultiplicativeUtility(
		const pladapt::Configuration& config, const pladapt::Environment& env,
		int time) const {

	auto& dartConfig = dynamic_cast<const DartConfiguration&>(config);
    auto dartEnv = DartEnvironment(dynamic_cast<const JointEnvironment&>(env));

	double probOfDestruction = dartEnv.getProbOfThreat()
			* threat->getProbabilityOfDestruction(dartConfig);

    return 1 - probOfDestruction;
}


double DartUtilityFunction::getFinalReward(
		const pladapt::Configuration& config, const pladapt::Environment& env,
		int time) const {
	return finalReward;
}


DartUtilityFunction::~DartUtilityFunction() {
	// TODO Auto-generated destructor stub
}

} /* namespace am2 */
} /* namespace dart */
