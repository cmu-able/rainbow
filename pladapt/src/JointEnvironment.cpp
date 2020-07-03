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

#include <pladapt/JointEnvironment.h>
#include <typeinfo>

namespace pladapt {

JointEnvironment::~JointEnvironment() {
	// TODO Auto-generated destructor stub
}

/*
 * share_ptr& because we take ownership only if it
 * is not an instance of JointEnvironment
 */
JointEnvironment::JointEnvironment(
		const std::shared_ptr<Environment>& componentA,
		const std::shared_ptr<Environment>& componentB) {

	try {
		JointEnvironment& a = dynamic_cast<JointEnvironment&>(*componentA);
		for (unsigned c = 0; c < a.getNumberOfComponents(); c++) {
			components.push_back(a.getSharedComponent(c));
		}
	} catch(std::bad_cast&) {
		// it isn't a joint environment
		//components.push_back(std::move(componentA));
		components.push_back(componentA);
	}

	try {
		JointEnvironment& b = dynamic_cast<JointEnvironment&>(*componentB);
		for (unsigned c = 0; c < b.getNumberOfComponents(); c++) {
			components.push_back(b.getSharedComponent(c));
		}
	} catch(std::bad_cast&) {
		// it isn't a joint environment
		//components.push_back(std::move(componentB));
		components.push_back(componentB);
	}
}


unsigned JointEnvironment::getNumberOfComponents() const {
	return components.size();
}

std::shared_ptr<Environment> JointEnvironment::getSharedComponent(
		unsigned c) {
	return components[c];
}

const Environment& JointEnvironment::getComponent(unsigned c) const {
	return *components[c];
}

Environment::EnvironmentClass JointEnvironment::getType() const {
	return Environment::C_JOINT_ENVIRONMENT;
}

void JointEnvironment::printOn(std::ostream& os) const {
	os << '[';
	for (auto env : components) {
		os << *env << ' ';
	}
	os << ']';
}

} /* namespace pladapt */
