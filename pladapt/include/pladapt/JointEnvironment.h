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

#ifndef JOINTENVIRONMENT_H_
#define JOINTENVIRONMENT_H_

#include <pladapt/Environment.h>
#include <vector>
#include <memory>

namespace pladapt {

class JointEnvironment: public Environment {
public:
	JointEnvironment(const std::shared_ptr<Environment>& componentA, const std::shared_ptr<Environment>& componentB);
	virtual unsigned getNumberOfComponents() const;
	virtual const Environment& getComponent(unsigned c) const;
	virtual void printOn(std::ostream& os) const;
	virtual EnvironmentClass getType() const;
	virtual ~JointEnvironment();

protected:
	std::shared_ptr<Environment> getSharedComponent(unsigned c);

	std::vector<std::shared_ptr<Environment>> components;
};

} /* namespace pladapt */

#endif /* JOINTENVIRONMENT_H_ */
