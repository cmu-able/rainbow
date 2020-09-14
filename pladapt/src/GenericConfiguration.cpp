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

#include <pladapt/GenericConfiguration.h>

namespace pladapt {


GenericConfiguration::~GenericConfiguration() {
	// TODO Auto-generated destructor stub
}

void GenericConfiguration::printOn(std::ostream& os) const {
	GenericProperties::printOn(os);
}

bool GenericConfiguration::equals(const Configuration& other) const {
    try {
        const GenericConfiguration& otherConf = dynamic_cast<const GenericConfiguration&>(other);
        return GenericProperties::equals(otherConf);
    }
    catch(std::bad_cast&) {}
    return false;
}

Configuration::ConfigurationClass GenericConfiguration::getType() const {
	return C_GENERIC_CONFIGURATION;
}

} /* namespace pladapt */
