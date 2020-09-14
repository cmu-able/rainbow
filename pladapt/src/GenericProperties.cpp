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

#include <pladapt/GenericProperties.h>
#include <boost/functional/hash.hpp>
#include <stdexcept>

namespace boost {
namespace spirit {
	std::size_t hash_value(pladapt::AnyT const & val) {
		if (val.type() == typeid(int)) {
			return boost::hash_value(val.cast<int>());
		} else if (val.type() == typeid(bool)) {
			return boost::hash_value(val.cast<bool>());
		} else if (val.type() == typeid(double)) {
			return boost::hash_value(val.cast<double>());
		}
		throw std::runtime_error("GenericProperties not able to handle type in hash_value()");
	}
}
}

namespace pladapt {

GenericProperties::GenericProperties() {
	updateHash();
}

GenericProperties::~GenericProperties() {
	// TODO Auto-generated destructor stub
}


void GenericProperties::updateHash() {
	hash = boost::hash_value(properties);
}

void GenericProperties::printOn(std::ostream& os) const {
	bool first = true;
	os << '(';
	for (const auto& p : properties) {
		if (first) {
			first = false;
		} else {
			os << ';';
		}
		os << p.first;
		os << '=';
		os << p.second;
	}
	os << ')';
}

bool GenericProperties::equals(const GenericProperties& other) const {
	return hash == other.hash;
}

} /* namespace pladapt */
