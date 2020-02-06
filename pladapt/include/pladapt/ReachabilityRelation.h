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

#ifndef _PLADAPT_REACHABILITYRELATION_H_
#define _PLADAPT_REACHABILITYRELATION_H_

#include <pladapt/ConfigurationSpace.h>
#include <pladapt/ConfigurationManager.h>
#include <pladapt/AdaptationManager.h>

#include <vector>
#include <string>

namespace pladapt {

/**
 * Reachability relation annotated with tactics
 *
 * For each tuple in the relation, this also holds the list of tactics needed
 * to realize the tuple
 */
class ReachabilityRelation {
public:
	static const std::size_t INVALID = std::numeric_limits<std::size_t>::max();

    struct Reachable {
        std::size_t configIndex;
        TacticList tactics;
    };
    typedef std::vector<Reachable> ReachableList;

private:
    const ConfigurationSpace& configSpace;
    std::vector<ReachableList> relation;

public:
    ReachabilityRelation(const ConfigurationSpace& configSpace);
    const ReachableList& getReachableFrom(std::size_t configIndex) const;
    std::size_t getReachableFrom(std::size_t configIndex, const TacticList &tactics) const;
    std::size_t getNumberOfStates() const;

    /**
     * Returns the set of all valid tactic combinations
     */
    std::set<TacticList> tacticCombinations() const;
    
    void load(std::string path, const ConfigurationManager& configManager);
    void makeIdentity();
    void dump();

    virtual ~ReachabilityRelation();
};

}
#endif /* _PLADAPT_REACHABILITYRELATION_H_ */
