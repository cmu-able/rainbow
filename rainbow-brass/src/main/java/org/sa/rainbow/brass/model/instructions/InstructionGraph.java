package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.models.ModelReference;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraph {



    public static InstructionGraph parseFromString (ModelReference ref, String igStr) {
        // Remove all the returns from string
        List<Instruction> instructions = parseFromString (igStr);
        InstructionGraph ig = new InstructionGraph (ref);
        ig.setInstructions (instructions);
        return ig;
    }

    public static List<Instruction> parseFromString (String igStr) {
        List<Instruction> instructions = new LinkedList<Instruction> ();
        igStr = igStr.replace("\n","").replace ("\r", "");
        igStr = igStr.substring (2); // Remove P(
        String[] is = igStr.split("V");
        Pattern instructionPattern = Pattern.compile ("V\\((.*), do (.*) then (.*).*");
        for (String i :
                is) {
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

    public String getExecutingInstruction () {
        return m_currentNode;
    }

    public boolean getCurrentOK () {
        return m_currentOK;
    }


    public static class Instruction {
        public String m_label;
        public String m_instruction;
        public String m_next;

        public Instruction copy () {
            Instruction i = new Instruction ();
            i.m_instruction = new String (m_instruction);
            i.m_label = new String (m_label);
            i.m_next = new String (m_next);
            return i;
        }
    }

    private Map<String, Instruction> m_instructions = new HashMap<> ();
    private LinkedList<Instruction> m_instructionList;
    private final ModelReference           m_model;
    private String m_currentNode;
    private boolean m_currentOK;

    public InstructionGraph (ModelReference model) {
        m_model = model;
    }

    public ModelReference getModelReference () {
        return m_model;
    }

    public InstructionGraph copy () {
        InstructionGraph ig = new InstructionGraph (m_model);
        ig.m_instructions = new HashMap<> ();

        for (Map.Entry<String,Instruction> i :
                m_instructions.entrySet ()) {
            ig.m_instructions.put (i.getKey (), i.getValue ().copy ());
        }

        return ig;
    }

    public void setInstructions (List<Instruction> instructions) {
        Instruction prev = null;
        m_instructionList = new LinkedList<Instruction> (instructions);
        m_instructions.clear ();
        for (Instruction i : instructions) {
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
    }

    public void setCurrentOK (boolean ok) {
        m_currentOK = ok;
    }

    // Note: this assumes sequential instructions
    public String toString () {
        StringBuffer b = new StringBuffer ();
        b.append ("P(");
        if (!m_instructionList.isEmpty ()) {
            outputInstruction(m_instructionList.get (0), 1, b);
            b.append("\n");
            int i = 0;
            for (i = 1; i < m_instructionList.size (); i++) {
                outputInstruction (m_instructionList.get (i), i+ 1, b);
                b.append ("::\n");
            }
            b.append ("V(");
            b.append (i+1);
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
        b.append (i+1);
        b.append (")");
    }
    
    

}
