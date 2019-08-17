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

#ifndef _PLADAPT_SDPADAPTATIONMANAGER_H_
#define _PLADAPT_SDPADAPTATIONMANAGER_H_

#include <pladapt/AdaptationManager.h>
#include <pladapt/EnvironmentDTMCPartitioned.h>
#include <pladapt/ReachabilityRelation.h>
#include <pladapt/UtilityFunction.h>
#include <boost/numeric/ublas/matrix_sparse.hpp>
#include <memory>
#include <set>
#include <string>
#include <yaml-cpp/yaml.h>

typedef boost::numeric::ublas::mapped_matrix<bool> ReachabilityMatrix;


// set to one to remove the optimization of partitioned environment state space
#define PLADAPT_SDP_NOPARTITION 0

// set to one to support getting a full strategy after a call to evaluate()
#define SUPPORTS_GET_STRATEGY 1


namespace pladapt {

/**
 * Adaptation manager using Alloy+SDP approach
 */
class SDPAdaptationManager : public AdaptationManager
{
	std::string reachArgs;

  protected:
    std::unique_ptr<ReachabilityRelation> pImmediateReachabilityRelation; /**< the isReachableImmediately relation annotated with tactics*/
    std::unique_ptr<ReachabilityRelation> pStepReachabilityRelation; /**< the reachability relation for the passing of one time step */

    std::shared_ptr<ReachabilityMatrix> pReachableImmediately; /**< the isReachableImmediately relation */

    /**
     * The isReachableFromConfig relation.
     *
     * Config c' is reachable from c if ct is the config that results after one
     * period elapses, and c' is reachable immediately from ct
     */
    std::shared_ptr<ReachabilityMatrix> pReachableFromConfig;

    std::shared_ptr<const ConfigurationManager> pConfigMgr;
    YAML::Node params;

    std::shared_ptr<Strategy> lastStrategy; /**< last strategy computed by evaluate() */

    /**
     * Executes Alloy to generate pImmediateReachabilityRelation and pReachableImmediately
     */
    void loadImmediateReachabilityRelation();

    /**
     * Executes Alloy to generate pReachableFromConfig
     */
    void loadReachabilityRelation();

    virtual bool isReachableImmediately(unsigned fromConfigIndex, unsigned toConfigIndex) const;
    virtual bool isReachableFromConfig(unsigned fromConfigIndex, unsigned toConfigIndex) const;

    /**
     * @param immediate if true it is generating the immediate relation
     *   otherwise it is generating the one step relation
     */
    std::string getAlloyOutputPath(bool immediate);

    /**
     * @param immediate if true it is generating the immediate relation
     *   otherwise it is generating the one step relation
     */
    std::string getAlloyCommand(bool immediate, std::string outputPath);

  public:
    static const char* NO_LATENCY;
    static const char* REACH_OPTIONS;
    static const char* REACH_PATH;
    static const char* REACH_PREFIX;
    static const char* REACH_MODEL;
    static const char* REACH_SCOPE;

    SDPAdaptationManager();

    /**
     * parameters must include:
     *   REACH_MODEL: pathToModelFile not including the ".als" extension
     *   REACH_PATH: pathToReachProgram
     *   REACH_SCOPE: string with scope definition
     *   NO_LATENCY: boolean, true if no tactic has latency. In that case,
     *   	R^T = R^I. Also, "-nl" will be appended to REACH_MODEL before loading it
     *   REACH_OPTIONS: options for reach.sh
     *   REACH_PREFIX: prefix to distinguish yaml files from concurrent processes
     */
    virtual void initialize(std::shared_ptr<const ConfigurationManager> configMgr, const YAML::Node& params);
    virtual TacticList evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
    		const UtilityFunction& utilityFunction, unsigned horizon);
    virtual bool supportsStrategy() const;
    virtual std::shared_ptr<Strategy> getStrategy();
    virtual ~SDPAdaptationManager();
};

}

#endif
