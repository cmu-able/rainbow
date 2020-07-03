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

#ifndef DARTENVIRONMENT_H_
#define DARTENVIRONMENT_H_

#include <pladapt/Environment.h>
#include <pladapt/JointEnvironment.h>

namespace dart {
namespace am2 {

/**
 * This class represents the joint environment state
 * including threats and targets
 */
class DartEnvironment : public pladapt::Environment {
public:
	DartEnvironment(double probOfThreat, double probOfTarget);

	/**
	 * This assumes that the joint env has two components:
	 * threats and targets (in that order)
	 */
	DartEnvironment(const pladapt::JointEnvironment& jointEnv);
	virtual ~DartEnvironment();

	virtual void printOn(std::ostream& os) const;

	double getProbOfTarget() const {
		return probOfTarget;
	}

	double getProbOfThreat() const {
		return probOfThreat;
	}

protected:
	double probOfThreat;
	double probOfTarget;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* DARTENVIRONMENT_H_ */
