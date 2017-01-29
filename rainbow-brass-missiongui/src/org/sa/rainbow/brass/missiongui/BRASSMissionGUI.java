package org.sa.rainbow.brass.missiongui;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
 
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.JFrame;
 
public class BRASSMissionGUI 
{
	
    public static void main(String[] args) 
    {
    	
    	// setup OpenGL Version 2
    	GLProfile profile = GLProfile.get(GLProfile.GL2);
    	GLCapabilities capabilities = new GLCapabilities(profile);
 
    	Renderer r=new Renderer();
    	
    	// The canvas is the widget that's drawn in the JFrame
    	GLCanvas glcanvas = new GLCanvas(capabilities);
    	glcanvas.addGLEventListener(r);
    	glcanvas.addMouseListener(r);
    	glcanvas.addMouseMotionListener(r);

    	glcanvas.setSize( 640, 480 );
 
        JFrame frame = new JFrame( "BRASS Mission Monitor" );
        frame.getContentPane().add( glcanvas);
        
        final FPSAnimator animator = new FPSAnimator(glcanvas, 25);		
        animator.start();
        
        
 
        // shutdown the program on windows close event
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
 
        frame.setSize( frame.getContentPane().getPreferredSize() );
        frame.setVisible( true );
    }
}