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

#ifndef DARTADAPTATIONMANAGER_H_
#define DARTADAPTATIONMANAGER_H_

#include <pladapt/AdaptationManager.h>
#include <pladapt/ConfigurationManager.h>
#include "Parameters.h"
#include "EnvironmentMonitor.h"
#include "DartUtilityFunction.h"
#include "DartConfiguration.h"
#include <vector>
#include <memory>

#ifdef PLADAPT_SUPPORTS_CE
#define DART_USE_CE 1
#else
#define DART_USE_CE 0
#endif

namespace dart {
namespace am2 {

using EnvironmentState = std::vector<bool>;


/**
 * Information that would be collected through monitoring.
 *
 * The simulation of the environment sensing in this case is left inside the adaptation
 * manager, that is why truth is provided. With real sensors, the environment information
 * would be the one sensed.
 */
struct DartMonitoringInfo {
	Coordinate position;
	double altitude; /**< current altitude of the team */

	// TODO these two could be replaced by a vector of Coordinate showing the planned route
	int directionX; /**< -1, 0 or +1 to indicate the horizontal direction of travel */
	int directionY; /**< -1, 0 or +1 to indicate the vertical direction of travel */

	unsigned formation;	/**< 0:loose, 1:tight. If the formation is changing, this is the formation it is changing FROM */
	double ttcIncAlt; /**< time to complete altitude increase, 0 means not executing */
	double ttcDecAlt; /**< time to complete altitude decrease, 0 means not executing */
	double ttcIncAlt2; /**< time to complete altitude increase 2, 0 means not executing */
	double ttcDecAlt2; /**< time to complete altitude decrease 2, 0 means not executing */
	bool ecm; /**< if ECM is on */
	double ttcFormationChange;  /**< time to complete altitude change, 0 means no ongoing change */

	/**
	 * Results of threat sensing
	 *
	 * Ideally, this will have sensed threat observations for the next H cells
	 * in the route, where H is the look-ahead horizon
	 */
	SensorResults threatSensing;

	/**
	 * Results of target sensing
	 *
	 * Ideally, this will have sensed target observations for the next H cells
	 * in the route, where H is the look-ahead horizon
	 */
	SensorResults targetSensing;
};


///**
// * The cell number of the first cell in the following
// * threats and targets vectors. This cell should correspond
// * to the cell the team is about to enter.
// *
// * Cells in the path of the team are numbered starting at 0.
// *
// * Note: this is needed to align environment observations between successive calls
// * to decideAdaptation().
// */
//unsigned environmentStartCell;
//
///**
// * True presence of threats in the H cells ahead, where H is the range of
// * the look-ahead sensor. If there are fewer than H cells in the remainder
// * of the route, it is OK to send fewer than H, but never less than one.
// */
//EnvironmentState threats;
//
///**
// * True presence of targets in the H cells ahead, where H is the range of
// * the look-ahead sensor. If there are fewer than H cells in the remainder
// * of the route, it is OK to send fewer than H, but never less than one.
// */
//EnvironmentState targets;


class DartAdaptationManager {
public:

	/**
	 * Must be called before any other method
	 */
	void initialize(const Params& params, std::unique_ptr<pladapt::UtilityFunction> utilityFunction);

	/**
	 * Make adaptation decisions based on the latest monitoring information
	 */
	pladapt::TacticList decideAdaptation(const DartMonitoringInfo& monitoringInfo);

    /**
     * Returns true if the evaluate() computes a full strategy
     */
    bool supportsStrategy() const;

    /**
     * Returns the strategy computed by the last call to evaluate()
     *
     * If evaluate() has not been called or if the method is not supported by
     * the adaptation manager, it returns a null ptr.
     */
    std::shared_ptr<pladapt::Strategy> getStrategy();

    virtual ~DartAdaptationManager();

protected:

	/**
	 * Instantiates and initializes the appropriate adaptation manager
	 */
	void instantiateAdaptationMgr(const Params& params);

	Params params;
	std::unique_ptr<pladapt::AdaptationManager> adaptMgr;

	std::shared_ptr<const pladapt::ConfigurationManager> configManager;
	std::unique_ptr<pladapt::UtilityFunction> pUtilityFunction;

	RealEnvironment threatEnv;
	RealEnvironment targetEnv;
	std::unique_ptr<EnvironmentMonitor> pEnvThreatMonitor;
	std::unique_ptr<EnvironmentMonitor> pEnvTargetMonitor;

public:
	/**
	 * TODO this should be in ConfigurationManager and be required in all derived classes
	 */
	DartConfiguration convertToDiscreteConfiguration(const DartMonitoringInfo& info) const;

	void printEnvironment();
};


} /* namespace am2 */
} /* namespace dart */

#endif /* DARTADAPTATIONMANAGER_H_ */
