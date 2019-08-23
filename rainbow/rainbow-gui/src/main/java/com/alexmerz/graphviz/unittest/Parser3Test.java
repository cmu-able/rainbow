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
import java.util.ArrayList;

import junit.framework.TestCase;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;


/**
 * The test.viz file contains some
 * heavy formated operations.
 * 
 * @version $Id: Parser3Test.java,v 1.5 2006/04/05 22:40:10 Alexander Exp $
 * @author Alexander
 *
 */
public class Parser3Test extends TestCase {
    
	public void testTest() throws ParseException {
        FileReader in=null;        
        
        try {
          	File f = new File( "testfiles/test.viz" );
            in = new FileReader(f);
            Parser p = new Parser(in);
            Boolean b = p.parse(in);
            ArrayList<Graph> al =p.getGraphs();
            for(int i=0; i<al.size();i++) {
            	System.out.println(al.get(i).toString());
            }
            assertTrue(b);                
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
	}
}
