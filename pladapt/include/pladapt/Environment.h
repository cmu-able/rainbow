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

#ifndef _PLADAPT_ENVIRONMENT_H_
#define _PLADAPT_ENVIRONMENT_H_

#include <ostream>

namespace pladapt {

/**
 * Abstract class to represent the environment state
 */
class Environment {
public:

	/**
	 * Enum for down-casting in Java
	 *
	 * To avoid using RTTI and still be able to down-cast correctly in Java
	 */
	enum EnvironmentClass { C_ENVIRONMENT, C_JOINT_ENVIRONMENT, C_GENERIC_ENVIRONMENT };

	/**
	 * Number of components of the environment state
	 *
	 * This is trivial for all subclasses except for JointEnvironment
	 *
	 * @return number of components
	 */
	virtual unsigned getNumberOfComponents() const;

	/**
	 * Get a components of the environment state
	 *
	 * This is trivial for all subclasses except for JointEnvironment
	 *
	 * @return number of components
	 */
	virtual const Environment& getComponent(unsigned c) const;


	/**
	 * If possible return environment value as double.
	 *
	 * If that is not possible (e.g., env value is not scalar) it throws
	 * std::domain_error (this is the default behavior unless overridden)
	 *
	 * @param scalar environment value
	 */
	virtual double asDouble() const;

	virtual EnvironmentClass getType() const;

    virtual void printOn(std::ostream& os) const = 0;
    friend std::ostream& operator<<(std::ostream& os, const Environment& env);
    virtual ~Environment();
};

}

#endif /* _PLADAPT_ENVIRONMENT_H_ */
