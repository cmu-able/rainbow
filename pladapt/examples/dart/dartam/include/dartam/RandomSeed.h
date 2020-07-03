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

#ifndef RANDOMSEED_H_
#define RANDOMSEED_H_

#include <random>
#include <memory>

namespace dart {
namespace am2 {

/**
 * This singleton is used to obtain seeds to initialized the different
 * random number generators.
 *
 * The seeds obtained with getNextSeed() are random, but that can be
 * itself seeded with seed().
 */
class RandomSeed {
public:

	/**
	 * Get the next (random) seed
	 */
	static int getNextSeed();

	/**
	 * Seed the generator used to generate random seeds.
	 */
	static void seed(int seed);

protected:
	std::uniform_int_distribution<> uniform;
	std::default_random_engine randomGenerator;

	// to make it singleton
	RandomSeed();

	static inline RandomSeed& getInstance();
	int getSeed();

	static std::unique_ptr<RandomSeed> instance;
};

}
}


#endif /* RANDOMSEED_H_ */
