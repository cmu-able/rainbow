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
package reach;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class ConfigUuv implements Configuration {
	public int speed;
	public boolean SENSOR1;
	public boolean SENSOR2;
	public boolean SENSOR3;
	
	public ConfigUuv() {
		speed = 0;
		SENSOR1 = false;
		SENSOR2 = false;
		SENSOR2 = false;
	}

	public ConfigUuv(int speed, boolean SENSOR1, boolean SENSOR2, boolean SENSOR3) {
		this.speed = speed;
		this.SENSOR1 = SENSOR1;
		this.SENSOR2 = SENSOR2;
		this.SENSOR3 = SENSOR3;
	}
	
	@Override
	public Configuration parse(String expression, A4Solution sol, Module mod) throws Err {
        int speed = Reachability.getIndex(expression + ".speed", sol, mod);
        boolean SENSOR1 = Reachability.getBoolean(expression + ".SENSOR1", sol, mod);
        boolean SENSOR2 = Reachability.getBoolean(expression + ".SENSOR2", sol, mod);
        boolean SENSOR3 = Reachability.getBoolean(expression + ".SENSOR3", sol, mod);
		
		return new ConfigUuv(speed, SENSOR1, SENSOR2, SENSOR3);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (SENSOR1 ? 1231 : 1237);
		result = prime * result + (SENSOR2 ? 1231 : 1237);
		result = prime * result + (SENSOR3 ? 1231 : 1237);
		result = prime * result + speed;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigUuv other = (ConfigUuv) obj;
		if (SENSOR1 != other.SENSOR1)
			return false;
		if (SENSOR2 != other.SENSOR2)
			return false;
		if (SENSOR3 != other.SENSOR3)
			return false;
		if (speed != other.speed)
			return false;
		return true;
	}

}
