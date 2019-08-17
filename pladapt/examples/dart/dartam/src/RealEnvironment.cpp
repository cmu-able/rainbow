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

#include <dartam/RealEnvironment.h>
#include <dartam/RandomSeed.h>

namespace dart {
namespace am2 {

void RealEnvironment::populate(Coordinate size, unsigned numOfObjects) {
	this->size = size;
	envMap.clear();

	std::default_random_engine gen(RandomSeed::getNextSeed());
	std::uniform_int_distribution<> unifX(0, size.x - 1);
	std::uniform_int_distribution<> unifY(0, size.y - 1);

	while (numOfObjects > 0) {
		unsigned x = unifX(gen);
		unsigned y = unifY(gen);
		while (envMap[Coordinate(x,y)]) {
			x = unifX(gen);
			y = unifY(gen);
		}
		envMap[Coordinate(x,y)] = true;
		numOfObjects--;
	}
}

Coordinate RealEnvironment::getSize() const {
	return size;
}

bool RealEnvironment::isObjectAt(Coordinate location) const {
	bool isThere = false;
	const auto it = envMap.find(location);
	if (it != envMap.end()) {
		isThere = it->second;
	}

	return isThere;
}

void RealEnvironment::setAt(Coordinate location, bool objectPresent) {
	envMap[location] = objectPresent;
}


RealEnvironment::~RealEnvironment() {
}

} /* namespace am2 */
} /* namespace dart */
