//
 // Copyright (c) 2015 Carnegie Mellon University. All Rights Reserved.

 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions
 // are met:

 // 1. Redistributions of source code must retain the above copyright
 // notice, this list of conditions and the following acknowledgments
 // and disclaimers.

 // 2. Redistributions in binary form must reproduce the above
 // copyright notice, this list of conditions and the following
 // disclaimer in the documentation and/or other materials provided
 // with the distribution.

 // 3. The names "Carnegie Mellon University," "SEI" and/or "Software
 // Engineering Institute" shall not be used to endorse or promote
 // products derived from this software without prior written
 // permission. For written permission, please contact
 // permission@sei.cmu.edu.

 // 4. Products derived from this software may not be called "SEI" nor
 // may "SEI" appear in their names without prior written permission of
 // permission@sei.cmu.edu.

 // 5. Redistributions of any form whatsoever must retain the following
 // acknowledgment:

 // This material is based upon work funded and supported by the
 // Department of Defense under Contract No. FA8721-05-C-0003 with
 // Carnegie Mellon University for the operation of the Software
 // Engineering Institute, a federally funded research and development
 // center.

 // Any opinions, findings and conclusions or recommendations expressed
 // in this material are those of the author(s) and do not necessarily
 // reflect the views of the United States Department of Defense.

 // NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE
 // ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS"
 // BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND,
 // EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT
 // LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY,
 // EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE
 // MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH
 // RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT
 // INFRINGEMENT.

 // This material has been approved for public release and unlimited
 // distribution.

 // DM-0002494
 //
/*
 * EnvPredictionRecord.cpp
 *
 *  Created on: Oct 2, 2015
 *      Author: ashutosp
 */

#include "pladapt/timeseries/EnvPredictionRecord.h"
#include <assert.h>
#include <fstream>
#include <string>
//#include <DebugFileInfo.h>

namespace pladapt {
namespace timeseries {

EnvPredictionRecord* EnvPredictionRecord::mEnvPredictionRecord = NULL;

EnvPredictionRecord::EnvPredictionRecord(unsigned horizon,
        double maxMatchWidth) :
        mHorizon(horizon), mMaxMatchWidthFactor(maxMatchWidth) {

    /*unsigned index = 0;

    while (index < mHorizon) {
        mArrivalRates.push_back(NULL);
        ++index;
    }*/
}

EnvPredictionRecord::~EnvPredictionRecord() {
    if (mArrivalRates.size() > 0) {
        ArrivalRates::iterator itr = mArrivalRates.begin();

        while (itr != mArrivalRates.end()) {
            if (*itr != NULL) {
                delete *itr;
                *itr = NULL;
            }
            ++itr;
        }
    }
}

void EnvPredictionRecord::resetArrivalRates() {
    ArrivalRates::iterator itr = mArrivalRates.begin();

    while (itr != mArrivalRates.end()) {
        if (*itr != NULL) (*itr)->clear();
        ++itr;
    }
}

void EnvPredictionRecord::updateArrivalPredictions(
        const ScenarioTree::Node* pNode, unsigned stateValue) {
    //std::cout << "state = " << stateValue << " value = " << 1 / pNode->value
    //        << " Depth = " << pNode->depth << std::endl;

    HorizonArrivalRates* horizonArrivalRate = NULL;

    if (mArrivalRates.size() < pNode->depth + 1) {
        horizonArrivalRate = new HorizonArrivalRates();
        mArrivalRates.push_back(horizonArrivalRate);//[pNode->depth] = ;
    } else {
        horizonArrivalRate = mArrivalRates[pNode->depth];
    }

    horizonArrivalRate->push_back(
            StateArrivalRatePair(stateValue, 1 / pNode->value));
}

double getModulus(double value) {
    return value < 0 ? (value*-1) : value;
}

unsigned EnvPredictionRecord::getClosestArrivalRateIndex(
        double interArrivalRate, unsigned horizon) const {
    std::cout << "got to EnvPredictionRecord getClosestArrivalRateIndex\n";
    unsigned stateIndex = UINT_MAX;

    double maxMatchWidth = std::min(interArrivalRate * mMaxMatchWidthFactor, 100.0);
    std::cout << "cp 1\n";    
    std::cout << "mArrivalRates.size() is " << mArrivalRates.size() << "\n";
    //if ((mArrivalRates.size() >= horizon + 1)) {
        HorizonArrivalRates* horizonArrivalRate = mArrivalRates[horizon];
        double matched = 0.0;
        std::cout << "cp 2\n";

        if (horizonArrivalRate != NULL) {
            std::cout << "cp 3\n";
            HorizonArrivalRates::iterator itr = horizonArrivalRate->begin();
            double diff = UINT_MAX;

            while (itr != horizonArrivalRate->end()) {
                //std::cout << "dbEntry = " << itr->second << " input Entry = " << interArrivalRate << endl;
                //std::cout << interArrivalRate - itr->second
                //        << " Absolute Value = "
                //        << getModulus(interArrivalRate - itr->second) << std::endl;
                if (diff > getModulus(interArrivalRate - itr->second)) {
                    stateIndex = itr->first;
                    diff = getModulus(interArrivalRate - itr->second);
                    matched = itr->second;
                }

                ++itr;
            }

            if (diff > maxMatchWidth) {
                stateIndex = UINT_MAX;
                //std::string file_name = DebugFileInfo::getInstance()->GetDebugFilePath();
                std::string file_name = "/home/frank/Dropbox/regression/HP_triggers_arrival_rate";
                std::ofstream myfile;
                myfile.open(file_name, std::ios::app);
                myfile << "Input Entry = " << interArrivalRate
                        << "  matched = " << matched << "   diff = " << diff << std::endl;
                myfile.close();
                std::cout << "Input Entry = " << interArrivalRate
                        << "  matched = " << matched << "   diff = " << diff << std::endl;
            }
        }
    //}

    return stateIndex;
}

}
}
