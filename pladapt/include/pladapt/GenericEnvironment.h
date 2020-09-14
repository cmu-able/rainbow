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

#ifndef PLADAPT_GENERICENVIRONMENT_H_
#define PLADAPT_GENERICENVIRONMENT_H_

#include <pladapt/Environment.h>
#include <pladapt/GenericProperties.h>

namespace pladapt {

class GenericEnvironment: public Environment, public GenericProperties {
public:
	GenericEnvironment();

	/**
	 * Designates doubleProperty as the property returned by asDouble()
	 */
	GenericEnvironment(const std::string& doubleProperty);

	virtual ~GenericEnvironment();
	virtual void printOn(std::ostream& os) const;
    virtual double asDouble() const;

    virtual EnvironmentClass getType() const;

	inline bool isEqual(const GenericEnvironment& other) {
		return GenericEnvironment::equals(other);
	}

protected:
    const std::string doubleProperty;

	virtual bool equals(const Environment& other) const;
};

} /* namespace pladapt */

#endif /* PLADAPT_GENERICENVIRONMENT_H_ */
