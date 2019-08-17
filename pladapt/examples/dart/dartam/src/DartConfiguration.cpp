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

#include <dartam/DartConfiguration.h>
#include <typeinfo>

namespace dart {
namespace am2 {

DartConfiguration::DartConfiguration(unsigned altitudeLevel,
		Formation formation, unsigned ttcIncAlt, unsigned ttcDecAlt,
		unsigned ttcIncAlt2, unsigned ttcDecAlt2, bool ecm)
	: altitudeLevel(altitudeLevel), formation(formation), ttcIncAlt(ttcIncAlt), ttcDecAlt(ttcDecAlt),
	  ttcIncAlt2(ttcIncAlt2), ttcDecAlt2(ttcDecAlt2),
	  ecm(ecm)
{
}

DartConfiguration::~DartConfiguration() {
}

void DartConfiguration::printOn(std::ostream& os) const {
	os << "(alt=" << altitudeLevel << ", form=" << formation
			<< ", ttcIncAlt=" << ttcIncAlt
			<< ", ttcDecAlt=" << ttcDecAlt
			<< ", ttcIncAlt2=" << ttcIncAlt2
			<< ", ttcDecAlt2=" << ttcDecAlt2
			<< ", ecm=" << ecm
			<< ')';
}

unsigned DartConfiguration::getAltitudeLevel() const {
	return altitudeLevel;
}

DartConfiguration::Formation DartConfiguration::getFormation() const {
	return formation;
}

// Returns the number of periods until the adaptation tactic completes
unsigned DartConfiguration::getTtcDecAlt() const {
	return ttcDecAlt;
}

// Returns the number of periods until the adaptation tactic completes
unsigned DartConfiguration::getTtcIncAlt() const {
	return ttcIncAlt;
}

// Returns the number of periods until the adaptation tactic completes
unsigned DartConfiguration::getTtcDecAlt2() const {
	return ttcDecAlt2;
}

// Returns the number of periods until the adaptation tactic completes
unsigned DartConfiguration::getTtcIncAlt2() const {
	return ttcIncAlt2;
}

void DartConfiguration::setAltitudeLevel(unsigned altitudeLevel) {
	this->altitudeLevel = altitudeLevel;
}

void DartConfiguration::setFormation(Formation formation) {
	this->formation = formation;
}

void DartConfiguration::setTtcDecAlt(unsigned ttcDecAlt) {
	this->ttcDecAlt = ttcDecAlt;
}

void DartConfiguration::setTtcIncAlt(unsigned ttcIncAlt) {
	this->ttcIncAlt = ttcIncAlt;
}

void DartConfiguration::setTtcDecAlt2(unsigned ttcDecAlt2) {
	this->ttcDecAlt2 = ttcDecAlt2;
}

void DartConfiguration::setTtcIncAlt2(unsigned ttcIncAlt2) {
	this->ttcIncAlt2 = ttcIncAlt2;
}

bool DartConfiguration::getEcm() const {
	return ecm;
}

void DartConfiguration::setEcm(bool ecm) {
	this->ecm = ecm;
}

bool DartConfiguration::equals(const Configuration& other) const {
    try {
        const DartConfiguration& otherConf = dynamic_cast<const DartConfiguration&>(other);
        return altitudeLevel == otherConf.altitudeLevel && formation == otherConf.formation
        		&& ttcIncAlt == otherConf.ttcIncAlt && ttcDecAlt == otherConf.ttcDecAlt
        		&& ttcIncAlt2 == otherConf.ttcIncAlt2 && ttcDecAlt2 == otherConf.ttcDecAlt2
				&& ecm == otherConf.ecm;
    }
    catch(std::bad_cast&) {}
    return false;
}

} /* namespace am2 */
} /* namespace dart */
