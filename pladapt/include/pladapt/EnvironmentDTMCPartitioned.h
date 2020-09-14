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

#ifndef _PLADAPT_ENVIRONMENTDTMCPARTITIONED_H_
#define _PLADAPT_ENVIRONMENTDTMCPARTITIONED_H_

#include <pladapt/DTMCPartitionedStates.h>
#include <pladapt/Environment.h>
#include <vector>
#include <memory>

namespace pladapt {

class EnvironmentDTMCPartitioned : public DTMCPartitionedStates {
public:
	typedef std::vector<std::shared_ptr<Environment>> StateValues;
protected:
	StateValues stateValues;
	const std::shared_ptr<Environment>& getSharedStateValue(unsigned state);

public:
	EnvironmentDTMCPartitioned(unsigned numberOfStates);

	void setStateValue(unsigned state, std::shared_ptr<Environment> pValue);
	const Environment& getStateValue(unsigned state) const;

	virtual void printOn(std::ostream& os) const;
	virtual ~EnvironmentDTMCPartitioned();

	static EnvironmentDTMCPartitioned createJointDTMC(EnvironmentDTMCPartitioned& a, EnvironmentDTMCPartitioned& b);

    friend std::ostream& operator<<(std::ostream& os, const EnvironmentDTMCPartitioned& envDTMC);
};

}

#endif /* _PLADAPT_ENVIRONMENTDTMCPARTITIONED_H_ */
