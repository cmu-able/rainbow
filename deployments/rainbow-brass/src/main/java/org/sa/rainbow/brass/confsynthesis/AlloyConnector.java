package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;

import edu.mit.csail.sdg.alloy4compiler.parser.*;
import edu.mit.csail.sdg.alloy4compiler.translator.*;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;



public class AlloyConnector {

	private HashMap<String, String> m_solutions = new HashMap<String, String>();
	public static final String SOLUTION_STRING="sol_";
	public static final int MAX_SOLUTIONS_DEFAULT = 500;
	public static int m_max_solutions = MAX_SOLUTIONS_DEFAULT;
		
	public void generateSolutions(String modelFile){
		int count=0;
		Module model=null;
		A4Solution ans=null;
		
		try{
			model = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null, modelFile);
		} catch (Err e){
			System.out.println("Error on static part of specification");//: "+modelFile);
			System.exit(-1);
		}
		
		Command command = model.getAllCommands().get(0);
		A4Options options = new A4Options();
        options.solver = A4Options.SatSolver.SAT4J;
	
        try{
        	 ans = TranslateAlloyToKodkod.execute_command(null, model.getAllReachableSigs(), command, options);	
        } catch (Err e){
			System.out.println("Error executing Alloy command: "+command.toString());
		}   
        
        while (ans.satisfiable() && count<m_max_solutions){
        	m_solutions.put(SOLUTION_STRING+String.valueOf(count), ans.toString());
            count++;
        	try{
            	ans=ans.next();
            } catch (Err e){
            }   
        }
		
	}
	
	public void setMaxSolutions(int m){
		m_max_solutions = m;
	}
	
	public int getMaxSolutions(){
		return m_max_solutions;
	}
	
	public HashMap<String, String> getSolutions(){
		return m_solutions;
	}
	
	public long getSolutionCount() {
		return m_solutions.size();
	}
	
	public String getSolution(String key){
		return m_solutions.get(key);
	}
	
}
