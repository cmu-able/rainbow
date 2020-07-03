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
#ifndef LESTIMESERIESPREDICTOR_H_
#define LESTIMESERIESPREDICTOR_H_

#include "TimeSeriesPredictor.h"
#include <boost/circular_buffer.hpp>
#include <vector>

namespace pladapt {
namespace timeseries {

/**
 * This implements a Holt's linear exponential smoothing method with adaptive damping
 * If phi (the third parameter) is 1, it is just Holt's linear trend
 */
class LESTimeSeriesPredictor: public TimeSeriesPredictor {
    double alpha;
    double beta;

    /**
     * This is the autoregressive damping parameter https://www.otexts.org/fpp/7/4
     */
    double phi;
    unsigned horizon;
    typedef std::vector<double> Forecast;
    typedef boost::circular_buffer<Forecast> ForecastBuffer;
    ForecastBuffer forecastBuffer;
    unsigned observationCount;
    double* squaredErrorSums; // array
    unsigned squaredErrorCount;
    double lt; // level
    double bt; // trend
public:
    virtual void observe(double v);
    virtual void predict(unsigned n, double* predictions, double* variances = 0) const;
    virtual TimeSeriesPredictor* clone() const;

    LESTimeSeriesPredictor(const std::vector<std::string>& model, unsigned trainingLength, unsigned horizon);
    LESTimeSeriesPredictor(const LESTimeSeriesPredictor& p);
    virtual ~LESTimeSeriesPredictor();
};

} // timeseries
} // pladapt

#endif /* LESTIMESERIESPREDICTOR_H_ */
