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

#include <dartam/DartPMCHelper.h>
#include <dartam/DartConfiguration.h>
#include <sstream>

using namespace std;

namespace dart {
namespace am2 {


DartPMCHelper::DartPMCHelper(const Params& params)
	: evaluationPeriod(params.adaptationManager.adaptationPeriod),
	  changeAltitudeLatency(params.tactics.changeAltitudeLatency),
	  maxAltitudeLevel(params.configurationSpace.ALTITUDE_LEVELS - 1),
	  destructionFormationFactor(params.environmentModel.DESTRUCTION_FORMATION_FACTOR),
	  threatRange(params.environmentModel.THREAT_RANGE),
	  detectionFormationFactor(params.environmentModel.TARGET_DETECTION_FORMATION_FACTOR),
	  sensorRange(params.environmentModel.TARGET_SENSOR_RANGE)
{
}


DartPMCHelper::~DartPMCHelper() {
	// TODO Auto-generated destructor stub
}


std::string DartPMCHelper::generateInitializations(const pladapt::Configuration& currentConfigObj,
            const pladapt::UtilityFunction& utilityFunction, unsigned horizon) const {

    auto& config = dynamic_cast<const DartConfiguration&>(currentConfigObj);

	stringstream initialState;
	initialState << "const double PERIOD = " << evaluationPeriod << ';' << endl;
	initialState << "const int HORIZON = " << horizon << ';' << endl;
	initialState << "const double IncAlt_LATENCY = " << changeAltitudeLatency
			<< ';' << endl;
	initialState << "const double DecAlt_LATENCY = " << changeAltitudeLatency
			<< ';' << endl;
	initialState << "const int MAX_ALT_LEVEL = " << maxAltitudeLevel << ';'
			<< endl;
	initialState << "const double destructionFormationFactor = "
			<< destructionFormationFactor << ';' << endl;
	initialState << "const double threatRange = " << threatRange << ';' << endl;
	initialState << "const double detectionFormationFactor = "
			<< detectionFormationFactor << ';' << endl;
	initialState << "const double sensorRange = " << sensorRange << ';' << endl;
	initialState << "const init_a = " << config.getAltitudeLevel() << ';'
			<< endl;
	initialState << "const init_f = " << config.getFormation() << ';' << endl;
	initialState << "const int ini_IncAlt_state = " << config.getTtcIncAlt()
			<< ';' << endl;
	initialState << "const int ini_DecAlt_state = " << config.getTtcDecAlt()
			<< ';' << endl;

    return initialState.str();
}

} /* namespace am2 */
} /* namespace dart */
