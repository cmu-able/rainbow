package org.sa.rainbow.brass.missiongui;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import org.sa.rainbow.brass.missiongui.BRASSMissionGUITester;

public class BRASSMissionGUI 
{
	
	private static int FRAME_RATE=25;

    public static void main(String[] args) 
    {

        // setup OpenGL Version 2
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        
        
        
        Renderer r=new Renderer(FRAME_RATE);

        // The canvas is the widget that's drawn in the JFrame
        GLCanvas glcanvas = new GLCanvas(capabilities);
        glcanvas.addGLEventListener(r);
        glcanvas.addMouseListener(r);
        glcanvas.addMouseMotionListener(r);

        glcanvas.setSize( 1024, 768 );

        JFrame frame = new JFrame( "BRASS Mission Monitor" );
        frame.getContentPane().add( glcanvas);

        final FPSAnimator animator = new FPSAnimator(glcanvas, FRAME_RATE);		
        animator.start();

        final BRASSMissionGUITester t = new BRASSMissionGUITester (r);
        t.start();

        // shutdown the program on windows close event
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });


        frame.setSize( frame.getContentPane().getPreferredSize() );
        frame.setVisible( true );
    }
}