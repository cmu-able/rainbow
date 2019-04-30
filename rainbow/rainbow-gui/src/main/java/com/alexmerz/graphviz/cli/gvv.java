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

package com.alexmerz.graphviz.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Id;
import com.alexmerz.graphviz.objects.Node;

/**
 * Main application class to view a flat 
 * dump of the Graph datastructure in a Graphviz
 * document
 * 
 * @version $Id: gvv.java,v 1.4 2006/03/20 16:45:41 Alexander Exp $
 * 
 */
public class gvv {

	/**
	 * Error code, if no file was given
	 */
	final static int NOFILE = 11;
	
	/**
	 * Error code, if given file couldn't be found 
	 * or opened
	 */
	final static int FILENOTFOUND = 12;
	
	/**
	 * Error code, Error while parsing
	 */
	final static int PROCESSING = 13;
	
	/**
	 * The main method
	 * 
	 * @param args commandline arguments
	 */
	public static void main(String[] args) {
		if(args.length!=1) {
			errorDie(NOFILE,"");
		}
		if(args[0].equals("-h") || args[0].equals("--help")) {
			showUsage();
			System.exit(0);
		} else {
			go(args[0]);
			System.exit(0);
		}
	}
	
	/**
	 * Exit with a error code and message
	 * @param msg
	 * @param msgS
	 */
	private static void errorDie(int msg, String msgS) {
		if(msg==NOFILE) {			
			System.err.println("ERROR: You must provide a Graphviz file!\n");
			showUsage();			
		} else if(msg==FILENOTFOUND) {
			System.err.println("ERROR: "+msgS+"\n");					
		} else if(msg==PROCESSING) {
			System.err.println("ERROR: "+msgS+"\n");
		}
		System.exit(msg);
	}
	
	/**
	 * Shows the command help
	 *
	 */
	private static void showUsage() {
		System.out.print("Usage: java -jar graphviz.jar <file>\n");
	}
	
	/**
	 * starts parsing 
	 * @param file the name of the file
	 */
	private static void go(String file) {
        FileReader in=null;
        
           	File f = new File( file );
            try {
				in = new FileReader(f);			
				Parser p = new Parser();
				p.parse(in);
				show(p);
            } catch (FileNotFoundException e) {
            	errorDie(FILENOTFOUND, "Could not found "+f.getAbsolutePath());
			} catch (ParseException e) {
				errorDie(PROCESSING, e.getMessage());
			}                            		
	}
	
	/**
	 * Shows the result of parsing
	 * @param p the Parser object
	 */
	private static void show(Parser p) {
		Graph g = null;	
		ArrayList<Node> nl = null;
		ArrayList<Edge> el = null;
		ArrayList<Graph> sl = null;
		ArrayList<Graph> gl = p.getGraphs();		
		
		for(int i=0; i<gl.size(); i++) {
			g = gl.get(0);			
			System.out.print(getGraphData(g, ""));
			System.out.print("\n");
			nl = g.getNodes(false);
			for(int a=0; a<nl.size(); a++) {
				System.out.print(getNodeData(nl.get(a),""));
				System.out.print("\n");
			}
			el = g.getEdges();					
			for(int a=0; a<el.size(); a++) {
				System.out.print(getEdgeData(el.get(a),""));
				System.out.print("\n");
			}
			sl = g.getSubgraphs();					
			for(int a=0; a<sl.size(); a++) {
				System.out.print(getSubgraphData(sl.get(a),""));
				System.out.print("\n");
			}
			System.out.print("%%\n");			
		}
	}
	
	/**
	 * Fetches the Graph data of a sub graph
	 * @param g the Graph object
	 * @param prefix an output prefix
	 * @return the string of the graph data
	 */
	private static String getSubgraphData(Graph g, String prefix) {
		String template = "";
		String id = "";
		if(!g.getId().getId().equals("")) {
			id = g.getId().getId();
		} else {
			id = g.getId().getLabel();
		}
		String pf = "SUBGRAPH: " + id + " : "+ prefix;
		ArrayList<Node> nl = null;
		ArrayList<Edge> el = null;
		ArrayList<Graph> sl = null;
								
			template = template + getGraphData(g, pf);
			template = template + "\n";
			nl = g.getNodes(false);
			for(int a=0; a<nl.size(); a++) {
				template = template + getNodeData(nl.get(a),pf);
				template = template + "\n";
			}
			el = g.getEdges();					
			for(int a=0; a<el.size(); a++) {
				template = template + getEdgeData(el.get(a),pf);
				template = template + "\n";
			}
			sl = g.getSubgraphs();					
			for(int a=0; a<sl.size(); a++) {
				template = template + getSubgraphData(sl.get(a),pf);
				template = template + "\n";
			}
					
		return template;
	}
	
	/**
	 * Fetches the edge data
	 * @param e the edge object
	 * @param prefix an output prefix
	 * @return a string containing the edge data
	 */
	private static String getEdgeData(Edge e, String prefix) {
		String template="EDGE: ";
		if(!e.getSource().getNode().getId().getId().equals("")) {
			template = template + e.getSource().getNode().getId().getId();
		} else {
			template =  template + "\"" + e.getSource().getNode().getId().getLabel()+"\"";
		}
		template = template + "(" + e.getSource().getPort() + ")";		
		if(e.getSource().getNode().isSubgraph()) {
			template = template + "[*]";
		}
		if(e.getType() == Graph.DIRECTED) {
			template = template + " -> ";
		} else {
			template = template + " -- ";
		}
		if(!e.getTarget().getNode().getId().getId().equals("")) {
			template = template + e.getTarget().getNode().getId().getId();
		} else {
			template = template + "\"" + e.getTarget().getNode().getId().getLabel()+"\"";
		}
		template = template + "(" + e.getTarget().getPort() + ")";
		if(e.getTarget().getNode().isSubgraph()) {
			template = template + "[*]";
		}				
		String text = "";
		Hashtable<String, String> ht = e.getAttributes();
		Enumeration<String> en = ht.keys();
		String key=""; 
		while(en.hasMoreElements()) {
			key = en.nextElement();
		    text = text +prefix + template +" : PROPERTY "+key+"="+ht.get(key)+"\n";
		    
		}
		if(key.equals("")) {
			text = template+"\n";
		} 
		return text.substring(0,text.length()-1);
	}
	
	/**
	 * Fetches the Node data
	 * @param n the Node object
	 * @param prefix an output prefix
	 * @return the string with the Node data
	 */
	private static String getNodeData(Node n, String prefix) {
		String id = "";
		Enumeration<String> e=null;
		Hashtable<String,String> ht = null;
		if(!n.getId().getId().equals("")) {
			id = n.getId().getId();
		} else {
			id = "\""+n.getId().getLabel()+"\"";
		}
		String template = prefix + "NODE: "+id+" : ID="+n.getId().getId()+"\n"+
			prefix + "NODE: "+id+" : LABEL="+ n.getId().getLabel()+"\n";
		ht = n.getAttributes();
		e = ht.keys();
		String key = "";
		while(e.hasMoreElements()) {
			key = e.nextElement();
			template = template + prefix + 
			"NODE: "+id+" : PROPERTY " + key + "=" + ht.get(key)+"\n"; 
		}
		return template.substring(0,template.length()-1);	
	}

	/**
	 * Fetches the Graph data
	 * @param g the Graph object
	 * @param prefix an output prefix
	 * @return the string of the graph data
	 */
	private static String getGraphData(Graph g, String prefix) {
		String template=
			prefix + "GRAPH: ID="+g.getId().getId()+"\n"+
			prefix + "GRAPH: LABEL="+g.getId().getLabel()+"\n"+
			prefix + "GRAPH: STRICT="+((g.isStrict())?"true":"false")+"\n"+
			prefix + "GRAPH: DIRECTION="+((g.getType()==Graph.DIRECTED)?"directed":"undirected");		
		Hashtable<String,String> ht = g.getAttributes();
		Enumeration<String> e = ht.keys();
		String key = "";
		while(e.hasMoreElements()) {
			key = e.nextElement();
			template = template +"\n" + prefix + 
					"GRAPH: PROPERTY " +
					key + "=" + ht.get(key);			
		}
		if(key.equals("")) {
			template = template+"\n";
		}
		ArrayList<Node> nl = g.getNodes(false);		
		Id id = null;
		if(nl.size()>0 && !key.equals("")) {
			template = template+"\n";
		}
		for(int i=0; i<nl.size();i++) {
			template = template +prefix + "GRAPH: NODE=";
			id = nl.get(i).getId();
			if(!id.getId().equals("")) {
				template = template + id.getId();
			} else {
				template = template + "\"" + id.getLabel() + "\"";
			}			
			template = template+"\n";
		}		
		
		return template.substring(0,template.length()-1);	
	}

}
