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

#ifndef _PLADAPT_CONFIGURATIONMANAGER_H_
#define _PLADAPT_CONFIGURATIONMANAGER_H_

#include <pladapt/ConfigurationSpace.h>
#include <yaml-cpp/yaml.h>
#include <memory>

namespace pladapt {

/**
 * Derived classes must know the parameters of the configuration space
 * (e.g., max number of servers, number of formations, etc.) to be able
 * to generate the configuration space
 */
class ConfigurationManager {
public:

    /**
     * Returns the configuration space
     */
    virtual const ConfigurationSpace& getConfigurationSpace() const = 0;

    /**
     * Parses a YAML node to get a configuration
     */
    virtual std::unique_ptr<Configuration> getConfigurationFromYaml(const YAML::Node& configDetails) const = 0;


	virtual ~ConfigurationManager();
};

}

#endif /* _PLADAPT_CONFIGURATIONMANAGER_H_ */
