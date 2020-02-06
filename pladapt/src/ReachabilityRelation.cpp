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
#include <pladapt/ReachabilityRelation.h>
#include <iostream>
#include <yaml-cpp/yaml.h>
#include <algorithm>

using namespace std;

namespace pladapt {

ReachabilityRelation::ReachabilityRelation(
        const ConfigurationSpace& configSpace) : configSpace(configSpace) {
}


void printNodeInfo(const string& label, const YAML::Node& node) {
    cout << "node " << label << ": ";
    if (node.IsDefined()) cout << " IsDefined";
    if (node.IsMap()) cout << " IsMap";
    if (node.IsSequence()) cout << " IsSequence";
    cout << endl;
}

std::set<TacticList> ReachabilityRelation::tacticCombinations() const
{
	std::set<TacticList> res;

	for (const auto &i : relation)
		for (const auto &j : i)
			res.insert(j.tactics);

	return res;
}
 
void ReachabilityRelation::load(std::string path, const ConfigurationManager& configManager) {
    vector<YAML::Node> nodes = YAML::LoadAllFromFile(path);
    YAML::Node configs = nodes[0]["configs"];
    int idMap[configs.size()];
    for (unsigned id = 0; id < configs.size(); id++) {
        idMap[id] = configSpace.getIndex(*configManager.getConfigurationFromYaml(configs[id]));
    }

    relation.clear();
    relation.resize(configSpace.size());
    YAML::Node relationNodes = nodes[1];
    for (YAML::const_iterator from = relationNodes.begin(); from != relationNodes.end(); ++from) {
        ReachableList& reachable = relation[idMap[from->first.as<int>()]];
        for (YAML::const_iterator to = from->second.begin(); to != from->second.end(); ++to) {
            Reachable target;
            target.configIndex = idMap[to->first.as<int>()];
            for (YAML::const_iterator tactic = to->second.begin(); tactic != to->second.end(); ++tactic) {
                target.tactics.insert(tactic->as<string>());
            }
            reachable.push_back(target);
        }
    }
}

void ReachabilityRelation::makeIdentity() {
    relation.clear();
    relation.resize(configSpace.size());
    for (unsigned i = 0; i < configSpace.size(); ++i) {
    	Reachable target;
    	target.configIndex = i;
    	relation[i].push_back(target);
    }
}

const ReachabilityRelation::ReachableList& ReachabilityRelation::getReachableFrom(std::size_t configIndex) const {
    return relation[configIndex];
}

std::size_t ReachabilityRelation::getReachableFrom(std::size_t configIndex, const TacticList &tactics) const
{
	for (const auto &r : getReachableFrom(configIndex)) {
		if (r.tactics == tactics)
			return r.configIndex;
	}

	// no reachable configurations were found.
	return INVALID;
}

void ReachabilityRelation::dump() {
    for (unsigned i = 0; i < configSpace.size(); i++) {
        cout << "from: " << configSpace.getConfiguration(i) << endl;
        cout << "to:" << endl;
        ReachableList reachable = getReachableFrom(i);
        std::sort(reachable.begin(), reachable.end(), [](Reachable a, Reachable b) { return a.configIndex < b.configIndex; }); // sort so that ouput is the same regardless of how it was generated
        for (ReachableList::const_iterator it = reachable.begin(); it != reachable.end(); ++it) {
            cout << '\t' << configSpace.getConfiguration(it->configIndex) << ':';
            for (TacticList::const_iterator tactic = it->tactics.begin(); tactic != it->tactics.end(); ++tactic) {
                cout << ' ' << *tactic;
            }
            cout << endl;
        }
    }
}

std::size_t ReachabilityRelation::getNumberOfStates() const {
    return configSpace.size();
}

ReachabilityRelation::~ReachabilityRelation() {
    // TODO Auto-generated destructor stub
}

} // namespace
