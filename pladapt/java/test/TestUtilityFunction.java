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
import pladapt.GenericConfiguration;
import pladapt.GenericEnvironment;
import pladapt.GenericUtilityFunction;
import pladapt.pladapt;

class TestUtilityFunction extends GenericUtilityFunction {

  static {
    System.loadLibrary("pladapt_wrap");
  }

	public double getGenAdditiveUtility(GenericConfiguration config, GenericEnvironment env, int time) {
		System.out.println("getGenAdditiveUtility(" + config 
				   + ", " + env + ")");		
		return config.getInt("s") * env.asDouble() + config.getInt("d");
	}
/*
	public double getGenFinalReward(GenericConfiguration config, GenericEnvironment env, int time) {
		System.out.println("getGenFinalReward()");
		return 10;
	}
*/
	public static void main(String argv[]) {
		GenericConfiguration c1 = new GenericConfiguration();
		c1.setInt("s", 3);
		c1.setInt("d", 2);
		GenericEnvironment e1 = new GenericEnvironment("p1");
		e1.setDouble("p1", 5);

		TestUtilityFunction u = new TestUtilityFunction();
		System.out.println(pladapt.testGeneric(u, c1, e1)); 
	}
}

