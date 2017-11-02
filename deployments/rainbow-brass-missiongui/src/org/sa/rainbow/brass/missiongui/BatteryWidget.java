package org.sa.rainbow.brass.missiongui;

import org.sa.rainbow.brass.model.map.MapTranslator;

import java.text.DecimalFormat;
import java.text.NumberFormat;


import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

public class BatteryWidget {
	protected Renderer m_ref=null;
	protected float m_left;
	protected float m_width;
	protected float m_top;
	protected float m_length;
	protected double m_voltage;
	protected double m_charge;
	protected boolean m_charging;
	
	protected final double MAX_VOLTAGE = 165.0;
	protected final double MIN_VOLTAGE = 104.0;
	protected final double MAX_CHARGE = Double.parseDouble(MapTranslator.ROBOT_BATTERY_RANGE_MAX);
	
	protected float BORDER_THICKNESS = 0.1f;
	
	protected float LOW_BATTERY_THRESHOLD = 20.0f;
	
	protected final float[] NORMAL_BATTERY_COLOR = {0.5f, 1.0f, 0.5f};
	protected final float[] LOW_BATTERY_COLOR = {1.0f, 0.5f, 0.5f};
	protected final float[] BORDER_COLOR = {0.3f, 0.5f, 0.3f};
	protected final float[] TEXT_COLOR = {1.0f, 1.0f, 1.0f};

	
	public BatteryWidget(Renderer r, float left, float top, float length, float width){
		m_ref=r;
		m_left=left;
		m_width=width;
		m_top=top;
		m_length=length;		
		m_voltage = MAX_VOLTAGE;
		m_charge = MAX_CHARGE;
		m_charging = false;
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
	
	public void setVoltage(double v){
		if (v>=MIN_VOLTAGE && v<=MAX_VOLTAGE)
			m_voltage = v; 
	}
	
	public void setCharge(double c){
		if (c>=0 && c <= MAX_CHARGE)
			m_charge = c;
	}
	
	public boolean isCharging(){
		return m_charging;
	}
	
	public void setCharging(boolean c){
		m_charging = c;
	}
	
	public void drawLightningBolt(float x, float y, float size){
		GL2 gl_ref=m_ref.getGLRef();
	   	gl_ref.glBegin(GL2.GL_LINE_STRIP);
	    gl_ref.glVertex3f(x+size*(2.0f/3.0f),y, 0.0f);
	    gl_ref.glVertex3f(x+size*(4.0f/3.0f),y, 0.0f);
	    gl_ref.glVertex3f(x+size*(2.0f/3.0f),y-size, 0.0f);
	    gl_ref.glVertex3f(x+size*(4.0f/3.0f),y-size, 0.0f);
	    gl_ref.glVertex3f(x+size/3.0f,y-size*2, 0.0f);
	    gl_ref.glVertex3f(x+size*(2.0f/3.0f),y-size*(4.0f/3.0f), 0.0f);
	    gl_ref.glVertex3f(x,y-size*(4.0f/3.0f), 0.0f);
	    gl_ref.glVertex3f(x+size*(2.0f/3.0f),y, 0.0f);
	    gl_ref.glEnd();
	}
	
	public void render(){	
		GL2 gl_ref=m_ref.getGLRef();
		NumberFormat f = new DecimalFormat("#0.0");
		
		setColor(BORDER_COLOR);
		gl_ref.glBegin(GL2.GL_QUADS);
		gl_ref.glVertex3f(m_left-BORDER_THICKNESS, m_top+BORDER_THICKNESS, 0.0f);
		gl_ref.glVertex3f(m_left+m_length+BORDER_THICKNESS, m_top+BORDER_THICKNESS, 0.0f);
		gl_ref.glVertex3f(m_left+m_length+BORDER_THICKNESS, m_top+m_width-BORDER_THICKNESS, 0.0f);
		gl_ref.glVertex3f(m_left-BORDER_THICKNESS, m_top+m_width-BORDER_THICKNESS, 0.0f);		
		gl_ref.glEnd();
		
		float v_offset=2;
		gl_ref.glBegin(GL2.GL_QUADS);
		gl_ref.glVertex3f(m_left-BORDER_THICKNESS,v_offset+ m_top+BORDER_THICKNESS, 0.0f);
		gl_ref.glVertex3f(m_left+m_length+BORDER_THICKNESS, v_offset+ m_top+BORDER_THICKNESS, 0.0f);
		gl_ref.glVertex3f(m_left+m_length+BORDER_THICKNESS, v_offset+ m_top+m_width-BORDER_THICKNESS, 0.0f);
		gl_ref.glVertex3f(m_left-BORDER_THICKNESS,v_offset+  m_top+m_width-BORDER_THICKNESS, 0.0f);		
		gl_ref.glEnd();
		
		
		
		double pc_charge = (m_charge/MAX_CHARGE)*100;
		if (pc_charge<LOW_BATTERY_THRESHOLD){
			setColor(LOW_BATTERY_COLOR);
		} else{
			setColor(NORMAL_BATTERY_COLOR);
		}	

		double charge_right = m_left + (m_charge/MAX_CHARGE) * m_length;
		
		gl_ref.glBegin(GL2.GL_QUADS);
		gl_ref.glVertex3f(m_left, m_top+BORDER_THICKNESS*2, 0.0f);
		gl_ref.glVertex3f((float)charge_right, m_top+BORDER_THICKNESS*2, 0.0f);
		gl_ref.glVertex3f((float)charge_right, m_top+m_width-BORDER_THICKNESS*2, 0.0f);
		gl_ref.glVertex3f(m_left, m_top+m_width-BORDER_THICKNESS*2, 0.0f);		
		gl_ref.glEnd();
			
		double voltage_right = m_left + ((m_voltage-MIN_VOLTAGE)/(MAX_VOLTAGE-MIN_VOLTAGE)) * m_length;
		
		gl_ref.glBegin(GL2.GL_QUADS);
		gl_ref.glVertex3f(m_left, v_offset+m_top+BORDER_THICKNESS*2, 0.0f);
		gl_ref.glVertex3f((float)voltage_right, v_offset+ m_top+BORDER_THICKNESS*2, 0.0f);
		gl_ref.glVertex3f((float)voltage_right,v_offset+ m_top+m_width-BORDER_THICKNESS*2, 0.0f);
		gl_ref.glVertex3f(m_left, v_offset+ m_top+m_width-BORDER_THICKNESS*2, 0.0f);		
		gl_ref.glEnd();
		
		setColor(TEXT_COLOR);
		String chargingStr="";
		
		if (isCharging()){
			drawLightningBolt(m_left+m_length+0.4f, m_top+(m_width/1.3f), 0.3f);
			drawLightningBolt(m_left+m_length+0.4f, v_offset+m_top+(m_width/1.3f), 0.3f);
			chargingStr =" [Charging]";
			
		}
		
		glPrint(m_left-3f, m_top+(m_width/2.0f),"Charge");
		glPrint(m_left+m_length+1.0f, m_top+(m_width/2.0f), String.valueOf(m_charge)+" milliwatt-hour"+" ("+f.format(pc_charge)+"%)"+chargingStr);
		
		glPrint(m_left-3f, v_offset+m_top+(m_width/2.0f),"Voltage");
		glPrint(m_left+m_length+1.0f, v_offset+m_top+(m_width/2.0f), String.valueOf(m_voltage)+" volts"+chargingStr);
		
		
	}

}
