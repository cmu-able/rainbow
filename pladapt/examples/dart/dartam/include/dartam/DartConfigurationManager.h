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

#ifndef DARTCONFIGURATIONMANAGER_H_
#define DARTCONFIGURATIONMANAGER_H_

#include <pladapt/ConfigurationManager.h>

namespace dart {
namespace am2 {

class DartConfigurationManager: public pladapt::ConfigurationManager {
public:
	DartConfigurationManager(unsigned altitudeLevels,
			unsigned changeAltitudeLatencyPeriods, bool hasEcm,
			bool hasAlt2Tactics);
	virtual ~DartConfigurationManager();

    virtual const pladapt::ConfigurationSpace& getConfigurationSpace() const;

    virtual std::unique_ptr<pladapt::Configuration> getConfigurationFromYaml(const YAML::Node& configDetails) const;
	unsigned getChangeAltitudeLatencyPeriods() const;

protected:
    unsigned altitudeLevels;
    unsigned changeAltitudeLatencyPeriods;
    unsigned alt2LatencyPeriods;
    pladapt::ConfigurationSpace configSpace;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* DARTCONFIGURATIONMANAGER_H_ */
