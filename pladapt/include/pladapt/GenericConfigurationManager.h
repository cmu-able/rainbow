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

#ifndef PLADAPT_GENERICCONFIGURATIONMANAGER_H_
#define PLADAPT_GENERICCONFIGURATIONMANAGER_H_

#include <pladapt/ConfigurationManager.h>
#include <pladapt/GenericConfiguration.h>

namespace pladapt {

/**
 * Generic configuration manager
 *
 * A configuration template has to be created by getting a configuration with
 * getConfigurationTemplate() and adding all the properties to it.
 *
 * New configurations can be added to the configuration space by invoking
 * addNewConfiguration(), and setting properties in the returned instance.
 */
class GenericConfigurationManager: public ConfigurationManager {
public:
	virtual ~GenericConfigurationManager();

	virtual const pladapt::ConfigurationSpace& getConfigurationSpace() const;

	virtual GenericConfiguration& getConfigurationTemplate();

	/**
	 * Builds a GenericConfiguration getting properties from the YAML node
	 * as defined by the properties set in the template
	 *
	 * @see getConfigurationTemplate()
	 */
    virtual std::unique_ptr<pladapt::Configuration> getConfigurationFromYaml(const YAML::Node& configDetails) const;

    /**
     * Adds a new empty configuration to the configuration space
     *
     * This empty configuration can then be used to set its properties.
     */
    virtual GenericConfiguration& addNewConfiguration();

protected:
    ConfigurationSpace configSpace;
    GenericConfiguration configTemplate;
};

} /* namespace pladapt */

#endif /* PLADAPT_GENERICCONFIGURATIONMANAGER_H_ */
