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
#include <pladapt/ConfigurationSpace.h>
#include <stdexcept>
#include <sstream>
using namespace std;

namespace pladapt {

void ConfigurationSpace::insert(Configuration* pConfig) {
	configurations.push_back(pConfig);
}

std::size_t ConfigurationSpace::size() const {
    return configurations.size();
}

const Configuration& ConfigurationSpace::getConfiguration(
        std::size_t index) const {
    return *configurations[index];
}

std::size_t ConfigurationSpace::getIndex(const Configuration& config) const {
    // this does a search, but could be optimized with a map
    std::size_t index = 0;
    while (index < configurations.size()) {
        if (*configurations[index] == config) {
            return index;
        }
        index++;
    }

    stringstream ss;
    ss << "configuration not found: ";
    ss << config;
    throw range_error(ss.str());
}

ConfigurationSpace::~ConfigurationSpace() {
	for (Configuration* pConfig : configurations) {
		delete pConfig;
	}
}

} // namespace
