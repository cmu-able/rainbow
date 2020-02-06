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

#ifndef _PLADAPT_CONFIGURATIONSPACE_H_
#define _PLADAPT_CONFIGURATIONSPACE_H_

#include <pladapt/Configuration.h>
#include <vector>

namespace pladapt {

class ConfigurationSpace {
    typedef std::vector<Configuration*> Configurations;
    Configurations configurations;

public:

    /**
     * Insert new configuration in the space
     *
     * This object takes ownership of the object pointed to by pConfig
     * and will destroy it when the ConfigurationSpace object is destroyed.
     *
     * @param pConfig pointer to configuration to be inserted
     */
    void insert(Configuration* pConfig);

    /**
     * @return number of configurations
     */
    std::size_t size() const;

    const Configuration& getConfiguration(std::size_t index) const;

    /**
     * Finds the index for a configuration
     *
     * Since all the valid configuration must exist this method
     * throws an std::range_error exception if the configuration is not found
     *
     * @param config configuration for which to find the index
     * @return the index of the configuration
     */
    std::size_t getIndex(const Configuration& config) const;

    virtual ~ConfigurationSpace();
};

}
#endif /* _PLADAPT_CONFIGURATIONSPACE_H_ */
