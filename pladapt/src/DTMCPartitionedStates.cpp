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
#include <pladapt/DTMCPartitionedStates.h>

namespace pladapt {

DTMCPartitionedStates::DTMCPartitionedStates(unsigned numberOfStates) : tm(numberOfStates, numberOfStates) {
}

std::size_t DTMCPartitionedStates::getNumberOfStates() const {
	return tm.size1();
}

const TransitionMatrix& DTMCPartitionedStates::getTransitionMatrix() const {
    return tm;
}

TransitionMatrix& DTMCPartitionedStates::getTransitionMatrix() {
    return tm;
}

void DTMCPartitionedStates::setTransitionProbability(unsigned from, unsigned to, double probability) {
	tm(from, to) = probability;
}

void DTMCPartitionedStates::assignToPart(unsigned partIndex,
        unsigned state) {
    while (partition.size() <= partIndex) {
        partition.push_back(Part());
    }
    partition[partIndex].insert(state);
}

unsigned DTMCPartitionedStates::getNumberOfParts() const {
    return partition.size();
}

const DTMCPartitionedStates::Part& DTMCPartitionedStates::getPart(unsigned index) const {
    return partition[index];
}


bool DTMCPartitionedStates::isLastPartFinal() const {
	for (const auto& state : partition[partition.size() - 1]) {

		/* check self-transition first */
		if (tm(state, state) > 0.0) {
			return false;
		}

		for (const auto& nextState : partition[partition.size() - 1]) {
			if (tm(state, nextState) > 0.0) {
				return false;
			}
		}
	}
	return true;
}


DTMCPartitionedStates::~DTMCPartitionedStates() {
    // TODO Auto-generated destructor stub
}

} // namespace
