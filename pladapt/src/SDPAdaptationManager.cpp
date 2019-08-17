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
#include <pladapt/SDPAdaptationManager.h>

#include <stack>
#include <map>
//#include "AllTactics.h"
#include <iostream>

#include <boost/archive/text_oarchive.hpp>
#include <fstream>
#include <stdexcept>
#include <float.h>
#include <boost/filesystem.hpp>

using namespace std;

#define EXTRACT_POLICY 1
#define PRINT_POLICY 0
#define DUMP_REACHABILITY 0
#define SERIALIZE_MATRICES 0

namespace pladapt {

const char* SDPAdaptationManager::NO_LATENCY = "nolatency";
const char* SDPAdaptationManager::REACH_OPTIONS = "reachOptions";
const char* SDPAdaptationManager::REACH_PATH = "reachPath";
const char* SDPAdaptationManager::REACH_PREFIX = "reachPrefix";
const char* SDPAdaptationManager::REACH_MODEL = "reachModel";
const char* SDPAdaptationManager::REACH_SCOPE = "reachScope";


SDPAdaptationManager::SDPAdaptationManager() {
}

std::string SDPAdaptationManager::getAlloyOutputPath(bool immediate) {
    stringstream outputPath;
    outputPath << "reach";
    if (immediate) {
        outputPath << "-i";
    }
    outputPath << '-' << boost::filesystem::path(params[REACH_MODEL].as<string>()).filename().string();
    if (params[REACH_PREFIX].IsDefined()) {
    	outputPath << '-' << params[REACH_PREFIX];
    }
    outputPath << '-' << params[REACH_SCOPE];
    outputPath << ".yaml";
    return outputPath.str();
}

std::string SDPAdaptationManager::getAlloyCommand(bool immediate, std::string outputPath) {
    stringstream command;
    command << params[REACH_PATH];
    command << ' ' << params[REACH_OPTIONS];
    if (immediate) {
        command << " -i";
    }
    command << ' ' << params[REACH_MODEL];
    if (params[NO_LATENCY].as<bool>()) {
        command << "-nl";
    }
    if (!immediate) {
        command << "-step";
    }
    command << ".als";
    command << " \"" << outputPath << "\" ";
    command << params[REACH_SCOPE];
    return command.str();
}


void SDPAdaptationManager::loadImmediateReachabilityRelation() {

    pImmediateReachabilityRelation.reset(new ReachabilityRelation(pConfigMgr->getConfigurationSpace()));

    string outputPath = getAlloyOutputPath(true);
    string command = getAlloyCommand(true, outputPath);

    cout << "invoking " << command << endl;

    if (system(command.c_str()) != 0) {
        throw runtime_error(string("Generation of immediate reachability function failed: ") + command.c_str());
    }

    pImmediateReachabilityRelation->load(outputPath, *pConfigMgr);
#if DUMP_REACHABILITY
    pImmediateReachabilityRelation->dump();
#endif

    unsigned numberOfStates = pImmediateReachabilityRelation->getNumberOfStates();
    pReachableImmediately.reset(new ReachabilityMatrix(numberOfStates, numberOfStates));

    for (unsigned i = 0; i < numberOfStates; i++) {
        ReachabilityRelation::ReachableList reachable = pImmediateReachabilityRelation->getReachableFrom(i);
        for (ReachabilityRelation::ReachableList::const_iterator it = reachable.begin(); it != reachable.end(); ++it) {
            (*pReachableImmediately)(i, it->configIndex) = true;
        }
    }
}

void SDPAdaptationManager::loadReachabilityRelation() {
    pStepReachabilityRelation.reset(new ReachabilityRelation(pConfigMgr->getConfigurationSpace()));
    if (params[NO_LATENCY].as<bool>()) {

        // in the case of zero latency this relation is equal to the immediate relation
        // that is, R^T = R^I
       pReachableFromConfig = pReachableImmediately;

       // the underlying StepReachabilityRelation is the identity
       pStepReachabilityRelation->makeIdentity();
    } else {
        string outputPath = getAlloyOutputPath(false);
        string command = getAlloyCommand(false, outputPath);

        cout << "invoking " << command << endl;

        if (system(command.c_str()) != 0) {
            throw runtime_error(string("Generation of step reachability function failed: ") + command.c_str());
        }

        pStepReachabilityRelation->load(outputPath, *pConfigMgr);
#if DUMP_REACHABILITY
        pStepReachabilityRelation->dump();
#endif

        unsigned numberOfStates = pReachableImmediately->size1();
        pReachableFromConfig.reset(new ReachabilityMatrix(numberOfStates, numberOfStates));
        for (unsigned i = 0; i < numberOfStates; i++) {
            const ReachabilityRelation::ReachableList& reachable = pStepReachabilityRelation->getReachableFrom(i);
            if (!reachable.empty()) {
                assert(reachable.size() == 1);
                unsigned configAfterOneStep = reachable.front().configIndex;
                for (unsigned j = 0; j < numberOfStates; j++) {
                    if (isReachableImmediately(configAfterOneStep, j)) {
                        (*pReachableFromConfig)(i,j) = true;
                    }
                }
            }
        }
    }
}

void SDPAdaptationManager::initialize(std::shared_ptr<const ConfigurationManager> configMgr, const YAML::Node& params)
{
    pConfigMgr = configMgr;
    this->params = params;

    loadImmediateReachabilityRelation();
    loadReachabilityRelation();

#if SERIALIZE_MATRICES
    // serialize
    stringstream outputPath;
    outputPath << "reach.yaml";

    {
        std::ofstream ofs(outputPath.str() + ".immediate");
        boost::archive::text_oarchive oa(ofs);
        oa << *pReachableImmediately;
    }
    {
        std::ofstream ofs(outputPath.str() + ".fromConfig");
        boost::archive::text_oarchive oa(ofs);
        oa << *pReachableFromConfig;
    }
#endif
}


TacticList SDPAdaptationManager::evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
		const UtilityFunction& utilityFunction, unsigned horizon) {

	if (horizon == 0) {
		throw std::invalid_argument("SDPAdaptationManager::evaluate() called with horizon = 0");
	}

	/* check if we need to adjust the horizon to the environment size */
	if ((envDTMC.getNumberOfParts() - 1) < horizon) {
		if (envDTMC.getNumberOfParts() > 1 && envDTMC.isLastPartFinal()) {
			horizon = envDTMC.getNumberOfParts() - 1;
			cout << "warning: environment is shorter than horizon" << endl;
		}
	}

    const ConfigurationSpace& configSpace = pConfigMgr->getConfigurationSpace();
    unsigned currentConfig = configSpace.getIndex(currentConfigObj);
    if (debug) {
        cout << "current config: " << currentConfigObj << " (" << currentConfig << ')' << endl;
    }

    typedef std::pair<unsigned, unsigned> SystemEnvPair; // (sysIndex, envIndex)
    typedef map<SystemEnvPair, double> StateUtility;

    StateUtility* pUtil = new StateUtility;
    StateUtility* pNextUtil = 0;

    /*
     * Since the state transition determines the action, the optimal policy
     * is recorded as the state that should follow a state at time t
     *
     * Note: we don't really need the whole policy because the best state at time 0
     * determines the tactics that must be started.
     */
#if EXTRACT_POLICY
    unsigned policy[horizon][configSpace.size()];
#endif


    // compute utility at the horizon
    unsigned t = horizon;
    for (unsigned s = 0; s < configSpace.size(); s++) {
        const Configuration& config = configSpace.getConfiguration(s);

        unsigned partIndex = min(t, envDTMC.getNumberOfParts() - 1);
        const DTMCPartitionedStates::Part& envPart = envDTMC.getPart(partIndex);
        for (DTMCPartitionedStates::Part::const_iterator envState = envPart.begin();
                envState != envPart.end(); envState++) {
                assert(pUtil->find(SystemEnvPair(s, *envState)) == pUtil->end());
                (*pUtil)[SystemEnvPair(s, *envState)] =
                    utilityFunction.getMultiplicativeUtility(config, envDTMC.getStateValue(*envState), t)
                    * (utilityFunction.getAdditiveUtility(config, envDTMC.getStateValue(*envState), t)
                           + utilityFunction.getFinalReward(config, envDTMC.getStateValue(*envState), t));
                if (debug) {
                    cout << "t=" << t << endl;
                    cout << "util(" << config << ", " << envDTMC.getStateValue(*envState) << ")="
                            << (*pUtil)[SystemEnvPair(s, *envState)] << endl;
                }
        }
    }

    while (t > 0) {
        t--;
        if (debug) {
            cout << "==>> t=" << t << endl;
        }

        // make last computed utilities, the next
        if (pNextUtil) {
            delete pNextUtil;
        }
        pNextUtil = pUtil;
        pUtil = new StateUtility;

        for (unsigned s = 0; s < configSpace.size(); s++) {

            // t = 0 is now before adapting, so only the current config is valid
            if (t == 0 && s != currentConfig) {
                continue;
            }

            // if it isn't reachable now (i.e., not a valid initial state), don't bother
            // note that t=1 is the first decision period. That is, the env at t=1
            // is the env we will have during the period that is about to start,
            // which will be preceded, possibly, by immediate adaptations
            if (t == 1 && !isReachableImmediately(currentConfig, s)) {
                if (debug) {
                    cout << "Not reachable: " << configSpace.getConfiguration(s) << '(' << s << ')' << endl;
                }
                continue;
            }

            if (debug) {
                cout << "Reachable: " << configSpace.getConfiguration(s) << '(' << s << ')' << endl;
            }

            const Configuration& config = configSpace.getConfiguration(s);

            unsigned partIndex = min(t, envDTMC.getNumberOfParts() - 1);
            const DTMCPartitionedStates::Part& envPart = envDTMC.getPart(partIndex);
            for (DTMCPartitionedStates::Part::const_iterator envState = envPart.begin();
                    envState != envPart.end(); envState++) {
#if PLADAPT_SDP_NOPARTITION
                if (t == 0 && *envState != 0) {
                    continue;
                }
#endif
                assert(t > 0 || *envState == 0); // we should only have the root env state at t=0

#if EXTRACT_POLICY
                unsigned bestNextState = 0; // assume this for now
#endif
                double maxUtil = -DBL_MAX;
                bool firstReachableState = true;

                for (unsigned nextS = 0; nextS < configSpace.size(); nextS++) {
                    if ((t == 0 && !isReachableImmediately(s, nextS))
                            || (t > 0 && !isReachableFromConfig(s, nextS))) {
                        continue;
                    }
                    double util = utilityFunction.getAdaptationReward(config, configSpace.getConfiguration(nextS), t);

                    unsigned nextPartIndex = min(t + 1, envDTMC.getNumberOfParts() - 1);
                    const DTMCPartitionedStates::Part& nextEnvPart = envDTMC.getPart(nextPartIndex);
                    for (DTMCPartitionedStates::Part::const_iterator nextEnvState = nextEnvPart.begin();
                            nextEnvState != nextEnvPart.end(); nextEnvState++) {
                        SystemEnvPair nextSysEnvPair(nextS, *nextEnvState);
                        //assert(pNextUtil->find(nextSysEnvPair) != pNextUtil->end());
                        try {
                            util += envDTMC.getTransitionMatrix()(*envState, *nextEnvState)
                                    * pNextUtil->at(nextSysEnvPair);
                        } catch(...) {
                            cout << "didn't find util for (" << configSpace.getConfiguration(nextSysEnvPair.first)
                                    << ',' << envDTMC.getStateValue(nextSysEnvPair.second) << " at t=" << t + 1 << endl;
                            assert(false);
                        }
                    }

                    if (firstReachableState || util >= maxUtil) {

                        // for equal utility, prefer the current configuration
                        if (firstReachableState || nextS == currentConfig || util > maxUtil) {
                            maxUtil = util;
#if EXTRACT_POLICY
                            bestNextState = nextS;
#endif
                        }
                    }
                    firstReachableState = false;
                }

                double localUtil = 0;
                double multiplicativeUtil = 1.0;
                /*
                 * if t = 0 we're at the current state, so we don't need to
                 * add the local utility.
                 * HOWEVER if we extend this to have rewards due to actions,
                 * we do need to add that here (as well as above)
                 */
                if (t > 0) {
                    localUtil = utilityFunction.getAdditiveUtility(config, envDTMC.getStateValue(*envState), t);
                    multiplicativeUtil = utilityFunction.getMultiplicativeUtility(config, envDTMC.getStateValue(*envState), t);
                }

                maxUtil = multiplicativeUtil * (localUtil + maxUtil);

                if (debug) {
                    cout << "\tt=" << t << " util=" << maxUtil << " locUtil=" << localUtil << " multUtil=" << multiplicativeUtil
                            << " env=" << envDTMC.getStateValue(*envState)
                            << " -> " << configSpace.getConfiguration(bestNextState) << '(' << bestNextState << ')' << endl;
                }

                assert(pUtil->find(SystemEnvPair(s, *envState)) == pUtil->end());
                (*pUtil)[SystemEnvPair(s, *envState)] = maxUtil;
#if EXTRACT_POLICY
                policy[t][s] = bestNextState;
#endif
            }
        }
    }

    // now we need to find the best initial state
    unsigned bestInitialState = policy[0][currentConfig];

#if SUPPORTS_GET_STRATEGY
    lastStrategy = make_shared<Strategy>();
#endif

#if PRINT_POLICY
    stringstream policyStr;
    policyStr << "policy @t0: " << configSpace.getConfiguration(bestInitialState) << ": [";
#endif

    // find the tactics that should be started, if any
    TacticList tactics;
    bool foundEntry = false;
    const ReachabilityRelation::ReachableList& reachable = pImmediateReachabilityRelation->getReachableFrom(currentConfig);
    for (ReachabilityRelation::ReachableList::const_iterator entry = reachable.begin(); entry != reachable.end(); ++entry) {
        if (entry->configIndex == bestInitialState) {
            foundEntry = true;
            tactics = entry->tactics;
#if SUPPORTS_GET_STRATEGY
            lastStrategy->push_back(tactics);
#endif

#if PRINT_POLICY
            if (entry->tactics.size() > 0) {
                for (const auto& tactic : entry->tactics) {
                    policyStr << ' ' << tactic;
                }
            }
#endif
        }
    }
    assert(foundEntry); // should've found it

#if PRINT_POLICY
    policyStr << "]" << endl;
#endif
#if SUPPORTS_GET_STRATEGY || PRINT_POLICY
    for (unsigned t = 1; t < horizon; t++) {
        unsigned configAfterOneStep = pStepReachabilityRelation->getReachableFrom(bestInitialState).front().configIndex;
        unsigned lastState = bestInitialState;
        bestInitialState = policy[t][bestInitialState];
#if PRINT_POLICY
        policyStr << "policy @t" << t << ": " << configSpace.getConfiguration(bestInitialState) << ": [";
#endif
        const ReachabilityRelation::ReachableList& reachable = pImmediateReachabilityRelation->getReachableFrom(configAfterOneStep);
        bool found = false;
        for (ReachabilityRelation::ReachableList::const_iterator entry = reachable.begin(); entry != reachable.end(); ++entry) {
            if (entry->configIndex == bestInitialState) {
                found = true;
#if SUPPORTS_GET_STRATEGY
                lastStrategy->push_back(entry->tactics);
#endif
#if PRINT_POLICY
				for (const auto& tactic : entry->tactics) {
						policyStr << ' ' << tactic;
				}
#endif
				break;
            }
        }
#if PRINT_POLICY
        policyStr << "]" << endl;
#endif
        if (!found) {
            cout << "Print/get policy error: can't find transition from "
                    << configSpace.getConfiguration(lastState)
                    << " to "
                    << configSpace.getConfiguration(bestInitialState)
                    << endl;
        }
    }
#if PRINT_POLICY
    cout << policyStr.str();
#endif
#endif

    if (pUtil) delete pUtil;
    if (pNextUtil) delete pNextUtil;

    return tactics;
}

bool SDPAdaptationManager::isReachableImmediately(unsigned fromConfigIndex,
        unsigned toConfigIndex) const {
    return (*pReachableImmediately)(fromConfigIndex, toConfigIndex);
}

bool SDPAdaptationManager::isReachableFromConfig(unsigned fromConfigIndex,
        unsigned toConfigIndex) const {
    return (*pReachableFromConfig)(fromConfigIndex, toConfigIndex);
}

bool SDPAdaptationManager::supportsStrategy() const {
#if SUPPORTS_GET_STRATEGY
	return true;
#else
	return false;
#endif
}

std::shared_ptr<Strategy> SDPAdaptationManager::getStrategy() {
	return lastStrategy;
}

SDPAdaptationManager::~SDPAdaptationManager() {
}

} // namespace

