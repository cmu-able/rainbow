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

public class Config implements Configuration {
	// public properties to simplify yaml serialization
	public int s;
	public boolean f;
	public int addServerProgress;
	public int removeServerProgress;
	
	public Config(int s, boolean f, int addServerProgress, int removeServerProgress) {
		this.s = s;
		this.f = f;
		this.addServerProgress = addServerProgress;
		this.removeServerProgress = removeServerProgress;
	}
	
	@Override
	public Config parse(String expression, A4Solution sol, Module mod) throws Err {
        int servers = Reachability.getIndex(expression + ".s", sol, mod);
        boolean fidelity = Reachability.getBoolean(expression + ".f", sol, mod);
        int addServerProgress = Reachability.getIndex(expression + ".p[AddServer]", sol, mod);
        int removeServerProgress = Reachability.getIndex(expression + ".p[RemoveServer]", sol, mod);
        return new Config(servers, fidelity, addServerProgress, removeServerProgress);
	}

	@Override
	public String toString() {
		return "Config [s=" + s + ", f=" + f + ", addServerProgress="
				+ addServerProgress + ", removeServerProgress="
				+ removeServerProgress + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + addServerProgress;
		result = prime * result + (f ? 1231 : 1237);
		result = prime * result + removeServerProgress;
		result = prime * result + s;
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
		Config other = (Config) obj;
		if (addServerProgress != other.addServerProgress)
			return false;
		if (f != other.f)
			return false;
		if (removeServerProgress != other.removeServerProgress)
			return false;
		if (s != other.s)
			return false;
		return true;
	}
	
}
