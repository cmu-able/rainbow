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

import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Reach {
	private static void usageError() {
		// -d is a hack for now (DART) 
		System.out.println("Usage: reach [-d] [-c configClassName] [-i] alloyModelPath outputFile {sig[#]=size}+");
		System.out.println("use sig#=size for scope of latency signatures");
		System.exit(1);
	}

	public static void main(String[] args) {
		int argBase = 0;
		boolean immediate = false;
		String configClassName = "ConfigRubis"; // default
		
		while (argBase < args.length && args[argBase].startsWith("-")) {
			if (args[argBase].equals("-d")) {
				configClassName = "ConfigDart";
				argBase++;
			}

			if (args[argBase].equals("-i")) {
				immediate = true;
				argBase++;
			}			

			if (args[argBase].startsWith("-c")) {
				configClassName = args[argBase].substring(2);
				argBase++;
				if (configClassName.isEmpty() && argBase < args.length) {
					configClassName = args[argBase];
					argBase++;
				}
			}			
		}			

		// need at least 2 more
		if ((args.length - argBase) < 2) {
			usageError();
		}
			
		Reachability reach = null;
		if (immediate) {
			reach = new Reachability();
		} else {
			reach = new ReachabilityStep();
		}

		
		String alloyModelPath = args[argBase++]; 
		String outputPath = args[argBase++]; 
		
		// create a map with the sig=size pairs
		HashMap<String, Integer> scopeMap = new HashMap<>();
    	while (args.length > argBase) {
            StringTokenizer st = new StringTokenizer(args[argBase++], "=");
            if (!st.hasMoreTokens()) {
            	usageError();
            }
            String sig = st.nextToken().trim();
            if (!st.hasMoreTokens()) {
            	usageError();
            }
            Integer size = new Integer(st.nextToken().trim());
            if (st.hasMoreTokens()) {
            	usageError();
            }
            scopeMap.put(sig, size);
    	}

    	Configuration templateConfig = null;
    	try {
    		templateConfig = (Configuration) Class.forName("reach." + configClassName).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			System.exit(3);
		}
    	
    	// don't regenerate file if it exists
    	if (!(new File(outputPath)).exists()) {
			try {
				reach.run(alloyModelPath, templateConfig, outputPath, scopeMap);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
				System.exit(2);
			}
    	}
    	
    	// workaround for issue with java becoming zombie in docker
    	System.exit(0);
	}
}
