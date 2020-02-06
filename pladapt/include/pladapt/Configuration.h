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

#ifndef _PLADAPT_CONFIGURATION_H_
#define _PLADAPT_CONFIGURATION_H_

#include <ostream>

namespace pladapt {

/**
 * This Configuration class represents a configuration, including the state of
 * tactics with latency
 *
 * In this implementation, this is hard-coded, but in the future it will be
 * replaced with a class that is defined from configuration files. In the
 * complete implementation, tactics would be specified in a modified Stitch,
 * required properties of configuration would be extracted from them, and this
 * class would be generated (well, a dynamic version of it), or the class could
 * be a key-value store to represent attributes.
 */
class Configuration {
public:

	/**
	 * Enum for down-casting in Java
	 *
	 * To avoid using RTTI and still be able to down-cast correctly in Java
	 */
	enum ConfigurationClass { C_CONFIGURATION, C_GENERIC_CONFIGURATION };

	bool operator==(const Configuration& other) const;
    virtual void printOn(std::ostream& os) const = 0;
    friend std::ostream& operator<<(std::ostream& os, const Configuration& config);
	virtual ConfigurationClass getType() const;

    virtual ~Configuration();

protected:
    virtual bool equals(const Configuration& other) const = 0;
};

}

#endif /* _PLADAPT_CONFIGURATION_H_ */
