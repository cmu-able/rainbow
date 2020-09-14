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

#include <pladapt/GenericConfigurationManager.h>

namespace pladapt {

GenericConfigurationManager::~GenericConfigurationManager() {
	// TODO Auto-generated destructor stub
}

const pladapt::ConfigurationSpace& GenericConfigurationManager::getConfigurationSpace() const {
	return configSpace;
}

GenericConfiguration& GenericConfigurationManager::getConfigurationTemplate() {
	return configTemplate;
}

std::unique_ptr<pladapt::Configuration> GenericConfigurationManager::getConfigurationFromYaml(
		const YAML::Node& configDetails) const {
    auto config = new GenericConfiguration;

	// iterate over all the properties of configTemplate and try to get them from the node with the appropriate type
	for (auto const& p : configTemplate) {
		const YAML::Node& property = configDetails[p.first];
		if (!property) {
			throw std::runtime_error(std::string("GenericConfigurationManager::getConfigurationFromYaml() couldn't find value for ") + p.first);
		}
		auto val = p.second;
		if (val.type() == typeid(int)) {
		    config->set(p.first, property.as<int>());
		} else if (val.type() == typeid(bool)) {
		    config->set(p.first, property.as<bool>());
		} else if (val.type() == typeid(double)) {
		    config->set(p.first, property.as<double>());
		} else {
			throw std::runtime_error(std::string("GenericProperties not able to handle type ") + val.type().name() + " for variable " + p.first);
		}
	}
	return std::unique_ptr<pladapt::Configuration>(config);
}

GenericConfiguration& GenericConfigurationManager::addNewConfiguration() {
	auto config = new GenericConfiguration;
	configSpace.insert(config);
	return *config;
}

} /* namespace pladapt */
