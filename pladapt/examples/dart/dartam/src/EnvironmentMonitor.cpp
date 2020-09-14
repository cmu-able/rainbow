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

#include <dartam/EnvironmentMonitor.h>
#include <algorithm>

namespace dart {
namespace am2 {

using namespace boost::math;
using namespace std;

EnvironmentMonitor::EnvironmentMonitor(std::unique_ptr<Sensor> sensor)
	: pSensor(std::move(sensor))
{
}

EnvironmentMonitor::~EnvironmentMonitor() {
}

void EnvironmentMonitor::sense(const RealEnvironment& realEnvironment, unsigned numOfObservations, const Route& route) {
	for (const auto& pos : route) {
		if (pos.isInsideRect(realEnvironment.getSize())) {
			for (unsigned c = 0; c < numOfObservations; c++) {
				observations[pos]++;
				if (pSensor->sense(realEnvironment.isObjectAt(pos))) {
					detections[pos]++;
				}
			}

			/* set detections to 0 if we haven't increased it */
			if (detections.find(pos) == detections.end()) {
				detections[pos] = 0;
			}
		} else {

			/*
			 * if it's outside of the environment, just default to 2 observations,
			 * one true and one false, and don't accumulate them.
			 */
			detections[pos] = 1;
			observations[pos] = 2;
		}
	}
}

boost::math::beta_distribution<> EnvironmentMonitor::getBetaDistribution(const Coordinate& location) const {
	double alpha = 1e-300;
	double beta = 1.0;
	if (observations.find(location) != observations.end()) {
		auto dets = detections.at(location);
		if (dets > 0) {
			alpha = dets;
		}
		beta = max(double(observations.at(location) - dets), 1e-300);
	}
	return beta_distribution<>(alpha, beta);
}

void EnvironmentMonitor::update(const SensorResults& sensorResults) {
	for (const auto& sensorResult: sensorResults) {
		observations[sensorResult.cellPosition] += sensorResult.observations;
		detections[sensorResult.cellPosition] += sensorResult.detections;
	}
}

SensorResults dart::am2::EnvironmentMonitor::getResults(
		const Route& route) const {
	SensorResults results;
	for (const auto& cell : route) {
		SensorResult result;
		result.cellPosition = cell;
		const auto observationPos = observations.find(cell);
		if (observationPos != observations.end()) {
			result.observations = observationPos->second;
		}
		const auto detectionPos = detections.find(cell);
		if (detectionPos != detections.end()) {
			result.detections = detectionPos->second;
		}
		results.push_back(result);
	}
	return results;
}

void dart::am2::EnvironmentMonitor::clear() {
	observations.clear();
	detections.clear();
}

} /* namespace am2 */
} /* namespace dart */
