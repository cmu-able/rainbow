package org.sa.rainbow.brass.missiongui;

import java.util.LinkedList;

import org.sa.rainbow.brass.missiongui.Notification;
import org.sa.rainbow.brass.missiongui.Renderer;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

public class NotificationWindow {
	private Renderer m_ref=null;
	private float m_left;
	private float m_right;
	private float m_top;
	private float m_bottom;
	
	private float PADDING_H = 1.0f;
	private float PADDING_V = 0.2f;
	private float STEP_V = 0.7f;
	private int MAX_NOTIFICATIONS = 6;
	
	
	private final float[] WINDOW_COLOR = {0.3f, 0.5f, 0.3f};
	private final float[] TEXT_COLOR = {1.0f, 1.0f, 1.0f};

	private LinkedList<String> m_notifications;
	
	
	public NotificationWindow(Renderer r, float left, float top, float right, float bottom){
		m_ref=r;
		m_left=left;
		m_right=right;
		m_top=top;
		m_bottom=bottom;
		m_notifications = new LinkedList<String> ();
		
	}
	
	public void setColor(float[] c){
		GL2 gl_ref=m_ref.getGLRef();
        gl_ref.glColor3f(c[0],c[1],c[2]);
    }
	
	public void glPrint(float x, float y, String s) {
		GL2 gl_ref=m_ref.getGLRef();
		gl_ref.glRasterPos2f(x, y);
		GLUT glut = new GLUT();
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, s);
	}
	
	public void drawNotifications(){
        for (int i=0;i<m_notifications.size();i++){
            String n = m_notifications.get(i);
            glPrint(m_left+PADDING_H, m_top-PADDING_V-(float)(i+1)*STEP_V,n);
        }
    }
	
	public void render(){	
		GL2 gl_ref=m_ref.getGLRef();
		setColor(WINDOW_COLOR);
		gl_ref.glBegin(GL2.GL_QUADS);
		gl_ref.glVertex3f(m_left, m_top, 0.0f);
		gl_ref.glVertex3f(m_right, m_top, 0.0f);
		gl_ref.glVertex3f(m_right, m_bottom, 0.0f);
		gl_ref.glVertex3f(m_left, m_bottom, 0.0f);		
		gl_ref.glEnd();
		setColor(TEXT_COLOR);
		drawNotifications();
	}
	
	public void addNotification(String string){
		if (m_notifications.size()>=MAX_NOTIFICATIONS){
			m_notifications.removeFirst();
		}
		m_notifications.add(string);
	}
	
}
