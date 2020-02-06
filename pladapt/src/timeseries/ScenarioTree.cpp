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
#include "pladapt/timeseries/ScenarioTree.h"
#include "pladapt/timeseries/EnvPredictionRecord.h"
#include <sstream>
#include <stack>
#include <iomanip>

using namespace std;

namespace pladapt {
namespace timeseries {

ScenarioTree::ScenarioTree() : root(0) {
}

ScenarioTree::Node& ScenarioTree::getRoot() {
	return root;
}

const ScenarioTree::Node& ScenarioTree::getRoot() const {
    return root;
}



ScenarioTree::~ScenarioTree() {
}

ScenarioTree* ScenarioTree::cloneWithNewRoot() {
    ScenarioTree* pTree = new ScenarioTree;
    pTree->getRoot().edges.push_back(Edge(1, root));

    updateDepths();
    return pTree;
}

int ScenarioTree::getNumberOfNodes() const {
    // TODO this traversal is repeated in several places...should refactor it as an iterator
    stack<ScenarioTree::NodeStateIndex> traversalStack;
    int stateIndex = 0;
    traversalStack.push(ScenarioTree::NodeStateIndex(&root, stateIndex++));
    while (!traversalStack.empty()) {
        const ScenarioTree::Node* pNode = traversalStack.top().pNode;
        traversalStack.pop();

        for (ScenarioTree::Edges::const_iterator it = pNode->edges.begin();
                it != pNode->edges.end(); it++) {
            traversalStack.push(ScenarioTree::NodeStateIndex(&(it->child), stateIndex++));
        }
    }
    return stateIndex;
}

void ScenarioTree::updateDepths() {
    stack<ScenarioTree::Node*> traversalStack;
    root.depth = 0;
    traversalStack.push(&root);
    while (!traversalStack.empty()) {
        ScenarioTree::Node* pNode = traversalStack.top();
        traversalStack.pop();

        for (ScenarioTree::Edges::iterator it = pNode->edges.begin();
                it != pNode->edges.end(); it++) {
            it->child.depth = pNode->depth + 1;
            traversalStack.push(&(it->child));
        }
    }
}

std::ostream& operator<<(std::ostream& os, const ScenarioTree& tree) {
	stack<ScenarioTree::NodeStateIndex> traversalStack;
	int stateIndex = 0;
	traversalStack.push(ScenarioTree::NodeStateIndex(&tree.root, stateIndex++));
	while (!traversalStack.empty()) {
		const ScenarioTree::Node* pNode = traversalStack.top().pNode;
		int nodeStateIndex = traversalStack.top().stateIndex;
		traversalStack.pop();

		string padding(" ");
		os << padding << '[' << nodeStateIndex << "] v=" << pNode->value << endl;
		for (ScenarioTree::Edges::const_iterator it = pNode->edges.begin();
				it != pNode->edges.end(); it++) {
			os << padding << "   " << "p=" << it->probability << "-> ["
					<< stateIndex << ']' << endl;
			traversalStack.push(ScenarioTree::NodeStateIndex(&(it->child), stateIndex++));
		}
	}

	return os;
}

void ScenarioTree::generateDiagram(std::ostream& os) const {
    const string STATE_VALUE_FORMULA = "formula stateValue = ";

    string result;
    stringstream edgesOutput;

    stack<ScenarioTree::NodeStateIndex> traversalStack;
    stringstream nodesOutput;

    int stateIndex = 0;
    string padding(STATE_VALUE_FORMULA.length(), ' ');

    traversalStack.push(ScenarioTree::NodeStateIndex(&getRoot(), stateIndex++));
    while (!traversalStack.empty()) {
        const ScenarioTree::Node* pNode = traversalStack.top().pNode;
        int nodeStateIndex = traversalStack.top().stateIndex;
        traversalStack.pop();

        // define node
        stringstream labelSS;
        labelSS << 's' << nodeStateIndex;
        string label = labelSS.str();

        nodesOutput << label
                << " [label=\"" << label
                << '=' << setprecision(3) << pNode->value
                << "\"]" << endl;

        for (ScenarioTree::Edges::const_iterator it = pNode->edges.begin();
                it != pNode->edges.end(); it++) {
            edgesOutput << label << " -> s" << stateIndex
                    << " [label=\"" << it->probability << "\"];" << endl;
            traversalStack.push(ScenarioTree::NodeStateIndex(&(it->child), stateIndex++));
        }
    }
    os << "digraph probability_tree {" << endl;
    os << nodesOutput.str() << edgesOutput.str();
    os << "}" << endl;
}

pladapt::EnvironmentDTMCPartitioned ScenarioTree::getEnvironmentDTMC(std::function<std::shared_ptr<pladapt::Environment>(double)> createEnvState) {
	updateDepths();
    unsigned numberOfStates = getNumberOfNodes();
    pladapt::EnvironmentDTMCPartitioned envDTMC(numberOfStates);

    pladapt::TransitionMatrix& tm = envDTMC.getTransitionMatrix();

    pladapt::timeseries::EnvPredictionRecord::get_instance()->resetArrivalRates();

    stack<ScenarioTree::NodeStateIndex> traversalStack;
    int stateIndex = 0;
    traversalStack.push(ScenarioTree::NodeStateIndex(&root, stateIndex++));
    while (!traversalStack.empty()) {
        const ScenarioTree::Node* pNode = traversalStack.top().pNode;
        int nodeStateIndex = traversalStack.top().stateIndex;
        traversalStack.pop();

        pladapt::timeseries::EnvPredictionRecord::get_instance()->updateArrivalPredictions(pNode,
                nodeStateIndex);

        envDTMC.setStateValue(nodeStateIndex, createEnvState(pNode->value));
        envDTMC.assignToPart(pNode->depth, nodeStateIndex);

        if (pNode->edges.empty()) {

            // it's leaf node, add a certain self-transition
            tm(nodeStateIndex, nodeStateIndex) = 1.0;
        } else {
            for (ScenarioTree::Edges::const_iterator it = pNode->edges.begin();
                    it != pNode->edges.end(); it++) {
                tm(nodeStateIndex, stateIndex) = it->probability;
                traversalStack.push(ScenarioTree::NodeStateIndex(&(it->child), stateIndex++));
            }
        }
    }

#if PLADAPT_SDP_NOPARTITION
    // put all env states in all the partitions
    for (unsigned p = 0; p < envDTMC.getNumberOfParts(); p++) {
        for (unsigned s = 0; s < numberOfStates; s++) {
            envDTMC.assignToPart(p, s);
        }
    }
#endif

    return envDTMC;
}



} // timeseries
} // pladapt
