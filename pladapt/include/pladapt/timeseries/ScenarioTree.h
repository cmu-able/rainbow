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
#ifndef SCENARIOTREE_H_
#define SCENARIOTREE_H_

#include <vector>
#include <string>
#include <iostream>
#include <pladapt/EnvironmentDTMCPartitioned.h>
#include <functional>


namespace pladapt {
namespace timeseries {

struct NodeData {
	std::vector<double> util;
	std::vector<int> next;
};

class ScenarioTree {

public:
	struct Edge;
	typedef std::vector<Edge> Edges;


	struct Node {
		Node(double value) : value(value), depth(0) {}
		Node(const Node& n) : value(n.value), edges(n.edges), data(n.data), depth(n.depth) {}
		double value;
		Edges edges;
		NodeData data;
		unsigned depth;
	};

	struct Edge {
		double probability;
		Node child;

		Edge(double probability, const Node& child)
			: probability(probability), child(child) {};
		Edge(const Edge& e) : probability(e.probability), child(e.child) {}
	};

	/**
	 * This struct is used to enumerate nodes (e.g., to map them to states)
	 */
    struct NodeStateIndex {
        NodeStateIndex(const Node* pNode, int stateIndex)
            : pNode(pNode), stateIndex(stateIndex) {};
        const Node* pNode;
        int stateIndex;
    };
protected:
	Node root;


public:
	ScenarioTree();
	Node& getRoot();
    const Node& getRoot() const;

	int getNumberOfNodes() const;

	/**
	 * The depth attribute of the nodes is not assigned until this is called
	 */
	void updateDepths();

	/**
	 * Creates a clone with a new root, making the (copy of) current root the only child of the new root
	 */
	ScenarioTree* cloneWithNewRoot();

	void generateDiagram(std::ostream& os) const;

	virtual ~ScenarioTree();

	friend std::ostream& operator<<(std::ostream& os, const ScenarioTree& tree);

	/**
	 * Constructs a DTMC for the environment using the scenario tree
	 *
	 * Example:
	 * getEnvironmentDTMC(
                    [](double mean) {return std::make_shared<Environment>(mean, pow(mean, 2));})
	 *
	 * @param createEnvState function that creates the environment value based on the prediction
	 */
	pladapt::EnvironmentDTMCPartitioned getEnvironmentDTMC(std::function<std::shared_ptr<pladapt::Environment>(double)> createEnvState);
};

} // timeseries
} // pladapt

#endif /* SCENARIOTREE_H_ */
