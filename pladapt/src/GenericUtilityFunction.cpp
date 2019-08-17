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

#include <pladapt/GenericUtilityFunction.h>

namespace pladapt {

GenericUtilityFunction::~GenericUtilityFunction() {
	// TODO Auto-generated destructor stub
}

double GenericUtilityFunction::getAdditiveUtility(const Configuration& config,
		const Environment& environment, int time) const {
	return getGenAdditiveUtility(
			dynamic_cast<const GenericConfiguration&>(config),
			dynamic_cast<const GenericEnvironment&>(environment), time);
}

double GenericUtilityFunction::getMultiplicativeUtility(
		const Configuration& config, const Environment& environment, int time) const {
	return getGenMultiplicativeUtility(
			dynamic_cast<const GenericConfiguration&>(config),
			dynamic_cast<const GenericEnvironment&>(environment), time);
}

double GenericUtilityFunction::getFinalReward(const Configuration& config,
		const Environment& environment, int time) const {
	return getGenFinalReward(
			dynamic_cast<const GenericConfiguration&>(config),
			dynamic_cast<const GenericEnvironment&>(environment), time);
}

double GenericUtilityFunction::getAdaptationReward(const Configuration& from,
		const Configuration& to, int time) const {
	return getGenAdaptationReward(
			dynamic_cast<const GenericConfiguration&>(from),
			dynamic_cast<const GenericConfiguration&>(to), time);
}

double GenericUtilityFunction::getGenAdditiveUtility(
		const GenericConfiguration& config, const GenericEnvironment& environment, int time) const {
	return 0.0;
}

double GenericUtilityFunction::getGenMultiplicativeUtility(
		const GenericConfiguration& config, const GenericEnvironment& environment, int time) const {
	return 1.0;
}

double GenericUtilityFunction::getGenFinalReward(const GenericConfiguration& config,
		const GenericEnvironment& environment, int time) const {
	return 0.0;
}

double GenericUtilityFunction::getGenAdaptationReward(
		const GenericConfiguration& from, const GenericConfiguration& to, int time) const {
	return 0.0;
}

} /* namespace pladapt */
