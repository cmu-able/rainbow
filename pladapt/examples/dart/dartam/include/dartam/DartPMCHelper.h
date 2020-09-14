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

#ifndef DARTPMCHELPER_H_
#define DARTPMCHELPER_H_

#include <pladapt/PMCAdaptationManager.h>
#include "Parameters.h"


namespace dart {
namespace am2 {

class DartPMCHelper : public pladapt::PMCHelper {
public:
	DartPMCHelper(const Params& params);
	virtual ~DartPMCHelper();
    std::string generateInitializations(const pladapt::Configuration& currentConfigObj,
            const pladapt::UtilityFunction& utilityFunction, unsigned horizon) const override;

protected:
    double evaluationPeriod;
    double changeAltitudeLatency;
    int maxAltitudeLevel;
    double destructionFormationFactor;
    double threatRange;
    double detectionFormationFactor;
    double sensorRange;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* DARTPMCHELPER_H_ */
