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

#ifndef __PLADAPT_PMCADAPTATIONMANAGER_H_
#define __PLADAPT_PMCADAPTATIONMANAGER_H_

#include "pladapt/AdaptationManager.h"
#include <pladapt/ConfigurationManager.h>
#include <memory>
#include <yaml-cpp/yaml.h>

namespace pladapt {

/**
 * Helper to customize the initialization part of the PRISM template
 */
class PMCHelper {
public:

	/**
	 * This method has to generate the initialization section to inject into
	 * the PRISM template
	 *
	 * Note: it is likely that this method will require some parameters that
	 * are also used by the utility function. Since both this class and
	 * the utility function must be customized for each problem, if some of
	 * that information is need by this method, then the matching utility
	 * function can provide access to that information through an interface
	 * agreed upon this method and the particular utility function class.
	 */
	virtual std::string generateInitializations(const Configuration& currentConfigObj,
			const UtilityFunction& utilityFunction, unsigned horizon) const = 0;
	virtual ~PMCHelper();
};

/**
 * Adaptation manager using Probabilistic Model Checking approach
 */
class PMCAdaptationManager : public AdaptationManager
{
public:

   /**
	 * params must include:
	 *   TEMPLATE_PATH: path to the PRISM template file, not including the
	 *   	extension ".prism"
	 *   NO_LATENCY: boolean, true if no tactic has latency. In that case,
	 *   	"-nl" will be appended the TEMPLATE_PATH before loading it
	 */
    virtual void initialize(std::shared_ptr<const ConfigurationManager> configMgr, const YAML::Node& params,
    		std::shared_ptr<const PMCHelper> helper);
    virtual TacticList evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
    		const UtilityFunction& utilityFunction, unsigned horizon);

    static std::string generateEnvironmentDTMC(const EnvironmentDTMCPartitioned& envDTMC);

    static const char* NO_LATENCY;
    static const char* TEMPLATE_PATH;

protected:
    std::shared_ptr<const ConfigurationManager> pConfigMgr;
    std::shared_ptr<const PMCHelper> pMcHelper;

    YAML::Node params;
};

}

#endif
