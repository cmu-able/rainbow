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

#ifndef PRISMWRAPPER_H_
#define PRISMWRAPPER_H_

#include <string>
#include <vector>

namespace pladapt {

class PRISMWrapper {
protected:
    std::string modelTemplatePath;
    std::vector<std::string> prismOptions;

    bool generateModel(std::string environmentModel, std::string initialState, const char* modelPath);
    bool runPrism(const char* modelPath, const char* adversaryPath, const char* statesPath,
    		const char* labelsPath, const char* pctl);

public:
	PRISMWrapper();

	/**
	 * Returns a vector of tactics that must be started now
	 * TODO the args should be a probability tree and a key-value map for the state
	 *
	 * @param path if not null, the temp directory path (relative) is stored there, and the
	 *   directory is not deleted
	 * @throws std::domain_error if there is no solution
	 */
	std::vector<std::string> plan(const std::string& environmentModel, const std::string& initialState,
			const std::string& pctl, std::string* path = 0);

	void setModelTemplatePath(const std::string& modelTemplatePath);
	void setPrismOptions(const std::vector<std::string>& options);

	virtual ~PRISMWrapper();
	void test();
};

}

#endif /* PRISMWRAPPER_H_ */
