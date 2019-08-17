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

#include <dartam/DartConfigurationManager.h>
#include <dartam/DartConfiguration.h>

#include <memory>
#include <vector>

using namespace std;

namespace dart {
namespace am2 {

DartConfigurationManager::DartConfigurationManager(unsigned altitudeLevels, unsigned changeAltitudeLatencyPeriods,
		bool hasEcm, bool hasAlt2Tactics)
	: altitudeLevels(altitudeLevels), changeAltitudeLatencyPeriods(changeAltitudeLatencyPeriods)
{
	const vector<DartConfiguration::Formation> Formations = { DartConfiguration::Formation::LOOSE, DartConfiguration::Formation::TIGHT };

	unsigned maxEcm = (hasEcm) ? 1 : 0;
	alt2LatencyPeriods = (hasAlt2Tactics) ? changeAltitudeLatencyPeriods : 0;

	for (unsigned ecm = 0; ecm <= maxEcm; ecm++) {
		for (unsigned alt = 0; alt < altitudeLevels; alt++) {
			for (auto form : Formations) {
				for (unsigned ttcIncAlt = 0; ttcIncAlt <= changeAltitudeLatencyPeriods; ++ttcIncAlt) {
					for (unsigned ttcDecAlt = 0; ttcDecAlt <= changeAltitudeLatencyPeriods; ++ttcDecAlt) {
						for (unsigned ttcIncAlt2 = 0; ttcIncAlt2 <= alt2LatencyPeriods; ++ttcIncAlt2) {
							for (unsigned ttcDecAlt2 = 0; ttcDecAlt2 <= alt2LatencyPeriods; ++ttcDecAlt2) {
								configSpace.insert(new DartConfiguration(alt, form, ttcIncAlt, ttcDecAlt,
										ttcIncAlt2, ttcDecAlt2, (ecm > 0)));
							}
						}
					}
				}
			}
		}
	}
}

DartConfigurationManager::~DartConfigurationManager() {
}

const pladapt::ConfigurationSpace& DartConfigurationManager::getConfigurationSpace() const {
	return configSpace;
}

std::unique_ptr<pladapt::Configuration> DartConfigurationManager::getConfigurationFromYaml(
		const YAML::Node& configDetails) const {
    unsigned altitude = configDetails["altitudeLevel"].as<int>();
    DartConfiguration::Formation formation = static_cast<DartConfiguration::Formation>(configDetails["formation"].as<int>());
    unsigned ttcIncAlt = changeAltitudeLatencyPeriods - configDetails["incAltProgress"].as<int>();
    unsigned ttcDecAlt = changeAltitudeLatencyPeriods - configDetails["decAltProgress"].as<int>();
    unsigned ttcIncAlt2 = 0;
    if (configDetails["incAlt2Progress"].IsDefined()) {
    	ttcIncAlt2 = alt2LatencyPeriods - configDetails["incAlt2Progress"].as<int>();
    }
    unsigned ttcDecAlt2 = 0;
    if (configDetails["decAlt2Progress"].IsDefined()) {
    	ttcDecAlt2 = alt2LatencyPeriods - configDetails["decAlt2Progress"].as<int>();
    }
    bool ecm = false;
    if (configDetails["ecm"].IsDefined()) {
    	ecm = configDetails["ecm"].as<bool>();
    }
	return std::unique_ptr<pladapt::Configuration>(
			new DartConfiguration(altitude, formation, ttcIncAlt, ttcDecAlt,
					ttcIncAlt2, ttcDecAlt2, ecm));
}

} /* namespace am2 */
} /* namespace dart */

unsigned dart::am2::DartConfigurationManager::getChangeAltitudeLatencyPeriods() const {
	return changeAltitudeLatencyPeriods;
}
