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

public class ConfigDart2 implements Configuration {

	public int formation; // 0: loose, 1: tight
	public int altitudeLevel;
	public int incAltProgress;
	public int decAltProgress;
	public int incAlt2Progress;
	public int decAlt2Progress;
	public boolean ecm;

	
	public ConfigDart2() {
		formation = 0;
		altitudeLevel = 0;
		incAltProgress = 0;
		decAltProgress = 0;
		incAlt2Progress = 0;
		decAlt2Progress = 0;
		ecm = false;
	}

	public ConfigDart2(int formation, int altitudeLevel, int incAltProgress, int decAltProgress,
			int incAlt2Progress, int decAlt2Progress,
			boolean ecm) {
		this.formation = formation;
		this.altitudeLevel = altitudeLevel;
		this.incAltProgress = incAltProgress;
		this.decAltProgress = decAltProgress;
		this.incAlt2Progress = incAlt2Progress;
		this.decAlt2Progress = decAlt2Progress;
		this.ecm = ecm;
	}
	
	@Override
	public Configuration parse(String expression, A4Solution sol, Module mod) throws Err {
		int formation = Reachability.getIndex(expression + ".f", sol, mod);
		int altitudeLevel = Reachability.getIndex(expression + ".a", sol, mod);
		
        // the progress part does not exist in the model with zero-latency
		int incAltProgress = 0;
		int decAltProgress = 0;
        try {
        	incAltProgress = Reachability.getIndex(expression + ".p[IncAlt]", sol, mod);
        	decAltProgress = Reachability.getIndex(expression + ".p[DecAlt]", sol, mod);
        } catch (Exception e) {
        	// do nothing
        }
        
        // the Alt2 tactics not present in all the models
		int incAlt2Progress = 0;
		int decAlt2Progress = 0;
        try {
        	incAlt2Progress = Reachability.getIndex(expression + ".p[IncAlt2]", sol, mod);
        	decAlt2Progress = Reachability.getIndex(expression + ".p[DecAlt2]", sol, mod);
        } catch (Exception e) {
        	// do nothing
        }
        
        // the ecm attribute is not present in all the models
        boolean ecm = false;
        try {
        	ecm = Reachability.getBoolean(expression + ".ecm", sol, mod);
        } catch (Exception e) {
        	// do nothing
        }
		
		return new ConfigDart2(formation, altitudeLevel,
				incAltProgress, decAltProgress,
				incAlt2Progress, decAlt2Progress,
				ecm);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + altitudeLevel;
		result = prime * result + decAlt2Progress;
		result = prime * result + decAltProgress;
		result = prime * result + (ecm ? 1231 : 1237);
		result = prime * result + formation;
		result = prime * result + incAlt2Progress;
		result = prime * result + incAltProgress;
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
		ConfigDart2 other = (ConfigDart2) obj;
		if (altitudeLevel != other.altitudeLevel)
			return false;
		if (decAlt2Progress != other.decAlt2Progress)
			return false;
		if (decAltProgress != other.decAltProgress)
			return false;
		if (ecm != other.ecm)
			return false;
		if (formation != other.formation)
			return false;
		if (incAlt2Progress != other.incAlt2Progress)
			return false;
		if (incAltProgress != other.incAltProgress)
			return false;
		return true;
	}

}
