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

#ifndef THREAT_H_
#define THREAT_H_

#include "RealEnvironment.h"
#include "DartConfiguration.h"
#include <random>

namespace dart {
namespace am2 {

/**
 * Simulates the effect of threats present in the environment
 */
class Threat {
public:
	Threat(double range, double destructionFormationFactor);
	virtual ~Threat();

	/**
	 * Simulates the effect of a (possible) threat
	 *
	 * @param threatEnv real threat environment
	 * @param config current team configuration
	 * @param location current team location
	 * @return true if destroyed by threat
	 */
	virtual bool isDestroyed(const RealEnvironment& threatEnv, const DartConfiguration& config, const Coordinate& location);

	/**
	 * Computes probability of destruction given that there is a threat
	 */
	virtual double getProbabilityOfDestruction(const DartConfiguration& config);

protected:
	double range;
	double destructionFormationFactor;
	std::uniform_real_distribution<> uniform;
	std::default_random_engine randomGenerator;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* THREAT_H_ */
