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

#ifndef _PLADAPT_DTMCPARTITIONEDSTATES_H_
#define _PLADAPT_DTMCPARTITIONEDSTATES_H_

#include <set>
#include <boost/numeric/ublas/matrix_sparse.hpp>


namespace pladapt {

typedef boost::numeric::ublas::mapped_matrix<double> TransitionMatrix;

/**
 * This class represents a DTMC in which the set of states can be partitioned
 *
 * This is used to represent the model of the environment, for example, in which
 * there is a clear mapping from time to the possible states
 *
 * It does not enforce a strict partition (i.e., requiring that each state belongs
 * to exactly one part) but it could.
 *
 * The states are unsigned integers which are not intended to represent state
 * values, but rather be state indexes. That is, the actual state values are
 * to be stored somewhere else, with the DTMC just providing an index.
 */
class DTMCPartitionedStates {
public:
    typedef std::set<unsigned> Part;
    typedef std::vector<Part> Partition;

protected:
    TransitionMatrix tm;
    Partition partition;
public:
    /**
     * Creates a single dimension DTMC with no transitions
     */
    DTMCPartitionedStates(unsigned numberOfStates);

    virtual std::size_t getNumberOfStates() const;
    virtual const TransitionMatrix& getTransitionMatrix() const;
    virtual TransitionMatrix& getTransitionMatrix();
    virtual void setTransitionProbability(unsigned from, unsigned to, double probability);

    /**
     * Assigns a state to a partition
     *
     * It does not check if the state is assigned to another partition
     */
    virtual void assignToPart(unsigned partIndex, unsigned state);

    virtual unsigned getNumberOfParts() const;
    virtual const Part& getPart(unsigned index) const;

    /**
     * Tells is there is no state in the last part that is reachable from
     * some state in the last part.
     */
    virtual bool isLastPartFinal() const;

    virtual ~DTMCPartitionedStates();
};

}

#endif /* _PLADAPT_DTMCPARTITIONEDSTATES_H_ */
