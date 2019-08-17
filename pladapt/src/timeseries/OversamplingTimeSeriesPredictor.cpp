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
#include "pladapt/timeseries/OversamplingTimeSeriesPredictor.h"
#include <sstream>
#include <stdexcept>
#include <stdlib.h>

using namespace std;

namespace pladapt {
namespace timeseries {

OversamplingTimeSeriesPredictor::OversamplingTimeSeriesPredictor(
        const std::vector<std::string>& model, unsigned trainingLength,
        unsigned horizon)
    : TimeSeriesPredictor(trainingLength, horizon)
{
    if (model.size() >= 2) {
        factor = atoi(model[1].c_str());
        vector<string>::const_iterator start = model.begin();
        // skip the two arguments of this predictor
        start++;
        start++;
        pPredictor = getInstance(
                vector<string>(start, model.end()),
                trainingLength * factor, horizon * factor);
    } else {
        stringstream ss;
        ss << "LESTimeSeriesPredictor(): model not valid:";
        for (unsigned i = 0; i < model.size(); i++) {
            ss << ' ';
            ss << model[i];
        }
        throw runtime_error(ss.str());
    }
    cout << "OversamplingTimeSeriesPredictor trainLength=" << trainLength
            << " factor=" << factor << endl;
}

OversamplingTimeSeriesPredictor::OversamplingTimeSeriesPredictor(const OversamplingTimeSeriesPredictor& p)
    : TimeSeriesPredictor(p),
      factor(p.factor),
      pPredictor(pPredictor->clone())
{
}

void OversamplingTimeSeriesPredictor::observe(double v) {
    TimeSeriesPredictor::observe(v);
    pPredictor->observe(v);
}

/**
 * Makes the prediction getting factor times more predictions from the
 * underlying predictor, and then taking the average of every factor-tuple
 * of predictions
 */
void OversamplingTimeSeriesPredictor::predict(unsigned n, double* predictions,
        double* variances) const {
    unsigned newN = n * factor;
    double pred[newN];
    double* var = (variances) ? new double[newN] : (double*) 0;

    pPredictor->predict(newN, pred, var);

    for (unsigned i = 0; i < n; i++) {
        double sumPred = 0;
        double sumVar = 0;
        for (unsigned j = 0; j < factor; j++) {
            sumPred += pred[i * factor + j];
            if (variances) {
                sumVar += var[i * factor + j];
            }
        }
        predictions[i] = sumPred / factor;
        if (variances) {
            variances[i] = sumVar / factor;
        }
    }

    if (var) delete[] var;
}

TimeSeriesPredictor* OversamplingTimeSeriesPredictor::clone() const {
    return new OversamplingTimeSeriesPredictor(*this);
}

typedef pair<const ScenarioTree::Node*, double> NodeProbPair;
typedef vector<NodeProbPair> NodeList; // the second is the probability


/**
 * Computes the mean value of all the paths of length factor
 */
double meanValue(unsigned factor, const ScenarioTree::Node& node) {
    double mean = node.value / factor;
    if (factor > 1) {
        double childrenMeanValue = 0;
        for (ScenarioTree::Edges::const_iterator it = node.edges.begin();
                        it != node.edges.end(); ++it) {
            childrenMeanValue += meanValue(factor - 1, it->child) * it->probability;
        }
        mean += childrenMeanValue * (factor - 1) / factor;
    }
    return mean;
}


void appendList(NodeList& a, const NodeList& b) {
    for (NodeList::const_iterator node = b.begin();
            node != b.end(); ++node) {
        a.push_back(*node);
    }
}

NodeList collectChildren(unsigned factor, const NodeProbPair& node) {
    NodeList list;
    if (factor == 1) {
        list.push_back(node);
    } else {
        for (ScenarioTree::Edges::const_iterator edge = node.first->edges.begin(); edge != node.first->edges.end(); ++edge) {
            NodeList childrenList = collectChildren(factor - 1, NodeProbPair(&edge->child, node.second * edge->probability));
            appendList(list, childrenList);
        }
    }
    return list;
}




/**
 * compresses factor levels into a single node, and follow recursively
 *
 * @param factor how many levels of the original tree make one level of the new tree
 * @param topNodes all the nodes of the original tree that are at the top of the
 *  new node. The probabilities of all the topNodes will be normalized so that they add
 *  up to one. Therefore, they can be their unconditional probability even if these nodes
 *  are several levels deep into the tree.
 */
ScenarioTree::Node compress(unsigned factor, NodeList topNodes) {

    // compute node mean value and collect leaf nodes for the layer
    double value = 0;
    double totalNodeProb = 0;
    NodeList leaves;
    for (NodeList::const_iterator top = topNodes.begin(); top != topNodes.end(); ++top) {
        value += meanValue(factor, *top->first) * top->second;
        totalNodeProb += top->second;
        appendList(leaves, collectChildren(factor, *top));
    }
    value /= totalNodeProb;

    ScenarioTree::Node node(value);

    // determine branching at the bottom of layer
    unsigned leafBranchingFactor = 0;
    bool leafBranchingFactorSet = false;
    vector<double> branchProbabilities;
    for (NodeList::const_iterator leaf = leaves.begin(); leaf != leaves.end(); ++leaf) {
        if (!leafBranchingFactorSet) {
            leafBranchingFactor = leaf->first->edges.size();
            leafBranchingFactorSet = true;
            for (ScenarioTree::Edges::const_iterator edge = leaf->first->edges.begin(); edge != leaf->first->edges.end(); ++edge) {
                branchProbabilities.push_back(edge->probability);
            }
        } else if (leafBranchingFactor != leaf->first->edges.size()) {
            throw runtime_error("OversamplingTimeSeriesPredictor: branching factors of bottom nodes don't match");
        } else {
            unsigned branch = 0;
            for (ScenarioTree::Edges::const_iterator edge = leaf->first->edges.begin(); edge != leaf->first->edges.end(); ++edge) {
                if (branchProbabilities[branch++] != edge->probability) {
                    throw runtime_error("OversamplingTimeSeriesPredictor: branching probabilities of bottom nodes don't match");
                }
            }
        }
    }

    if (leafBranchingFactor > 0) {
//        // split leaves into branching factor. Assumes leaves are ordered (e.g., left to right)
//        // TODO, this can be optimized for when leafBranchFactor = 1
//        unsigned nodesPerBranch = leaves.size() / leafBranchingFactor;
//        NodeList branchList;
//        for (NodeList::const_iterator it = leaves.begin(); it != leaves.end(); ++it) {
//            branchList.push_back(*it);
//            if (branchList.size() == nodesPerBranch) {
//                // add child
//                node.edges.push_back(ScenarioTree::Edge(branchProbabilities[node.edges.size()],
//                        compress(factor, branchList)));
//
//                branchList.clear();
//            }
//        }

        // TODO: there seems there is overlap between this collection of top nodes for the next layer
        // and the collection of leaves for the current layer. Revise because it could be optimized
        // split leaves into branching factor. Assumes leaves are ordered (e.g., left to right)
        unsigned nodesPerBranch = leaves.size();
        NodeList branchList;
        for (NodeList::const_iterator leaf = leaves.begin(); leaf != leaves.end(); ++leaf) {
            for (ScenarioTree::Edges::const_iterator edge = leaf->first->edges.begin(); edge != leaf->first->edges.end(); ++edge) {
                branchList.push_back(NodeProbPair(&edge->child, leaf->second * edge->probability));
                if (branchList.size() == nodesPerBranch) {
                    // add child
                    node.edges.push_back(ScenarioTree::Edge(branchProbabilities[node.edges.size()],
                            compress(factor, branchList)));

                    branchList.clear();
                }
            }
        }
    }

    return node;
}

ScenarioTree* OversamplingTimeSeriesPredictor::createScenarioTree(double lowerBound,
        double upperBound, unsigned branchingDepth, unsigned depth) const {

    ScenarioTree* pLargeTree = pPredictor->createScenarioTree(lowerBound,
            upperBound, branchingDepth * factor, depth * factor);
    pLargeTree->updateDepths();

    ScenarioTree* pTree = new ScenarioTree;
    pTree->getRoot().value = pLargeTree->getRoot().value;

    for (ScenarioTree::Edges::const_iterator edge = pLargeTree->getRoot().edges.begin(); edge != pLargeTree->getRoot().edges.end(); ++edge) {
        NodeList nodeList;
        nodeList.push_back(NodeProbPair(&edge->child, 1.0));
        pTree->getRoot().edges.push_back(ScenarioTree::Edge(edge->probability, compress(factor, nodeList)));
    }

    delete pLargeTree;
    return pTree;
//    delete pTree;
//    return pLargeTree;
}

OversamplingTimeSeriesPredictor::~OversamplingTimeSeriesPredictor() {
    // TODO Auto-generated destructor stub
}

} // timeseries
} // pladapt
