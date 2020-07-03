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

#ifndef ROUTE_H_
#define ROUTE_H_

#include <vector>
#include <iostream>

namespace dart {
namespace am2 {

using CoordT = int;

/**
 * Coordinate in 2D
 */
class Coordinate {
public:
	CoordT x;
	CoordT y;
	Coordinate(CoordT x = 0, CoordT y = 0) : x(x), y(y) {};
	bool operator==(const Coordinate& b) const {
		return x == b.x && y == b.y;
	}

	/**
	 * This defines a strict ordering
	 */
	bool operator<(const Coordinate& b) const {
		return x < b.x || (x == b.x && y < b.y);
	}

	/**
	 * @param b corner opposite from the origin, defining the rectangle
	 * @return true if coordinate is inside the rectangle defined by 0,0 and b
	 */
	bool isInsideRect(const Coordinate& b) const {
		return x < b.x && y < b.y;
	}

    void printOn(std::ostream& os) const;
    friend std::ostream& operator<<(std::ostream& os, const Coordinate& coord);
};


/**
 * A route is a vector of Coordinate
 *
 * The points in the route are not necessarily contiguous, so this class can be
 * used to represent a route with waypoints for example. A TBD constructor
 * could take a Route and create another Route that represents the original
 * route but with the necessary points added to make it contiguous.
 */
class Route : public std::vector<Coordinate> {
public:
	Route() {};
	Route(Coordinate origin, double directionX, double directionY, unsigned length);
	virtual ~Route();
};

} /* namespace am2 */
} /* namespace dart */

#endif /* ROUTE_H_ */
