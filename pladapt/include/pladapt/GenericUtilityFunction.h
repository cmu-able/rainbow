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

#ifndef PLADAPT_GENERICUTILITYFUNCTION_H_
#define PLADAPT_GENERICUTILITYFUNCTION_H_

#include <pladapt/UtilityFunction.h>
#include <pladapt/GenericConfiguration.h>
#include <pladapt/GenericEnvironment.h>

namespace pladapt {

class GenericUtilityFunction: public UtilityFunction {
public:
	virtual ~GenericUtilityFunction();

	virtual double getAdditiveUtility(const Configuration& config, const Environment& env, int time) const;
    virtual double getMultiplicativeUtility(const Configuration& config, const Environment& env, int time) const;
    virtual double getFinalReward(const Configuration& config, const Environment& env, int time) const;
    virtual double getAdaptationReward(const Configuration& from, const Configuration& to, int time) const;

    /* these methods take GenericConfiguration and GenericEnvironment
     * to make it easier to use them from Java
     */
	virtual double getGenAdditiveUtility(const GenericConfiguration& config, const GenericEnvironment& environment, int time) const;
    virtual double getGenMultiplicativeUtility(const GenericConfiguration& config, const GenericEnvironment& environment, int time) const;
    virtual double getGenFinalReward(const GenericConfiguration& config, const GenericEnvironment& environment, int time) const;
    virtual double getGenAdaptationReward(const GenericConfiguration& from, const GenericConfiguration& to, int time) const;

};

} /* namespace pladapt */

#endif /* PLADAPT_GENERICUTILITYFUNCTION_H_ */
