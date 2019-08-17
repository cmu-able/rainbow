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

#ifndef DARTDTMCENVIRONMENT_H_
#define DARTDTMCENVIRONMENT_H_

#include <pladapt/EnvironmentDTMCPartitioned.h>
#include "EnvironmentMonitor.h"

namespace dart {
namespace am2 {

class DartDTMCEnvironment: public pladapt::EnvironmentDTMCPartitioned {
public:

	/**
	 * Different approaches to discretize the environment probability distributtions
	 *
	 * E_PT : Extended Pearson-Tuckey (3-point discretization)
	 * POINT : single point estimate (mean)
	 */
	enum DistributionApproximation { E_PT, POINT };

	DartDTMCEnvironment(const EnvironmentMonitor& envMonitor,
			const Route& route,
			DistributionApproximation approx = DistributionApproximation::E_PT);
	virtual ~DartDTMCEnvironment();
};

} /* namespace am2 */
} /* namespace dart */

#endif /* DARTDTMCENVIRONMENT_H_ */
