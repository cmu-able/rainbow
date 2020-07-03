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
#ifndef TIMESERIESPREDICTOR_H_
#define TIMESERIESPREDICTOR_H_

#include <string>
#include <vector>
#include "ScenarioTree.h"

namespace pladapt {
namespace timeseries {

class TimeSeriesPredictor {
protected:
    unsigned long trainLength;
    double lastObservation;

    TimeSeriesPredictor(unsigned trainingLength, unsigned horizon);
    TimeSeriesPredictor(const TimeSeriesPredictor& p);

    virtual void expandTreeBranch(double lowerBound, double upperBound, unsigned branchingDepth, unsigned depth, ScenarioTree::Node& node,
            bool isRootNode = false) const;

public:
    static TimeSeriesPredictor* getInstance(const std::vector<std::string>& modelArgs, unsigned trainingLength, unsigned horizon);
	static TimeSeriesPredictor* getInstance(const std::string& model, unsigned trainingLength, unsigned horizon);
	virtual unsigned long getTrainingLength() const;

	/**
	 * Process a new observation
	 *
	 * Derived classes overriding this method must invoke this one to keep track
	 * of the last observation, or make sure lastObservation == v upon return
	 */
	virtual void observe(double v);
	virtual void predict(unsigned n, double* predictions, double* variances = 0) const = 0;
	virtual TimeSeriesPredictor* clone() const = 0;

	/**
	 * Creates a scenario tree
	 *
	 * @param branchingDepth up to which depth it has to branch. For example, if it is
	 *   1, it means that it branches once (from the root node to the nodes of depth 1
	 * @param depth the depth of the tree, a tree with only a root node is considered
	 *   to have depth 0
	 */
	virtual ScenarioTree* createScenarioTree(unsigned branchingDepth, unsigned depth = 0) const;

	// bounded versions
	virtual void predict(double lowerBound, double upperBound, unsigned n,
			double* predictions, double* variances = 0) const;
	virtual ScenarioTree* createScenarioTree(double lowerBound,
			double upperBound, unsigned branchingDepth,
			unsigned depth = 0) const;

	virtual ~TimeSeriesPredictor();
};

} // timeseries
} // pladapt

#endif /* TIMESERIESPREDICTOR_H_ */
