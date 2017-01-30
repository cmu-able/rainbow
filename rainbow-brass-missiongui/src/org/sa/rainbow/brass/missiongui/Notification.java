package org.sa.rainbow.brass.missiongui;

import org.sa.rainbow.brass.missiongui.Renderer;

public class Notification {

	private final int fade_period = 25;
	
	private String m_text;
	private float m_posx;
	private float m_posy;
	private int m_duration;
	private int m_elapsed;
	private Renderer m_ref;
	
	public Notification(Renderer r, float x, float y, int duration, String text){
		m_ref=r;
		m_posx = x;
		m_posy = y;
		m_duration = duration;
		m_elapsed = m_duration;
		m_text = text;
	}
	
	public boolean expired(){
		return m_elapsed == 0;
	}
	
	public void render() {
		float fi;
		if (m_duration-m_elapsed<fade_period){
			fi = (float)(m_duration-m_elapsed)/(float)fade_period;
		} else
		if (m_elapsed<fade_period){
			fi = (float)m_elapsed/(float)fade_period;
		} else { 
			fi = 1.0f;
		}			
		if (m_elapsed>0){
			m_ref.getGLRef().glColor3f(fi,fi,fi);
			m_ref.glPrint(m_posx, m_posy-fi, m_text);
			m_elapsed--;
		}
	}
	
	public String getM_text() {
		return m_text;
	}
	public void setM_text(String m_text) {
		this.m_text = m_text;
	}
	public float getM_posx() {
		return m_posx;
	}
	public void setM_posx(float m_posx) {
		this.m_posx = m_posx;
	}
	public float getM_posy() {
		return m_posy;
	}
	public void setM_posy(float m_posy) {
		this.m_posy = m_posy;
	}
	public int getM_duration() {
		return m_duration;
	}
	public void setM_duration(int m_duration) {
		this.m_duration = m_duration;
	}
	public int getM_elapsed() {
		return m_elapsed;
	}
	public void setM_elapsed(int m_elapsed) {
		this.m_elapsed = m_elapsed;
	}
	
	
}
