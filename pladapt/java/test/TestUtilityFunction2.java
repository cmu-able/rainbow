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
import pladapt.GenericConfigurationManager;
import pladapt.GenericEnvironment;
import pladapt.UtilityFunction;
import pladapt.pladapt;
import pladapt.*;

public class TestUtilityFunction2 extends UtilityFunction {

  static {
    System.loadLibrary("pladapt_wrap");
  }


	public double getAdditiveUtility(Configuration config, Environment environment, int time) {
		GenericConfiguration _config = (GenericConfiguration) config;
		GenericEnvironment _environment = (GenericEnvironment) environment;
		System.out.println("getAdditiveUtility(" + _config + ", " + _environment + ")");
		System.out.println("config is of class " + _config.getClass().getName());
		System.out.println("environment is of class " + _environment.getClass().getName());
		return _config.getInt("s") * _environment.asDouble() + _config.getInt("d");
	}
/*
	public double getGenFinalReward(GenericConfiguration config, GenericEnvironment env, int time) {
		System.out.println("getGenFinalReward()");
		return 10;
	}
*/

	public double getMultiplicativeUtility(Configuration config, Environment environment, int time) {
		System.out.println("getMultiplicativeUtility() config in is of class " + config.getClass().getName());
		GenericConfiguration _config = (GenericConfiguration) config;
		System.out.println("getMultiplicativeUtility() config is of class " + _config.getClass().getName());
		return 1.0;
	}

	public static void main(String argv[]) {
		GenericConfiguration c1 = new GenericConfiguration();
		c1.setInt("s", 3);
		c1.setInt("d", 2);
		GenericEnvironment e1 = new GenericEnvironment("p1");
		e1.setDouble("p1", 5);

		TestUtilityFunction2 u = new TestUtilityFunction2();

		System.out.println("Direct test:");
		u.getAdditiveUtility(c1, e1, 0);
		
		System.out.println("Indirect test:");
		System.out.println(pladapt.testUtilityFunction(u, c1, e1));
		
		System.out.println("Indirect with ConfigurationManager test:");
		GenericConfigurationManager cm = new GenericConfigurationManager();
		GenericConfiguration c = cm.addNewConfiguration(); 
		c.setInt("s", 3);
		c.setInt("d", 2);
		System.out.println(pladapt.testUtilityFunctionWithConfigMgr(u, cm, e1));
	}
}

