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

#ifndef _PLADAPT_UTILITYFUNCTION_H_
#define _PLADAPT_UTILITYFUNCTION_H_

#include <pladapt/Configuration.h>
#include <pladapt/Environment.h>

namespace pladapt {

class UtilityFunction {
public:

	/**
	 * The parameter time is used here because in some cases the utility depends on something
	 * that is in turn a function of the decision stage (e.g., how far has a UAV moved since
	 * the start of the horizon). Instead of having to encode this in the configuration or the
	 * environment, we have it as a separate parameter.
	 *
	 * @param time time (in steps) since the beginning of the horizon
	 */
	virtual double getAdditiveUtility(const Configuration& config, const Environment& environment, int time) const;

    virtual double getMultiplicativeUtility(const Configuration& config, const Environment& environment, int time) const;

    /**
     * This is added to the additive utility only in the final stage (the end of the horizon)
     */
    virtual double getFinalReward(const Configuration& config, const Environment& environment, int time) const;

    /**
     * Returns the reward of adapting from one configuration to another
     *
     * Typically this will return a cost (i.e., a negative reward)
     * This, for example could return the cost of the tactics needed to trigger
     * the transition between these two configurations
     *
     * This is only used by PLA-SDP
     */
    virtual double getAdaptationReward(const Configuration& from, const Configuration& to, int time) const;

	virtual ~UtilityFunction();
};

}

#endif /* _PLADAPT_UTILITYFUNCTION_H_ */
