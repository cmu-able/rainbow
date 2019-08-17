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

public class test {
  static {
    System.loadLibrary("pladapt_wrap");
  }

  public static void main(String argv[]) {
	GenericConfiguration c1 = new GenericConfiguration();
	c1.setInt("intv", 3);
	c1.setDouble("dv", 3.14);

	GenericConfiguration c2 = new GenericConfiguration();
	c2.setInt("intv", 3);
	c2.setDouble("dv", 3.14);
	//c2.setInt("x", 2);

	System.out.println(c1.isEqual(c2)); 

	GenericEnvironment e1 = new GenericEnvironment("p1");
	e1.setDouble("p1", 3);

	GenericEnvironment e2 = new GenericEnvironment("p1");
	e2.setDouble("p1", 3.1);

	System.out.println(e1.isEqual(e2)); 

	System.out.println(e2.asDouble());

//	GenericEnvironment e3 = new GenericEnvironment();
//	e3.setDouble("p1", 3.1);
//	System.out.println(e3.asDouble());
  }
}

