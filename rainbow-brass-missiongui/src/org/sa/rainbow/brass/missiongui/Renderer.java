package org.sa.rainbow.brass.missiongui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.brass.missiongui.IBRASSOperations;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.missiongui.NotificationWindow;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;


class Renderer implements GLEventListener, MouseListener, MouseMotionListener, IBRASSOperations
{
    private EnvMap m_map=null;
    private PrismPolicy m_policy=null;
    private GLU glu = new GLU();
    private GL2 gl_ref;

    private LinkedList<Notification> m_notifications;
    private NotificationWindow m_notification_window;

    private int viewport_w;
    private int viewport_h;

    private float rot_loc=0.0f;
    private float robot_x = 52.22f;
    private float robot_y = 69.0f;
    private boolean robot_obstructed=false;

    private float prevMouseX = 0.0f;
    private float prevMouseY = 0.0f;
    private boolean mouseRButtonDown = false;

    private String target_location="l1";

    private final float DEG2RAD = 3.14159f/180.0f;

    private final float[] COLOR_ARC_ENABLED = {0.0f, 1.0f, 0.0f};
    private final float[] COLOR_ARC_DISABLED = {0.3f, 0.3f, 0.3f};
    private final float[] COLOR_PLAN_ARC = {1.0f, 1.0f, 1.0f};
    private final float[] COLOR_LOCATION = {1.0f, 1.0f, 1.0f};
    private final float[] COLOR_TARGET_LOCATION = {1.0f, 0.5f, 0.0f};
    private final float[] COLOR_ROBOT = {0.3f, 0.3f, 1.0f};
    private final float[] COLOR_ROBOT_OBSTRUCTED ={1.0f, 0.0f, 0.0f};


    private final float SIZE_NODE = 0.5f;
    private final float THICKNESS_ARC = 12.0f;
    private final float THICKNESS_PLAN_ARC = 10.0f;
    private final float THICKNESS_NORMAL = 2.0f;

    public Renderer(){
        m_notifications = new LinkedList<Notification> ();
        m_notification_window = new NotificationWindow(this, 15, 55, 55, 50);
//        try {
//            RainbowAdapter ra = new RainbowAdapter (new IBRASSOperations () {
//
//                private float m_r_x = 0;
//                private float m_r_y = 0;
//
//                @Override
//                public void setRobotObstructed (Boolean obstructed) {
//                    if (obstructed) {
//                        m_notifications.add (new Notification (Renderer.this, m_r_x, m_r_y, 5, "Robot obstructed"));
//                    }
//
//                }
//
//                @Override
//                public void setRobotLocation (Double x, Double y) {
//                    m_r_x = x.floatValue ();
//                    m_r_y = y.floatValue ();
//                    Renderer.this.drawRobot (m_r_x, m_r_y);
//
//                }
//
//                @Override
//                public void setInstructionFailed (Boolean result) {
//                    // TODO Auto-generated method stub
//
//                }
//
//                @Override
//                public void setExecutingInstruction (String label) {
//                    // TODO Auto-generated method stub
//
//                }
//
//                @Override
//                public void reportFromDAS (String string) {
//
//                }
//
//                @Override
//                public void newInstructionGraph (InstructionGraphProgress igModel) {
//                    // TODO Auto-generated method stub
//
//                }
//
//                @Override
//                public void insertMapNode (String n, String na, String nb, Double x, Double y) {
//                    Renderer.this.m_map.insertNode (n, na, nb, x, y);
//                }
//            });
//        }
//        catch (RainbowConnectionException e) {
//            System.err.println ("Could not connect to Rainbow");
//            e.printStackTrace ();
//        }
    }

    public GL2 getGLRef(){
        return gl_ref;
    }

    public void setColor(float[] c){
        gl_ref.glColor3f(c[0],c[1],c[2]);
    }

    public void glPrint(float x, float y, String s) {
        gl_ref.glRasterPos2f(x, y);
        GLUT glut = new GLUT();
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, s);
    }

    public void drawCircle(float x, float y, float radius) {
        gl_ref.glPushMatrix();
        gl_ref.glTranslatef(x, y, 0.0f);
        gl_ref.glBegin(GL2.GL_LINE_LOOP);
        for (int i=0; i < 360; i++) {
            float degInRad = i*DEG2RAD;
            gl_ref.glVertex3f((float)Math.cos(degInRad)*radius,(float)Math.sin(degInRad)*radius, 0.0f);
        }
        gl_ref.glEnd();
        gl_ref.glPopMatrix();
    }

    public void drawTriangle(float x, float y, float size) {
        gl_ref.glPushMatrix();
        gl_ref.glTranslatef(x, y, 0.0f);
        gl_ref.glBegin(GL2.GL_TRIANGLE_STRIP);
        gl_ref.glVertex3f(-size, 0.0f, 0.0f);
        gl_ref.glVertex3f(size, size, 0.0f);
        gl_ref.glVertex3f(size, -size, 0.0f);
        gl_ref.glVertex3f(-size, 0.0f, 0.0f);
        gl_ref.glEnd();
        gl_ref.glPopMatrix();
    }

    public void drawRobot(float x, float y){
        gl_ref.glLineWidth(4.0f);
        if (robot_obstructed){
        	setColor(COLOR_ROBOT_OBSTRUCTED);
        } else {
            setColor(COLOR_ROBOT);	
        }
        drawCircle(x,y,SIZE_NODE/3);
        drawCircle(x,y,SIZE_NODE);
        // 	gl_ref.glPushMatrix();
        // 	gl_ref.glTranslatef(-SIZE_NODE*2,0.0f, 0.0f);
        // 	gl_ref.glRotatef(3.0f, 0.0f, 1.0f, 0.0f);
        // 	drawTriangle(x,y,SIZE_NODE);
        // 	gl_ref.glPopMatrix();
    }

    public void drawLocation(EnvMapNode n){
        float x = (float)n.getX();
        float y = (float)n.getY();
        GL2 gl = gl_ref;
        if (n.getLabel().equals(target_location)){
            setColor(COLOR_TARGET_LOCATION);
        } else {
            setColor(COLOR_LOCATION);    		
        }
        gl.glBegin(GL2.GL_QUADS);           	
        gl.glRotatef(rot_loc,0.0f,0.0f,1.0f);
        gl.glVertex3f(x-SIZE_NODE, y+SIZE_NODE, 0.0f);	
        gl.glVertex3f(x+SIZE_NODE, y+SIZE_NODE, 0.0f);	
        gl.glVertex3f(x+SIZE_NODE, y-SIZE_NODE, 0.0f);	
        gl.glVertex3f(x-SIZE_NODE, y-SIZE_NODE, 0.0f);	
        gl.glEnd();	
        gl.glLineWidth(THICKNESS_NORMAL);
        drawCircle(x, y, SIZE_NODE*1.5f);
        glPrint(x+SIZE_NODE*2, y+SIZE_NODE*2, n.getLabel());
    }

    public void drawMapLocations(){
        for (Map.Entry<String,EnvMapNode> entry : m_map.getNodes().entrySet() ){
            EnvMapNode n = entry.getValue();
            drawLocation(n);
        }
    }

    public void drawArc(EnvMapArc a){
        GL2 gl = gl_ref;
        gl.glLineWidth(THICKNESS_ARC);
        if (a.isEnabled()){
            setColor(COLOR_ARC_ENABLED);
        } else {
            setColor(COLOR_ARC_DISABLED);
        }
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f((float)m_map.getNodeX(a.getSource()), (float)m_map.getNodeY(a.getSource()), 0.0f);
        gl.glVertex3f((float)m_map.getNodeX(a.getTarget()), (float)m_map.getNodeY(a.getTarget()), 0.0f);
        gl.glEnd();

    }

    public void drawMapArcs(){
        for (int i=0;i<m_map.getArcs().size();i++){
            drawArc(m_map.getArcs().get(i));    	
        }
    }

    public void drawPlan(){
//    	ArrayList<String> plan = m_policy.getPlan();
        // TODO: Update this by a real plan obtained from the model
        ArrayList<String> plan = new ArrayList<String>() {{
            add("newnode_to_l4");
            add("l4_to_l5");
            add("l5_to_l6");
            add("l6_to_l7");
            add("l7_to_l8");
            add("l8_to_l2");
            add("l2_to_l1");
        }};

        for (int i = 0; i < plan.size(); i++) {
            String action = plan.get(i);
            String[] elements = action.split("_");
            String target_label = elements[2];
            String source_label = elements[0];
            setColor(COLOR_PLAN_ARC);
            gl_ref.glLineWidth(THICKNESS_PLAN_ARC);
            gl_ref.glBegin(GL2.GL_LINES);
            gl_ref.glVertex3f((float)m_map.getNodeX(source_label), (float)m_map.getNodeY(source_label), 0.0f);
            gl_ref.glVertex3f((float)m_map.getNodeX(target_label), (float)m_map.getNodeY(target_label), 0.0f);
            gl_ref.glEnd();

        }    	
    }

    public void drawNotifications(){
        for (int i=0;i<m_notifications.size();i++){
            Notification n = m_notifications.get(i);
            n.render();
            if (n.expired()){
                m_notifications.remove(i);
            }
        }
    }

    @Override
    public void display(GLAutoDrawable gLDrawable) 
    {
        final GL2 gl = gLDrawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(-35.0f, -65.0f, -40.0f);
        drawMapArcs();
        drawPlan();
        drawMapLocations();
        drawRobot(robot_x, robot_y);
        drawNotifications();
        
        m_notification_window.render();

        gl.glFlush();

    }


    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) 
    {
        System.out.println("displayChanged called");
    }

    @Override
    public void init(GLAutoDrawable gLDrawable) 
    {

        // Map init
        m_map = new EnvMap(null);		
        m_map.insertNode("newnode", "l4", "l3", 46.0, 67.0);
        m_policy = new PrismPolicy("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
        m_policy.readPolicy();


        // GL Setup
        System.out.println("init() called");
        GL2 gl = gLDrawable.getGL().getGL2();
        gl_ref = gl;
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL2.GL_FLAT);
    }

    @Override
    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) 
    {
        //System.out.println("reshape() called: x = "+x+", y = "+y+", width = "+width+", height = "+height);
        final GL2 gl = gLDrawable.getGL().getGL2();

        if (height <= 0) // avoid a divide by zero error!
        {
            height = 1;
        }

        viewport_w = width;
        viewport_h = height;
        final float h = (float) width / (float) height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1.0, 50.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {} 

    @Override
    public void mousePressed(MouseEvent e) {
        prevMouseX = e.getX();
        prevMouseY = e.getY();
        if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
            mouseRButtonDown = true;
        }
        System.out.println("Mouse pressed @ "+ String.valueOf(prevMouseX)+" "+String.valueOf(prevMouseY));
        //m_notifications.add(new Notification(this,prevMouseX,prevMouseY,250,"Mouse pressed @ "+ String.valueOf(prevMouseX)+" "+String.valueOf(prevMouseY)));
        //m_notifications.add(new Notification(this,robot_x,robot_y,250,"Mouse pressed @ "+ String.valueOf(prevMouseX)+" "+String.valueOf(prevMouseY)));

    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if ((e.getModifiers() & e.BUTTON3_MASK) != 0) {
            mouseRButtonDown = false;
        }
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
    }


    @Override
    public void dispose(GLAutoDrawable arg0) 
    {
        System.out.println("dispose() called");
    }
    
    @Override
    public void setRobotLocation (Double x, Double y){
    	robot_x = x.floatValue();
    	robot_y = y.floatValue();
    }
    
    @Override
    public void setRobotObstructed (Boolean obstructed){
    	if (obstructed){
    		m_notifications.add(new Notification(this,robot_x+SIZE_NODE,robot_y,100,"Robot blocked."));
    	}
    	robot_obstructed = obstructed;
    }
    
    @Override
    public void insertMapNode (String n, String na, String nb, Double x, Double y){
    	m_map.insertNode(n, na, nb, x, y);
    }
    
    @Override
    public void reportFromDAS (String string){
    	m_notification_window.addNotification(string);
    }
    
    @Override
    public void setExecutingInstruction (String label){
    	
    }

    @Override
    public void setInstructionFailed (Boolean result){
    	
    }
    
    @Override
    public void newInstructionGraph (InstructionGraphProgress igModel){
    	
    }
    
}