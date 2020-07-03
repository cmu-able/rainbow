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

#ifndef SENSOR_H_
#define SENSOR_H_

#include <random>

namespace dart {
namespace am2 {

/**
 * Takes ground truth and senses with given false positive and false negative rates
 */
class Sensor {
public:
	Sensor(double falsePositiveRate, double falseNegativeRate);
	bool sense(bool truth);
	virtual ~Sensor();

protected:
	double fpr; /**< false positive rate */
	double fnr; /**< false negative rate */
	std::uniform_real_distribution<> uniform;
	std::default_random_engine randomGenerator;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* SENSOR_H_ */
