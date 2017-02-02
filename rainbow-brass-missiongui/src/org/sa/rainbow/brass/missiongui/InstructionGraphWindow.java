package org.sa.rainbow.brass.missiongui;

import org.sa.rainbow.brass.missiongui.NotificationWindow;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.GL2;

public class InstructionGraphWindow extends NotificationWindow {

	
	private String m_executing_instruction ="";
	
	public InstructionGraphWindow(Renderer r, float left, float top, float right, float bottom) {
		super(r, left, top, right, bottom);
		// TODO Auto-generated constructor stub
	}
	
	protected final String DONE_STRING="(Done)";
	protected final String FAILED_STRING="(Failed)";
	protected final String EXECUTING_STRING="(Executing)";
	
	protected final float[] INSTRUCTION_COLOR = {0.7f, 0.7f, 0.7f};
	protected final float[] INSTRUCTION_DONE_COLOR = {0.5f, 1.0f, 0.5f};
	protected final float[] INSTRUCTION_EXECUTING_COLOR = {1.0f, 1.0f, 1.0f};
	protected final float[] INSTRUCTION_FAILED_COLOR = {1.0f, 0.5f, 0.5f};

	
	public void clearInstructions(){
			m_notifications.clear();
	}
	
	public void addInstruction(String string){
		addNotification(string);
		System.out.println("ADDIND NOTIFICATION: " + string);
	}
	
	public void instructionFailed(boolean result){
		  for (int i=0;i<m_notifications.size();i++){
	            String n = m_notifications.get(i);
	            if (result && n.equals(m_executing_instruction)){
	            	m_notifications.set(i, n+" "+FAILED_STRING);
	            }
		  }
	}
	
	public void setExecutingInstruction(String label){
		 for (int i=0;i<m_notifications.size();i++){
	            String n = m_notifications.get(i);
	            if (n.equals(m_executing_instruction) & !n.equals(label)){
	            	m_notifications.set(i, n+" "+DONE_STRING);
	            }
		 }
		m_executing_instruction = label;
	}
	
	public void drawNotifications(){
        for (int i=0;i<m_notifications.size();i++){
            String n = m_notifications.get(i);
            setColor (INSTRUCTION_COLOR);
            if (n.equals(m_executing_instruction)) 
            	setColor (INSTRUCTION_EXECUTING_COLOR);
            String[] elements = n.split(" ");
            if (elements[elements.length-1].equals(FAILED_STRING))
            	setColor (INSTRUCTION_FAILED_COLOR);
            if (elements[elements.length-1].equals(DONE_STRING))
            	setColor (INSTRUCTION_DONE_COLOR);            
            if (n.equals(m_executing_instruction))
            	glPrint(m_left+PADDING_H, m_top-PADDING_V-(float)(i+1)*STEP_V,n+" "+EXECUTING_STRING);
            else 
            	glPrint(m_left+PADDING_H, m_top-PADDING_V-(float)(i+1)*STEP_V,n);
        }
    }
	
}
