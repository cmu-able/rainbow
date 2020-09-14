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

#ifndef _PLADAPT_ADAPTATIONMANAGER_H_
#define _PLADAPT_ADAPTATIONMANAGER_H_

#include <pladapt/EnvironmentDTMCPartitioned.h>
#include <pladapt/UtilityFunction.h>
#include <string>
#include <set>
#include <list>
#include <memory>

namespace pladapt {

typedef std::set<std::string> TacticList; /**< a set of tactic labels */

/**
 *  A list of TacticList representing an adaptation strategy.
 *
 *  Each element of the list represents the set of tactics that must be started
 *  in each decision period over the decision horizon.
 */
typedef std::list<TacticList> Strategy;


/**
 * Adaptation manager abstract base class
 */
class AdaptationManager
{
  public:
    virtual TacticList evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
    		const UtilityFunction& utilityFunction, unsigned horizon) = 0;
    void setDebug(bool debug);

    /**
     * Returns true if the evaluate() computes a full strategy
     */
    virtual bool supportsStrategy() const;

    /**
     * Returns the strategy computed by the last call to evaluate()
     *
     * If evaluate() has not been called or if the method is not supported by
     * the adaptation manager, it returns a null ptr.
     */
    virtual std::shared_ptr<Strategy> getStrategy();

    virtual ~AdaptationManager();

  protected:
    bool debug = false; /**< print debug info if true */
};

}

#endif
