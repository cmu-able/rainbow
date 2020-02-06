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

import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.esotericsoftware.yamlbeans.*;

public class Reachability {
	
	Map<Configuration, Integer> configMap;
    DynamicMatrix<List<String>> relation;
    Configuration configTemplate;
    boolean debug = false;
    int repeatedEntryCount = 0;
    
	
	public static int getIndex(String expression, A4Solution sol, Module mod) throws Err {
        Expr expr = CompUtil.parseOneExpression_fromString(mod, expression);
        String s = sol.eval(expr).toString();

		int bracePos = s.indexOf('}');
		if (bracePos == -1) {
			s = s.substring(s.lastIndexOf('$') + 1); // cut everything after the $ sign
		} else {
			s = s.substring(s.lastIndexOf('$') + 1, bracePos); // cut everything after the $ sign up to the }
		}
		return Integer.parseInt(s);
	}

	public static boolean getBoolean(String expression, A4Solution sol, Module mod) throws Err {
        Expr expr = CompUtil.parseOneExpression_fromString(mod, expression);
        String s = sol.eval(expr).toString();
        return s.indexOf("boolean/True") >= 0;
	}
	
	public static List<String> getTacticStrings(String expression, A4Solution sol, Module mod) throws Err {
		// format example: {Tactic1$0, Tactic2$0}
		List<String> list = new ArrayList<>();
        Expr expr = CompUtil.parseOneExpression_fromString(mod, expression);
        String s = sol.eval(expr).toString();
        
        StringTokenizer st = new StringTokenizer(s, "{,}");
        while (st.hasMoreTokens()) {
        	String token = st.nextToken().trim();
        	String tactic = token.substring(0, token.lastIndexOf('$'));
        	list.add(tactic);
            //System.out.println("got: " + tactic);
        }
        
        return list;
	}

	/**
	 * Loads configuration from solution and stores it in configMap
	 * @param expression
	 * @param sol
	 * @param mod
	 * @return config index
	 * @throws Err
	 */
	protected int loadConfig(String expression, A4Solution sol, Module mod) throws Err {
		Configuration config = configTemplate.parse(expression, sol, mod);
        int configIdx = 0;
        if (configMap.containsKey(config)) {
        	configIdx = configMap.get(config);
        } else {
        	configIdx = configMap.size();
        	configMap.put(config, configIdx);
        }
        return configIdx;
	}
	
	private Command changeScope(Module mod, Command cmd, String sigName, int scope)  throws Err {
		Command result = null;
		String fullSigName = new String("this/") + sigName;
		for (Sig sig: mod.getAllSigs()) {
			if (sig.label.equals(fullSigName)) {
				boolean isExact = cmd.getScope(sig).isExact;
				result = cmd.change(sig, isExact, scope);
			}
		}
		return result;
	}
		
	public void run(String filename, Configuration configTemplate,
			String outputFile, HashMap<String, Integer> scopeMap
			) throws Exception {
		this.configTemplate = configTemplate;
		
		A4Reporter rep = new A4Reporter();
		Module world = CompUtil.parseEverything_fromFile(rep, null, filename);
	    A4Options options = new A4Options();
	    options.solver = A4Options.SatSolver.MiniSatJNI;
	    // options.symmetry = 0; // optionally turn off symmetry breaking
	    
	    configMap = new HashMap<>();
	    relation = new DynamicMatrix<>();
	    
	    for (Command command: world.getAllCommands()) {
	    	if (!command.label.equals("show")) {
	    		continue;
	    	}
	    	
	    	// set scope
	    	for (String sig : scopeMap.keySet()) {
	    		int size = scopeMap.get(sig);
	    		if (sig.endsWith("#")) {
	    			sig = sig.substring(0, sig.length() - 1);
	    			size++; // the scope of a latency sig is one more than the number of latency periods
	    		}
	    		
	    		command = changeScope(world, command, sig, size);
	    	}
	    	
	    	System.out.println("****Command: " + command);
	        // Execute the command
	        executeAlloyAndGetSolution(rep, world, options, command);
	        if (debug) {
	        	System.out.println("There were " + repeatedEntryCount + " repeated entries.");
	        }
	        break;
	    }
	    
	    saveAsYaml(outputFile);
	}

	/**
	 * Executes Alloy, gets solution, and populates relation
	 * 
	 * @param rep
	 * @param world
	 * @param options
	 * @param command
	 * @throws Err
	 */
	protected void executeAlloyAndGetSolution(A4Reporter rep, Module world,
			A4Options options, Command command) throws Err {
		A4Solution sol = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
		while (sol.satisfiable()) {
		    int configFrom = loadConfig("trace/first.cp", sol, world);
		    int configTo = loadConfig("trace/last.cp", sol, world);
		    List<String> tactics = getTacticStrings("trace/last.starts", sol, world);
		    if (debug) {
		    	if (relation.get(configFrom, configTo) != null) {
		    		repeatedEntryCount++;
		    		System.out.println("Repeated from " + configFrom + " to " + configTo);
		    	}
//		    	if (configFrom == 69 && configTo == 110) {
//		    		sol.writeXML("sol" + repeatedEntryCount);
//		    	}
		    }
		    relation.set(configFrom, configTo, tactics);
		    sol = sol.next();
		}
	}
	
	protected void saveAsYaml(String filename) throws IOException, YamlException {
		YamlWriter writer = new YamlWriter(new FileWriter(filename));
		writer.getConfig().writeConfig.setWriteDefaultValues(true);
		
		// ouput list of configs ordered by their index
		Configuration[] configList = new Configuration[configMap.size()];
		//ArrayList<Config> configList = new ArrayList<>(configMap.size());
		for (Configuration config : configMap.keySet()) {
			configList[configMap.get(config)] = config;
		}
		Map<String, Configuration[]> c = new HashMap<>();
		c.put("configs", configList);
		writer.write(c);
		
		//Map<Integer, ArrayList<List<String>>> r = new HashMap<>();
		Map<Integer, Map<Integer, List<String>>> r = new HashMap<>();
		ArrayList<ArrayList<List<String>>> matrix = relation.matrix;
		for (int row = 0; row < matrix.size(); row++) {
			ArrayList<List<String>> columns = matrix.get(row);
			if (columns.size() > 0) {
				// add this one
				Map<Integer, List<String>> rowMap = new HashMap<>();
				for (int col = 0; col < columns.size(); col++) {
					List<String> entry = columns.get(col);
					if (entry != null) {
						rowMap.put(col, entry);
					}
				}
				r.put(row, rowMap);
			}
		}

		writer.write(r);
		writer.close();
	}


}
