package org.sa.rainbow.brass.model.instructions;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.models.ModelReference;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraphProgress {

    public static InstructionGraphProgress parseFromString (ModelReference ref, String igStr) {
        // Remove all the returns from string
        List<Instruction> instructions = parseFromString (igStr);
        InstructionGraphProgress ig = new InstructionGraphProgress (ref);
        ig.setInstructions (instructions);
        return ig;
    }

    public static List<Instruction> parseFromString (String igStr) {
        List<Instruction> instructions = new LinkedList<Instruction> ();
        igStr = igStr.replace ("\n", "").replace ("\r", "");
        igStr = igStr.substring (2); // Remove P(
        String[] is = igStr.split ("V");
        Pattern instructionPattern = Pattern.compile ("\\((.*), do (.*) then (.*).*");
        for (String i : is) {
            Matcher m = instructionPattern.matcher (i);
            if (m.matches ()) {
                Instruction inst = new Instruction ();
                inst.m_label = m.group (1);
                inst.m_instruction = m.group (2);
                inst.m_next = m.group (3);
                instructions.add (inst);
            }
        }
        return instructions;
    }

    public Collection<? extends Instruction> getInstructions () {
        return m_instructionList;
    }

    public Instruction getInstruction (String instLabel) {
		return m_instructions.get(instLabel). copy ();
	}

	public String getExecutingInstruction () {
        return m_currentNode;
    }

    public boolean getCurrentOK () {
        return m_currentOK;
    }
    
    public Instruction getCurrentInstruction () {
    	return m_instructions.get (m_currentNode);
    }
    
    public static class Instruction {
        public String m_label;
        public String m_instruction;
        public String m_next;
        
        private double m_targetX;
        private double m_targetY;
        private double m_targetW;
        
        public Instruction copy () {
            Instruction i = new Instruction ();
            i.m_instruction = new String (m_instruction);
            i.m_label = new String (m_label);
            i.m_next = new String (m_next);
            return i;
        }
        
        /**
         * This method is called every time this instruction is set in setInstructions () in
         * {@link InstructionGraphProgress}.
         */
        public void parseMoveAbsTargetPose () {
        	Pattern moveAbsPattern = Pattern.compile ("MoveAbs\\((.+), (.+), (.+)\\)");
        	Matcher m = moveAbsPattern.matcher (m_instruction);
        	if (m.matches ()) {
        		m_targetX = Double.parseDouble (m.group (1));
        		m_targetY = Double.parseDouble (m.group (2));
        		m_targetW = Double.parseDouble (m.group (3));
            }
        }
        
        public double getTargetX () {
        	return m_targetX;
        }
        
        public double getTargetY () {
        	return m_targetY;
        }
        
        public double getTargetHeading () {
        	return m_targetW;
        }
    }

    public static class ExecutionObservation {
        String  label;
        boolean successful;
        long    startTime;
        long    endTime;
    }

    private Map<String, Instruction>    m_instructions     = new HashMap<> ();
    private LinkedList<Instruction>     m_instructionList;
    private final ModelReference        m_model;
    private String                      m_currentNode;
    private boolean                     m_currentOK        = true;
    private Deque<ExecutionObservation> m_executionHistory = new ArrayDeque<> ();

    public InstructionGraphProgress (ModelReference model) {
        m_model = model;
    }

    public ModelReference getModelReference () {
        return m_model;
    }

    public InstructionGraphProgress copy () {
        InstructionGraphProgress ig = new InstructionGraphProgress (m_model);
        ig.m_instructions = new HashMap<> ();
        ig.m_executionHistory = new ArrayDeque<> (m_executionHistory);

        for (Map.Entry<String, Instruction> i : m_instructions.entrySet ()) {
            ig.m_instructions.put (i.getKey (), i.getValue ().copy ());
        }

        return ig;
    }

    public void setInstructions (List<Instruction> instructions) {
        Instruction prev = null;
        m_instructionList = new LinkedList<Instruction> (instructions);
        m_instructions.clear ();
        for (Instruction i : instructions) {
        	// Parse the target pose from MoveAbs(x, y, w)
            i.parseMoveAbsTargetPose ();
            
            if (prev != null) {
                prev.m_next = i.m_label;
            }
            prev = i;
            m_instructions.put (i.m_label, i);
        }
    }

    public void setExecutingInstruction (String instLabel) {
    	if (m_instructions.containsKey (instLabel)) {
            m_currentNode = instLabel;
        }
//        ExecutionObservation observation = new ExecutionObservation ();
//        observation.startTime = new Date().getTime ();
//        observation.label = instLabel;
//        observation.
//        m_executionHistory.push (observation);

    }

    public void setCurrentOK (boolean ok) {
        m_currentOK = ok;
    }

    // Note: this assumes sequential instructions
    @Override
    public String toString () {
        StringBuffer b = new StringBuffer ();
        b.append ("P(");
        if (!m_instructionList.isEmpty ()) {
            outputInstruction (m_instructionList.get (0), 1, b);
            b.append ("\n");
            int i = 0;
            for (i = 1; i < m_instructionList.size (); i++) {
                outputInstruction (m_instructionList.get (i), i + 1, b);
                b.append ("::\n");
            }
            b.append ("V(");
            b.append (i + 1);
            b.append (", end)::\n");
        }
        b.append ("nil)");
        return b.toString ();
    }

    private void outputInstruction (Instruction instruction, int i, StringBuffer b) {
        b.append ("V(");
        b.append (i);
        b.append (", do ");
        b.append (instruction.m_instruction);
        b.append (" then ");
        b.append (i + 1);
        b.append (")");
    }

}
