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

#include <pladapt/EnvironmentDTMCPartitioned.h>
#include <pladapt/JointEnvironment.h>
#include <stdexcept>

namespace pladapt {

EnvironmentDTMCPartitioned::EnvironmentDTMCPartitioned(unsigned numberOfStates)
	: DTMCPartitionedStates(numberOfStates), stateValues(numberOfStates) {
}

void EnvironmentDTMCPartitioned::setStateValue(unsigned state,
        std::shared_ptr<Environment> pValue) {
	stateValues[state] = std::move(pValue);
}

const Environment& EnvironmentDTMCPartitioned::getStateValue(unsigned state) const {
	const auto& value = stateValues[state];
	if (!value) {
		throw std::logic_error("EnvironmentDTMCPartitioned::getStateValue() value not set");
	}
	return *value;
}

const std::shared_ptr<Environment>& EnvironmentDTMCPartitioned::getSharedStateValue(unsigned state) {
    return stateValues[state];
}

EnvironmentDTMCPartitioned::~EnvironmentDTMCPartitioned() {
}

/**
 * Creates a joint partitioned DTMC
 *
 * Both input DTMCs must have the same number of parts.
 * The states only join with states in the same part
 */
EnvironmentDTMCPartitioned EnvironmentDTMCPartitioned::createJointDTMC(EnvironmentDTMCPartitioned& a, EnvironmentDTMCPartitioned& b) {
    assert(a.getNumberOfParts() == b.getNumberOfParts());

    unsigned numberOfStates = 0;
    for (unsigned p = 0; p < a.getNumberOfParts(); p++) {
        numberOfStates += a.getPart(p).size() * b.getPart(p).size();
    }

    unsigned int newStates[a.getNumberOfStates()][b.getNumberOfStates()]; // map from pair of old state index to joint state index

    EnvironmentDTMCPartitioned joint(numberOfStates);
    unsigned int jointState = 0;
    for (unsigned p = 0; p < a.getNumberOfParts(); p++) {
        auto partA = a.getPart(p);
        auto partB = b.getPart(p);
        for (auto stateA : partA) {
            for (auto stateB : partB) {
                newStates[stateA][stateB] = jointState;
                joint.setStateValue(jointState, std::make_shared<JointEnvironment>(a.getSharedStateValue(stateA), b.getSharedStateValue(stateB)));
                joint.assignToPart(p, jointState);
                if (p > 0) {
                    auto prevPartA = a.getPart(p - 1);
                    auto prevPartB = b.getPart(p - 1);
                    for (auto prevA : prevPartA) {
                        for (auto prevB : prevPartB) {
                            joint.tm(newStates[prevA][prevB], jointState)
                                    = a.tm(prevA, stateA) * b.tm(prevB, stateB);
                        }
                    }
                }
                jointState++;
            }
        }
    }

    assert(jointState == numberOfStates); // we created all the joint states

    return joint;
}


void EnvironmentDTMCPartitioned::printOn(std::ostream& os) const {
    for (unsigned from = 0; from < tm.size1(); ++from) {
        os << "from: " << from << ' ' << getStateValue(from) << std::endl;
        for (unsigned to = 0; to < tm.size2(); ++to) {
            if (tm(from, to) > 0.0) {
                os << "  to: " << to << ' ' << getStateValue(to) << " p=" << tm(from, to) << std::endl;
            }
        }
    }
}

std::ostream& operator<<(std::ostream& os, const EnvironmentDTMCPartitioned& envDTMC) {
    envDTMC.printOn(os);
    return os;
}

} // namespace
