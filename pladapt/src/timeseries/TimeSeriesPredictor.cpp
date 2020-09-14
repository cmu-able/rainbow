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
#include "pladapt/timeseries/TimeSeriesPredictor.h"
#include <boost/tokenizer.hpp>
#include <memory>
#include <math.h>
#include <float.h>

#include "pladapt/timeseries/LESTimeSeriesPredictor.h"
#include "pladapt/timeseries/OversamplingTimeSeriesPredictor.h"

using namespace std;

namespace pladapt {
namespace timeseries {


// from "Reexamining Discrete Approximation"
// also see http://repositories.lib.utexas.edu/handle/2152/24941
//const double P10_PROB = 0.25;
//const double P90_PROB = 0.25;

// for Extended Swanson-Megil
//const double P_QUANTILE = 0.3;
//const double QUANTILE = 1.281552;

// for Extended Pearson-Tukey
const double P_QUANTILE = 0.185;
const double QUANTILE = 1.644854;

const char* LES_PREDICTOR_KEYWORD = "LES";
const char* OVERSAMPLING_PREDICTOR_KEYWORD = "OS";

TimeSeriesPredictor* TimeSeriesPredictor::getInstance(const std::vector<std::string>& modelArgs, unsigned trainingLength, unsigned horizon) {
    TimeSeriesPredictor* pPredictor = 0;

    if (modelArgs[0] == LES_PREDICTOR_KEYWORD) {
        pPredictor = new LESTimeSeriesPredictor(modelArgs, trainingLength, horizon);
    } else if (modelArgs[0] == OVERSAMPLING_PREDICTOR_KEYWORD) {
        pPredictor = new OversamplingTimeSeriesPredictor(modelArgs, trainingLength, horizon);
    }
    return pPredictor;
}


TimeSeriesPredictor* TimeSeriesPredictor::getInstance(const std::string&  model, unsigned trainingLength, unsigned horizon) {
    typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
	boost::char_separator<char> sep(" ");
	tokenizer tok(model, sep);

	vector<string> modelArgs;
	for (tokenizer::const_iterator it = tok.begin(); it != tok.end(); ++it) {
		modelArgs.push_back(*it);
	}
	return getInstance(modelArgs, trainingLength, horizon);
}

TimeSeriesPredictor::TimeSeriesPredictor(unsigned trainingLength, unsigned horizon)
    : trainLength(trainingLength), lastObservation(0) {
}

TimeSeriesPredictor::TimeSeriesPredictor(const TimeSeriesPredictor& p)
    : trainLength(p.trainLength), lastObservation(p.lastObservation)
{
}


unsigned long TimeSeriesPredictor::getTrainingLength() const {
    return trainLength;
}

void TimeSeriesPredictor::observe(double v) {
    lastObservation = v;
}

//TODO isRootNode <=> node.depth == 0, so it is redundant now
void TimeSeriesPredictor::expandTreeBranch(double lowerBound, double upperBound, unsigned branchingDepth, unsigned depth,
        ScenarioTree::Node& node, bool isRootNode) const {
    if (depth == 0) {
        return;
    }

    unique_ptr<TimeSeriesPredictor> pPredictor(clone());

    if (!isRootNode) {

        /*
         * this is not done for the current state (root node) because the
         * predictor has already been fed it
         */
        pPredictor->observe(node.value);
    }

    double variance;
    double prediction;
    pPredictor->predict(1, &prediction, &variance);
    double sd = sqrt(variance);

    prediction = min(max(lowerBound, prediction), upperBound);

    if (branchingDepth > 0) {
        double p10 = min(max(lowerBound, prediction - QUANTILE * sd), upperBound);
        double p90 = min(max(lowerBound, prediction + QUANTILE * sd), upperBound);

        node.edges.push_back(ScenarioTree::Edge(P_QUANTILE, ScenarioTree::Node(p10)));
        node.edges.push_back(ScenarioTree::Edge(1 - 2 * P_QUANTILE,
                ScenarioTree::Node(prediction)));
        node.edges.push_back(ScenarioTree::Edge(P_QUANTILE, ScenarioTree::Node(p90)));
    } else {
        node.edges.push_back(ScenarioTree::Edge(1,
                ScenarioTree::Node(prediction)));
    }

    for (ScenarioTree::Edges::iterator it = node.edges.begin();
            it != node.edges.end(); it++) {
        expandTreeBranch(lowerBound, upperBound, (branchingDepth > 0) ? branchingDepth - 1 : 0,
                depth - 1, it->child);
    }
}

ScenarioTree* TimeSeriesPredictor::createScenarioTree(double lowerBound,
        double upperBound, unsigned branchingDepth, unsigned depth) const {
    depth = max(branchingDepth, depth);
    ScenarioTree* pTree = new ScenarioTree;
    pTree->getRoot().value = lastObservation;

    expandTreeBranch(lowerBound,
            upperBound, branchingDepth, depth, pTree->getRoot(), true);

    return pTree;
}


void TimeSeriesPredictor::predict(double lowerBound, double upperBound,
        unsigned n, double* predictions, double* variances) const {
    predict(n, predictions, variances);
    for (unsigned i = 0; i < n; i++) {
        predictions[i] = min(max(lowerBound, predictions[i]), upperBound);
    }
}

ScenarioTree* TimeSeriesPredictor::createScenarioTree(unsigned branchingDepth,
        unsigned depth) const {
    return createScenarioTree(-DBL_MAX, DBL_MAX, branchingDepth, depth);
}



TimeSeriesPredictor::~TimeSeriesPredictor() {
}

} // timeseries
} // pladapt
