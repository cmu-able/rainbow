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
#ifndef OVERSAMPLINGTIMESERIESPREDICTOR_H_
#define OVERSAMPLINGTIMESERIESPREDICTOR_H_

#include "TimeSeriesPredictor.h"

namespace pladapt {
namespace timeseries {

/**
 * This is a wrapper for other time series predictors that takes observations
 * at a higher rate than the predictions it does. That is, the prediction period
 * is a multiple of the sampling period.
 */
class OversamplingTimeSeriesPredictor : public TimeSeriesPredictor {
    unsigned factor; // the factor between the sampling and the prediction periods
    TimeSeriesPredictor* pPredictor;
public:
    virtual void observe(double v);
    virtual void predict(unsigned n, double* predictions, double* variances = 0) const;
    virtual TimeSeriesPredictor* clone() const;

    virtual ScenarioTree* createScenarioTree(double lowerBound,
            double upperBound, unsigned branchingDepth,
            unsigned depth = 0) const;

    OversamplingTimeSeriesPredictor(const std::vector<std::string>& model, unsigned trainingLength, unsigned horizon);
    OversamplingTimeSeriesPredictor(const OversamplingTimeSeriesPredictor& p);
    virtual ~OversamplingTimeSeriesPredictor();
};

} // timeseries
} // pladapt

#endif /* OVERSAMPLINGTIMESERIESPREDICTOR_H_ */
