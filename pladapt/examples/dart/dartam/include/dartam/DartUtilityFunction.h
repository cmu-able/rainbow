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

#ifndef DARTUTILITYFUNCTION_H_
#define DARTUTILITYFUNCTION_H_

#include <pladapt/UtilityFunction.h>
#include "TargetSensor.h"
#include "Threat.h"
#include <memory>

namespace dart {
namespace am2 {

class DartUtilityFunction: public pladapt::UtilityFunction {
public:

	/**
	 * @param threat this is used to compute the probability of being hit by the threat
	 * @param targetSensor this is used to compute the probability of detecting a target
	 * @param finalReward value for the final reward (obtained at the end of the horizon)
	 */
	DartUtilityFunction(std::shared_ptr<Threat> threat, std::shared_ptr<TargetSensor> targetSensor,
			double finalReward);
	virtual double getAdditiveUtility(const pladapt::Configuration& config, const pladapt::Environment& env, int time) const;
    virtual double getMultiplicativeUtility(const pladapt::Configuration& config, const pladapt::Environment& env, int time) const;
    virtual double getFinalReward(const pladapt::Configuration& config, const pladapt::Environment& env, int time) const;
    virtual ~DartUtilityFunction();

protected:
    std::shared_ptr<Threat> threat;
    std::shared_ptr<TargetSensor> targetSensor;
    double finalReward;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* DARTUTILITYFUNCTION_H_ */
