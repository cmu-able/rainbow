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


#include <pladapt/GenericUtilityFunction.h>
#include <pladapt/GenericConfigurationManager.h>
#include <iostream>

using namespace std;

namespace pladapt {

double testGeneric(const GenericUtilityFunction& u, const GenericConfiguration& c, const GenericEnvironment& e) {
	cout << "testGeneric(u," << c << "," << e << ")" << endl;
	return u.getAdditiveUtility(c, e, 0) * u.getMultiplicativeUtility(c, e, 0) + u.getFinalReward(c, e, 0);
}

double testUtilityFunction(const UtilityFunction& u, const Configuration& c, const Environment& e) {
	cout << "testUtilityFunction(u," << c << "," << e << ")" << endl;
	return u.getAdditiveUtility(c, e, 0) * u.getMultiplicativeUtility(c, e, 0) + u.getFinalReward(c, e, 0);
}

double testUtilityFunctionWithConfigMgr(const UtilityFunction& u, const GenericConfigurationManager& cm, const Environment& e) {
	const Configuration& c = cm.getConfigurationSpace().getConfiguration(0);
	cout << "testUtilityFunctionWithConfigMgr(u," << c << "," << e << ")" << endl;
	return u.getAdditiveUtility(c, e, 0) * u.getMultiplicativeUtility(c, e, 0) + u.getFinalReward(c, e, 0);
}

}
