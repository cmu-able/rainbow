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
import pladapt.TimeSeriesPredictor;
import pladapt.EnvironmentDTMCPartitioned;
import java.util.Random;

class TestTimeSeries {

  static {
    System.loadLibrary("pladapt_wrap");
  }

	public static void main(String argv[]) {
		TimeSeriesPredictor ts = TimeSeriesPredictor.getInstance("LES 0.8 0.15", 15, 5);
		
		Random rnd = new Random(3);
		for (int i = 0; i < 30; i++) {
			ts.observe(2 + i + 2 * rnd.nextDouble());
		}
		EnvironmentDTMCPartitioned envDTMC = ts.generateEnvironmentDTMC(2, 4);

		System.out.println(envDTMC); 
	}
}

