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

#ifndef TARGETSENSOR_H_
#define TARGETSENSOR_H_

#include "DartConfiguration.h"
#include <random>

namespace dart {
namespace am2 {

class TargetSensor {
public:
	TargetSensor(double range, double detectionFormationFactor);
	virtual ~TargetSensor();
	virtual bool sense(const DartConfiguration& config, bool targetPresent);

	/**
	 * Compute probability of detection given that there is a target
	 */
	virtual double getProbabilityOfDetection(const DartConfiguration& config);

protected:
	double range;
	double detectionFormationFactor;
	std::uniform_real_distribution<> uniform;
	std::default_random_engine randomGenerator;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* TARGETSENSOR_H_ */
