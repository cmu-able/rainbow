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

#ifndef PLADAPT_JAVASDPADAPTATIONMANAGER_H_
#define PLADAPT_JAVASDPADAPTATIONMANAGER_H_

#include <pladapt/SDPAdaptationManager.h>
#include <pladapt/GenericConfigurationManager.h>
#include <pladapt/GenericUtilityFunction.h>
#include <iostream>
#include <vector>

namespace pladapt {

class JavaSDPAdaptationManager : public SDPAdaptationManager {
public:
	virtual ~JavaSDPAdaptationManager();

    virtual void initialize(std::shared_ptr<const GenericConfigurationManager> configMgr, const std::string& YAMLParams) {
    	SDPAdaptationManager::initialize(std::static_pointer_cast<const ConfigurationManager, const GenericConfigurationManager>(configMgr),
    			YAML::Load(YAMLParams));
    }

    /*
     * we're changing the return type, so we cannot simple override evaluate()
     * we use rename in SWIG to change the name back to evaluate()
     */
    virtual std::vector<std::string> evaluateWrapper(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
    		const UtilityFunction& utilityFunction, unsigned horizon) {
    	// need to convert result to vector
    	auto tactics = SDPAdaptationManager::evaluate(currentConfigObj, envDTMC, utilityFunction, horizon);
    	std::vector<std::string> result(tactics.begin(), tactics.end());
    	return result;
    }
};

} /* namespace pladapt */

#endif /* PLADAPT_JAVASDPADAPTATIONMANAGER_H_ */
