
/***************************************************************
 *    JPGD - Java-based Parser for Graphviz Documents
 *    Copyright (C) 2006  Alexander Merz
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.alexmerz.graphviz.unittest;

import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;

/**
 * This test suite uses the Graphviz examples
 * from the Gallery on the Graphviz webpage.
 * (An exception is only NodeEdge) 
 * All files are stored in the testfiles directories.
 * 
 * @version $Id: Parser2Test.java,v 1.4 2006/03/20 16:47:49 Alexander Exp $
 * @author Alexander
 *
 */
public class Parser2Test extends TestCase {

	public void testNodeEdge() throws ParseException {
        FileReader in=null;
        try {
        	File f = new File( "testfiles/NodeEdge.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);            
            assertTrue(p.parse(in));  
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }               
	}
	
	public void testCluster() throws ParseException {
        FileReader in=null;
        try {
        	File f = new File( "testfiles/cluster.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
	}   
    
    public void testCrazy() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/crazy.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}
    
    public void testDatastruct() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/datastruct.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}
    
    public void testFdpclust() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/fdpclust.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}    

    public void testFSM() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/fsm.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

    public void testHello() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/hello.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}
   
    public void testInet() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/inet.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

    
       
    public void testProcess() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/process.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

    public void testProfile() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/profile.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

    public void testSdh() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/sdh.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}


    
    public void testSoftmain() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/softmain.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

    public void testSwitch() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/switch.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}    

    
    public void testTransparency() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/transparency.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

    
    public void testUnix() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/unix.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}    
    
    public void testWorld() throws ParseException {
        FileReader in=null;
        try {
          	File f = new File( "testfiles/world.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            assertTrue(p.parse(in));                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}

}
