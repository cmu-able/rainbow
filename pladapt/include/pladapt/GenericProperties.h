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

#ifndef PLADAPT_GENERICPROPERTIES_H_
#define PLADAPT_GENERICPROPERTIES_H_
#include <boost/spirit/home/support/detail/hold_any.hpp>
#include <map>
#include <string>

#define ANY_NS boost::spirit

namespace pladapt {

typedef ANY_NS::hold_any AnyT;


/**
 * This class implements a generic class with properties that can be get/set,
 * and instances compared.
 *
 * It is intended to be a base class for GenericEnvironment and GenericConfiguration.
 */
class GenericProperties {
protected:
	typedef std::map<std::string, AnyT> Properties;

public:
	GenericProperties();
	virtual ~GenericProperties();

	template<typename T> inline T get(const std::string& property) const;
	template<typename T> void set(const std::string& property, const T& value);

	void printOn(std::ostream& os) const;
	bool equals(const GenericProperties& other) const;

	Properties::const_iterator begin() const { return properties.begin(); }
	Properties::const_iterator end() const { return properties.end(); }
	Properties::const_iterator cbegin() const { return properties.cbegin(); }
	Properties::const_iterator cend() const { return properties.cend(); }

protected:
	Properties properties;
	std::size_t hash;

	void updateHash();
};

template<typename T> inline T GenericProperties::get(const std::string& property) const {
	const auto it = properties.find(property);
	if (it == properties.end()) {
		return ANY_NS::any_cast<T>(AnyT());
	}
	return ANY_NS::any_cast<T>(it->second);
}

template<typename T> void GenericProperties::set(const std::string& property, const T& value) {
	properties.insert(std::make_pair(property, AnyT(value)));
	updateHash();
}

} /* namespace pladapt */

#endif /* PLADAPT_GENERICPROPERTIES_H_ */
