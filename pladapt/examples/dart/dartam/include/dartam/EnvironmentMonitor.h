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

#ifndef ENVIRONMENTMONITOR_H_
#define ENVIRONMENTMONITOR_H_

#include "RealEnvironment.h"
#include "Sensor.h"
#include <map>
#include <memory>
#include <boost/math/distributions/beta.hpp>

namespace dart {
namespace am2 {


struct SensorResult {
	Coordinate cellPosition; /**< cell that was sensed */
	unsigned observations; /**< number of observations taken */
	unsigned detections; /**< number of observations that resulted in a detection */
};

using SensorResults = std::vector<SensorResult>;


/**
 * Monitors the environment by getting observations through a sensor
 */
class EnvironmentMonitor {
public:
	EnvironmentMonitor(std::unique_ptr<Sensor> sensor);
	virtual ~EnvironmentMonitor();

	/**
	 * Takes a number of observations for each location in the route
	 *
	 * @param realEnvironment the real environment to sense
	 * @param numOfObservations number of observations to take in each location
	 * @param route route to sense
	 */
	void sense(const RealEnvironment& realEnvironment, unsigned numOfObservations, const Route& route);

	/**
	 * Updates the monitored model of the invironment by adding new observations
	 *
	 * The new observations are added to previous observations at the
	 * corresponding locations.
	 *
	 * @param sensorResults new observations
	 */
	void update(const SensorResults& sensorResults);


	/**
	 * Return the sensor results for a given route
	 */
	SensorResults getResults(const Route& route) const;

	/**
	 * Remove all previous observations
	 */
	void clear();

	/**
	 * Returns the probability distribution of having an object at the location
	 *
	 * @param location
	 * @return distribution
	 */
	boost::math::beta_distribution<> getBetaDistribution(const Coordinate& location) const;

protected:
	std::unique_ptr<Sensor> pSensor;

	typedef std::map<Coordinate, int> MapCount;
	MapCount observations; /**< number of observations taken in each location */
	MapCount detections; /**< number of times an object was detected in each location */
};

} /* namespace am2 */
} /* namespace dart */

#endif /* ENVIRONMENTMONITOR_H_ */
