package org.sa.rainbow.brass.model.instructions;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
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
        List<IInstruction> instructions = parseFromString (igStr);
        InstructionGraphProgress ig = new InstructionGraphProgress (ref);
        ig.setInstructions (instructions);
        return ig;
    }

    public static List<IInstruction> parseFromString (String igStr) {
        List<IInstruction> instructions = new LinkedList<IInstruction> ();
        igStr = igStr.replace ("\n", "").replace ("\r", "");
        igStr = igStr.substring (2); // Remove P(
        String[] is = igStr.split ("V");
        Pattern instructionPattern = Pattern.compile ("\\((.*), do (.*) then (.*).*");

        for (String i : is) {
            Matcher m = instructionPattern.matcher (i);

            if (m.matches ()) {
                String label = m.group(1);
                String instruction = m.group(2);
                String nextLabel = m.group(3);
                IInstruction inst2;

                if (instruction.startsWith(MoveAbsHInstruction.COMMAND_NAME)) {
                    inst2 = new MoveAbsHInstruction(label, instruction, nextLabel);
                } else {
                    //TODO
                    inst2 = new MoveAbsHInstruction(label, instruction, nextLabel);
                }

                instructions.add(inst2);
            }
        }
        return instructions;
    }

    public Collection<? extends IInstruction> getInstructions () {
        return m_instructionList;
    }

    /**
     * 
     * @return The remaining instructions, excluding the current instruction, to be executed
     */
    public Collection<? extends IInstruction> getRemainingInstructions () {
        List<IInstruction> remainingInstructions = new LinkedList<>();
        IInstruction instPtr = getCurrentInstruction();

        while (instPtr != null && m_instructions.containsKey(instPtr.getNextInstructionLabel())) {
            String nextLabel = instPtr.getNextInstructionLabel();
            IInstruction nextInstruction = m_instructions.get(nextLabel);
            remainingInstructions.add(nextInstruction);
            instPtr = m_instructions.get(nextLabel);
        }

        return remainingInstructions;
    }

    public IInstruction getInstruction (String instLabel) {
        return m_instructions.get(instLabel). copy ();
    }

    public String getExecutingInstruction () {
        return m_currentNode;
    }

    public boolean getCurrentOK () {
        return m_currentOK;
    }

    public IInstruction getCurrentInstruction () {
        return m_instructions.get (m_currentNode);
    }

    public static class ExecutionObservation {
        String  label;
        boolean successful;
        long    startTime;
        long    endTime;
    }

    private Map<String, IInstruction>    m_instructions     = new HashMap<> ();
    private List<IInstruction>          m_instructionList  = Collections.<IInstruction> emptyList ();
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

        for (Map.Entry<String, IInstruction> i : m_instructions.entrySet ()) {
            ig.m_instructions.put (i.getKey (), i.getValue ().copy ());
        }

        return ig;
    }

    public void setInstructions (List<IInstruction> instructions) {
        m_instructionList = new LinkedList<IInstruction> (instructions);
        m_instructions.clear ();
        for (IInstruction i : instructions) {
            m_instructions.put (i.getInstructionLabel(), i);
        }
    }

    public void setExecutingInstruction (String instLabel) {
//    	if (m_instructions.containsKey (instLabel)) {
        m_currentNode = instLabel;
        if (!m_currentOK)
        {
            m_currentOK = true;
//        }
//        ExecutionObservation observation = new ExecutionObservation ();
//        observation.startTime = new Date().getTime ();
//        observation.label = instLabel;
//        observation.
//        m_executionHistory.push (observation);
        }

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

    private void outputInstruction (IInstruction instruction, int i, StringBuffer b) {
        b.append ("V(");
        b.append (i);
        b.append (", do ");
        b.append (instruction.getInstruction());
        b.append (" then ");
        b.append (i + 1);
        b.append (")");
    }

}
