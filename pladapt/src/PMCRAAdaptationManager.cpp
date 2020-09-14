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

#include <pladapt/PMCRAAdaptationManager.h>
#include <iostream>
#include <sstream>
#include <pladapt/PRISMWrapper.h>

using namespace std;

namespace pladapt {

const char* PMCRAAdaptationManager::PROBABILITY_BOUND = "probabilityBound";
const std::string PCTL_A = "multi(Rmax=? [ C ], P>=";
const std::string PCTL_B = " [ G satisfied ])";
const std::string PCTL_BEST_EFFORT = "Pmax=? [ G satisfied ]";

const std::vector<std::string> PRISM_OPTIONS_FOR_RA = { "-s", "-lp" };
const std::vector<std::string> PRISM_OPTIONS_DEFAULT = { };

PMCRAAdaptationManager::~PMCRAAdaptationManager() {
	// TODO Auto-generated destructor stub
}


void PMCRAAdaptationManager::initialize(std::shared_ptr<const ConfigurationManager> configMgr, const YAML::Node& params,
     		std::shared_ptr<const PMCHelper> helper) {
	PMCAdaptationManager::initialize(configMgr, params, helper);

	if (params[PROBABILITY_BOUND].IsDefined()) {
		survivalRequirement = params[PROBABILITY_BOUND].as<double>();
	}
}

TacticList PMCRAAdaptationManager::evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
    		const UtilityFunction& utilityFunction, unsigned horizon) {

	/* check if we need to adjust the horizon to the environment size */
	if ((envDTMC.getNumberOfParts() - 1) < horizon) {
		if (envDTMC.getNumberOfParts() > 1 && envDTMC.isLastPartFinal()) {
			horizon = envDTMC.getNumberOfParts() - 1;
			cout << "warning: environment is shorter than horizon" << endl;
		}
	}

	string initialState = pMcHelper->generateInitializations(currentConfigObj, utilityFunction, horizon);

    string environmentModel = generateEnvironmentDTMC(envDTMC);

    string templatePath = params[TEMPLATE_PATH].as<string>();
    if (params[NO_LATENCY].as<bool>()) {
    	templatePath += "-nl";
    }

    templatePath += ".prism";

    PRISMWrapper planner;
    planner.setModelTemplatePath(templatePath);
    string* pPath = 0;
    if (debug) {
        pPath = new string;
    }

    stringstream pctl;
    pctl << PCTL_A;
    pctl << survivalRequirement;
    pctl << PCTL_B;

    std::vector<std::string> tactics;
    try {
    	planner.setPrismOptions(PRISM_OPTIONS_FOR_RA);
    	tactics = planner.plan(environmentModel, initialState, pctl.str(), pPath);
    } catch (std::domain_error&) {
    	cout << "Survivability requirement cannot be met < " << survivalRequirement << endl;
    	planner.setPrismOptions(PRISM_OPTIONS_DEFAULT);
    	tactics = planner.plan(environmentModel, initialState, PCTL_BEST_EFFORT, pPath);
    }


    TacticList result;
    // remove sufixes from tactic names (everything after after (and including) an underscore)
    for (auto& tactic : tactics) {
    	auto pos = tactic.find('_');
    	if (pos != string::npos) {
    		tactic.erase(pos);
    	}
    	result.insert(tactic);
    }

    if (pPath) {
        cout << "*debug path " << *pPath << endl;
        delete pPath;
    }

    return result;
}


} /* namespace pladapt */
