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
#ifndef DARTCONFIGURATION_H_
#define DARTCONFIGURATION_H_

#include <pladapt/Configuration.h>

namespace dart {
namespace am2 {

class DartConfiguration: public pladapt::Configuration {
public:
	enum Formation { LOOSE, TIGHT };

	DartConfiguration(unsigned altitudeLevel, Formation formation,
			unsigned ttcIncAlt, unsigned ttcDecAlt,
			unsigned ttcIncAlt2, unsigned ttcDecAlt2, bool ecm = false);
	virtual ~DartConfiguration();

	virtual void printOn(std::ostream& os) const;
	unsigned getAltitudeLevel() const;
	Formation getFormation() const;
	unsigned getTtcDecAlt() const;
	unsigned getTtcIncAlt() const;
	unsigned getTtcDecAlt2() const;
	unsigned getTtcIncAlt2() const;
	void setAltitudeLevel(unsigned altitudeLevel);
	void setFormation(Formation formation);
	void setTtcDecAlt(unsigned ttcDecAlt);
	void setTtcIncAlt(unsigned ttcIncAlt);
	void setTtcDecAlt2(unsigned ttcDecAlt2);
	void setTtcIncAlt2(unsigned ttcIncAlt2);
	bool getEcm() const;
	void setEcm(bool ecm);

protected:
	unsigned altitudeLevel;
	Formation formation;
	unsigned ttcIncAlt; // time to complete tactic
	unsigned ttcDecAlt;
	unsigned ttcIncAlt2;
	unsigned ttcDecAlt2;
	bool ecm; // electronic countermeasures

	virtual bool equals(const Configuration& other) const;
};

} /* namespace am2 */
} /* namespace dart */

#endif /* DARTCONFIGURATION_H_ */
