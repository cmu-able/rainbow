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

#include <dartam/DartEnvironment.h>
#include <dartam/DartSimpleEnvironment.h>

namespace dart {
namespace am2 {

DartEnvironment::DartEnvironment(double probOfThreat, double probOfTarget)
	: probOfThreat(probOfThreat), probOfTarget(probOfTarget)
{
}

DartEnvironment::DartEnvironment(const pladapt::JointEnvironment& jointEnv) {
    auto& threatEnv = dynamic_cast<const DartSimpleEnvironment&>(jointEnv.getComponent(0));
    auto& targetEnv = dynamic_cast<const DartSimpleEnvironment&>(jointEnv.getComponent(1));

    probOfThreat = threatEnv.getProbOfObject();
    probOfTarget = targetEnv.getProbOfObject();
}

void DartEnvironment::printOn(std::ostream& os) const {
	os << "P(threat)=" << probOfThreat
			<< "P(target)=" << probOfTarget;
}

DartEnvironment::~DartEnvironment() {
}

} /* namespace am2 */
} /* namespace dart */
