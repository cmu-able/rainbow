package org.sa.rainbow.brass.confsynthesis;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.sa.rainbow.brass.confsynthesis.ConstantDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author jcamara
 * Iterates over the state space of possible point combinations
 * determined by a set of constant ranges (or fixed values) for an experiment
 */
public class SpaceIterator {

	private LinkedList<ConstantDefinition> m_constant_definitions = new LinkedList<ConstantDefinition>();
	private LinkedList<Double> m_current = new LinkedList<Double>();
	private LinkedList<LinkedList<String>> m_points = new LinkedList<LinkedList<String>>();
	

	public SpaceIterator(HashMap<String, ConstantDefinition> m){
		init (m);
	}
	
	public void init(HashMap<String, ConstantDefinition> m){
		int i=0;
		for (Map.Entry<String, ConstantDefinition> e: m.entrySet()){
			m_constant_definitions.add(e.getValue());
			LinkedList<String> curPoints = new LinkedList<String>();
			if (!Objects.equals(m_constant_definitions.get(i).getStep(),"0")){
				Double curStep = Double.parseDouble(m_constant_definitions.get(i).getStep());
				Double curMax = Double.parseDouble(m_constant_definitions.get(i).getMax());
				Double curMin = Double.parseDouble(m_constant_definitions.get(i).getMin());
				Double iter=curMin;
				while (iter<=curMax){
					String iterStr = iter.toString();
					if (iter % 1 == 0)
						iterStr = String.valueOf(iter.intValue());
					curPoints.add(iterStr);
					iter += curStep;
				}
			} else{
				Double curMin = Double.parseDouble(m_constant_definitions.get(i).getMin());
				String iterStr = curMin.toString();
				if (curMin % 1 == 0)
					iterStr = String.valueOf(curMin.intValue());
				curPoints.add(iterStr);
			}
			m_points.add(curPoints);
			i++;
		}
	}
		
	
	public List<String> getCombinations(){
		int sizeArray[] = new int[m_points.size()];

	    // keep track of the index of each inner String array which will be used
	    // to make the next combination
	    int counterArray[] = new int[m_points.size()];

	    // Discover the size of each inner array and populate sizeArray.
	    // Also calculate the total number of combinations possible using the
	    // inner String array sizes.
	    int totalCombinationCount = 1;
	    for(int i = 0; i < m_points.size(); ++i) {
	        sizeArray[i] = m_points.get(i).size();
	        totalCombinationCount *= m_points.get(i).size();
	    }
	    
	    // Store the combinations in a List of String objects
	    List<String> combinationList = new ArrayList<String>(totalCombinationCount);

	    StringBuilder sb;  // more efficient than String for concatenation

	    for (int countdown = totalCombinationCount; countdown > 0; --countdown) {
	        // Run through the inner arrays, grabbing the member from the index
	        // specified by the counterArray for each inner array, and build a
	        // combination string.
	        sb = new StringBuilder();
	        for(int i = 0; i < m_points.size(); ++i) {
	        	sb.append(m_constant_definitions.get(i).getId());
	        	sb.append("=");
	            sb.append( m_points.get(i).get(counterArray[i]));
	        	if (i<m_points.size()-1)
					sb.append(",");
	        }
	        combinationList.add(sb.toString());  // add new combination to list

	        // Now we need to increment the counterArray so that the next
	        // combination is taken on the next iteration of this loop.
	        for(int incIndex = m_points.size() - 1; incIndex >= 0; --incIndex) {
	            if(counterArray[incIndex] + 1 < sizeArray[incIndex]) {
	                ++counterArray[incIndex];
	                // None of the indices of higher significance need to be
	                // incremented, so jump out of this for loop at this point.
	                break;
	            }
	            // The index at this position is at its max value, so zero it
	            // and continue this loop to increment the index which is more
	            // significant than this one.
	            counterArray[incIndex] = 0;
	        }
	    }
	    return combinationList;
	}
	
	
	public LinkedList<LinkedList<String>> getPoints(){
		return m_points;
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}


}
