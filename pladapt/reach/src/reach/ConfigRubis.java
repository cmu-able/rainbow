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

public class ConfigRubis implements Configuration {
	public int s;
	public int d;
	public int addServerProgress;
	public boolean coldCache;
	
	public ConfigRubis() {
		s = 1;
		d = 1;
		addServerProgress = 1;
		coldCache = false;
	}
	
	public ConfigRubis(int s, int d, int addServerProgress, boolean coldCache) {
		this.s = s;
		this.d = d;
		this.addServerProgress = addServerProgress;
		this.coldCache = coldCache;
	}

	@Override
	public Configuration parse(String expression, A4Solution sol, Module mod)
			throws Err {
        int servers = Reachability.getIndex(expression + ".s", sol, mod);
        int dimmer = Reachability.getIndex(expression + ".d", sol, mod);
        
        // the progress part does not exist in the model with zero-latency
        int addServerProgress = 0;
        try {
        	addServerProgress = Reachability.getIndex(expression + ".p[AddServer]", sol, mod);
        } catch (Exception e) {
        	// do nothing
        }
        
        // the coldCache attribute is not present in all the models
        boolean coldCache = false;
        try {
        	coldCache = Reachability.getBoolean(expression + ".coldCache", sol, mod);
        } catch (Exception e) {
        	// do nothing
        }
        
        return new ConfigRubis(servers, dimmer, addServerProgress, coldCache);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + addServerProgress;
		result = prime * result + (coldCache ? 1231 : 1237);
		result = prime * result + d;
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
		ConfigRubis other = (ConfigRubis) obj;
		if (addServerProgress != other.addServerProgress)
			return false;
		if (coldCache != other.coldCache)
			return false;
		if (d != other.d)
			return false;
		if (s != other.s)
			return false;
		return true;
	}

}
