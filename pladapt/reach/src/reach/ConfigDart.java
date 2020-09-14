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

public class ConfigDart implements Configuration {
	public int formation; // 0: loose, 1: tight
	public int tightPeriodsLeft; // number of tightPeriods remaining
	
	public ConfigDart() {
		formation = 0;
		tightPeriodsLeft = 0;
	}

	public ConfigDart(int formation, int tightPeriodsLeft) {
		this.formation = formation;
		this.tightPeriodsLeft = tightPeriodsLeft;
	}
	
	@Override
	public Configuration parse(String expression, A4Solution sol, Module mod)
			throws Err {
		int formation = Reachability.getIndex(expression + ".f", sol, mod);
		int tightPeriodsLeft = Reachability.getIndex(expression + ".tightPeriodsLeft", sol, mod);
		return new ConfigDart(formation, tightPeriodsLeft);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + formation;
		result = prime * result + tightPeriodsLeft;
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
		ConfigDart other = (ConfigDart) obj;
		if (formation != other.formation)
			return false;
		if (tightPeriodsLeft != other.tightPeriodsLeft)
			return false;
		return true;
	}

}
