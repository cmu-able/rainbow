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

#ifndef PLADAPT_GENERICCONFIGURATION_H_
#define PLADAPT_GENERICCONFIGURATION_H_

#include <pladapt/Configuration.h>
#include <pladapt/GenericProperties.h>

namespace pladapt {

class GenericConfiguration: public Configuration, public GenericProperties {
public:
	virtual ~GenericConfiguration();
	virtual void printOn(std::ostream& os) const;
	inline bool isEqual(const GenericConfiguration& other) {
		return GenericConfiguration::equals(other);
	}
	virtual ConfigurationClass getType() const;

protected:
	virtual bool equals(const Configuration& other) const;
};

} /* namespace pladapt */

#endif /* PLADAPT_GENERICCONFIGURATION_H_ */
