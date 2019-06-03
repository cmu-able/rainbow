/***************************************************************
 *    JPGD - Java-based Parser for Graphviz Documents
 *    Copyright (C) 2006, 2015  Alexander Merz, Braxton Fitts
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

package com.alexmerz.graphviz.objects;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class represents a graph.
 * 
 * A Graph contains Nodes, Edges and Subgraphs
 * 
 * @author Alexander
 * @author Braxton
 * @version $Id: Graph.java,v 1.5 2006/04/05 22:39:23 Alexander Exp $
 *
 */
public class Graph {

	/* Constants */
	/**
	 * Constant for an undirected graph
	 */
	public static final int UNDIRECTED = 1;
	
	/**
	 * Constant for an directed graph
	 */
	public static final int DIRECTED = 2;
	
	/**
	 * Identifier object for the graph
	 */
	private Id id=null;
	
	/**
	 * Holds all Node objects of this graph
	 */
	private ArrayList<Node> nodes = new ArrayList<Node>();
	
	/**
	 * Holds all Edge objects of this graph
	 */
	private ArrayList<Edge> edges = new ArrayList<Edge>();

	/**
	 * Holds all Subgraphs
	 */
	private ArrayList<Graph> graphs = new ArrayList<Graph>();	
	
	/**
	 * The type of this graph
	 * @see DIRECTED
	 * @see UNDIRECTED 
	 */
	private int type = 0;
	
	/**
	 * Wether the graph is strict or not
	 */
	private boolean strictgraph = false;
	
	/**
	 * Holds the graph attributes
	 */
	private Hashtable<String,String> attributes = new Hashtable<String, String>();
	
	/**
	 * Holds generic attributes for all edges
	 */
	private Hashtable<String,String> genericEdgeAttributes = new Hashtable<String, String>();
	
	/**
	 * Holds generic attributes for all nodes 
	 */
	private Hashtable<String,String> genericNodeAttributes = new Hashtable<String, String>();

	/**
	 * Holds generic attributes for graphs 
	 */
	private Hashtable<String,String> genericGraphAttributes = new Hashtable<String, String>();	
	
	/**
	 * Sets a generic attribute for all edges of this graph.
	 * This attributes are NOT additionally stored in the Edge objects. 
	 * 
	 * @param key the attribute name
	 * @param value the attribute value
	 */
	public void addGenericEdgeAttribute(String key, String value) {
		this.genericEdgeAttributes.put(key, value);
	}
	
	/**
	 * Returns a generic attribute of an edge
	 * @param key the attribute name
	 * @return the value of the attribute or null, if the attribute does not exist.
	 */
	public String getGenericEdgeAttribute(String key) {
		return (String)this.genericEdgeAttributes.get(key);
	}

	/**
	 * Sets a generic attribute for clusters of this graph.
	 * This attributes are NOT additionally stored in the Edge objects. 
	 * 
	 * @param key the attribute name
	 * @param value the attribute value
	 */
	public void addGenericGraphAttribute(String key, String value) {
		this.genericGraphAttributes.put(key, value);
	}
	
	/**
	 * Returns a generic attribute of the clusters in the graph
	 * @param key the attribute name
	 * @return the value of the attribute or null, if the attribute does not exist.
	 */
	public String getGenericGraphAttribute(String key) {
		return (String)this.genericGraphAttributes.get(key);
	}
	
	
	/**
	 * Sets a generic attribute for all nodes of this graph
	 * This attributes are NOT additionally stored in the Node objects.
	 * @param key the attribute name
	 * @param value the attribute value
	 */
	public void addGenericNodeAttribute(String key, String value) {
		this.genericNodeAttributes.put(key, value);
	}
	
	/**
	 * Returns a generic attribute of a node
	 * @param key the attribute name
	 * @return the value of the attribute or null, if the attribute does not exist.
	 */
	public String getGenericNodeAttribute(String key) {
		return (String)this.genericNodeAttributes.get(key);
	}	
	
	/**
	 * Adds a graph attribute.
	 * This attributes are NOT inherited to the attributes of sub graphs.
	 * 
	 * @param key the name of the attribute
	 * @param value the value of the attribute
	 */
	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	/**
	 * Returns an attribute of the Graph or null if not found.
	 * @param key the name of the attribute
	 * @return the value of the attribute
	 */
	public String getAttribute(String key) {
		return (String) attributes.get(key);
	}	
	
	/**
	 * Adds a Node object to the graph
	 * @param n the Node Object to add
	 */
	public void addNode(Node n) {
		nodes.add(n);
	}
	
	
	/**
	 * Adds an Edge object to the graph
	 * @param e the Edge object to add
	 */
	public void addEdge(Edge e) {
		edges.add(e);
	}
	
	/**
	 * Returns the Id object to the Graph
	 * @return the Id object
	 */
	public Id getId() {
		return id;
	}
	
	/**
	 * Sets the Id object for this graph
	 * @param id
	 */
	public void setId(Id id) {
		this.id = id;
	}

	/**
	 * Returns the type of the graph
	 * 
	 * @return the type of the graph
	 * @see com.alexmerz.graphviz.objects.Graph#DIRECTED
	 * @see com.alexmerz.graphviz.objects.Graph#UNDIRECTED
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Sets the type of the graph
	 * @param type the type of the graph
	 * @see com.alexmerz.graphviz.objects.Graph#DIRECTED
	 * @see com.alexmerz.graphviz.objects.Graph#UNDIRECTED
	 */
	public void setType(int type) {
		if(graphs.size()!=0) {			
			for(int i=0; i<graphs.size(); i++) {
				graphs.get(i).setType(type);				
			}
		}
		this.type = type;
	}
	
	/**
	 * Returns wether the graph is strict or not
	 * @return the static state
	 */
	public boolean isStrict() {
		return strictgraph;
	}
	
	/**
	 * Sets if the graph is strict or not.
	 * @param isStrict
	 */
	public void setStrict(boolean isStrict) {
		this.strictgraph = isStrict;
	}
	
	/**
	 * Returns a String representation of the graph
	 * @return the string representation
	 */
	public String toString() {
		StringBuffer r = new StringBuffer();
		if(isStrict()) {
			r.append("strict ");
		}
		if(DIRECTED == getType()) {
			r.append("digraph ");
		} else {
			r.append("graph ");
		}
		if(!id.getId().equals("")) {
			r.append(id.getId());
		} else if(!id.getLabel().equals("")) {
			r.append(id.getLabel());
		}
		r.append(" {\n");

		if(attributes.size()>0) { 		
			Enumeration<String> e = attributes.keys();
			String k;
			while(e.hasMoreElements()) {
				k = e.nextElement();
				r.append(k);
				if(!attributes.get(k).equals("")) {
					r.append("=");
					if(-1 == attributes.get(k).indexOf(" ")) {
						r.append(attributes.get(k));
					} else {
						r.append("\"");
						r.append(attributes.get(k));
						r.append("\"");					
					}					
				}
				r.append(";\n");				
			}			
		}		
		
		if(genericNodeAttributes.size()>0) { 		
			Enumeration<String> e = genericNodeAttributes.keys();
			String k;
			r.append("node [");
			while(e.hasMoreElements()) {
				k = e.nextElement();
				r.append(k);
				if(!genericNodeAttributes.get(k).equals("")) {				
					r.append("=");
					if(-1 == genericNodeAttributes.get(k).indexOf(" ")) {
						r.append(genericNodeAttributes.get(k));
					} else {
						r.append("\"");
						r.append(genericNodeAttributes.get(k));
						r.append("\"");					
					}			
				}
				r.append(", ");
			}
			r.delete(r.length()-2, r.length());			
			r.append("];\n");
		}
		
		if(genericEdgeAttributes.size()>0) { 		
			Enumeration<String> e = genericEdgeAttributes.keys();
			String k;
			r.append("edge [");
			while(e.hasMoreElements()) {
				k = e.nextElement();
				r.append(k);
				if(!genericEdgeAttributes.get(k).equals("")) {				
					r.append("=");
					if(-1 == genericEdgeAttributes.get(k).indexOf(" ")) {
						r.append(genericEdgeAttributes.get(k));
					} else {
						r.append("\"");
						r.append(genericEdgeAttributes.get(k));
						r.append("\"");					
					}			
				}
				r.append(", ");				
			}
			r.delete(r.length()-2, r.length());			
			r.append("];\n");
		}		

		if(genericGraphAttributes.size()>0) { 		
			Enumeration<String> e = genericGraphAttributes.keys();
			String k;
			r.append("graph [");
			while(e.hasMoreElements()) {
				k = e.nextElement();
				r.append(k);
				if(!genericGraphAttributes.get(k).equals("")) {				
					r.append("=");
					if(-1 == genericGraphAttributes.get(k).indexOf(" ")) {
						r.append(genericGraphAttributes.get(k));
					} else {
						r.append("\"");
						r.append(genericGraphAttributes.get(k));
						r.append("\"");					
					}
				}
				r.append(", ");
			}
			r.delete(r.length()-2, r.length());			
			r.append("];\n");
		}		
		if(nodes.size()>0) {
			for(int i=0; i<nodes.size(); i++) {
				r.append(nodes.get(i).toString());
			}
		}
		if(edges.size()>0) {
			for(int i=0; i<edges.size(); i++) {
				r.append(edges.get(i).toString());
			}
		}
		if(graphs.size()>0) {
			for(int i=0; i<graphs.size(); i++) {
				r.append(graphs.get(i).toString());
				r.append("\n");
			}
		}		
		r.append(" }");
		return r.toString();
/*		
		return "Graph: " + ((id!=null)?id.toString():"") + ", "+
				"static="+Boolean.toString(strictgraph)+", "+
				((type==DIRECTED)?"directed":"undirected")+
				", Attributes: "+attributes.toString()+
				", Nodes: "+nodes.toString()+
				", Edges: "+edges.toString()+
				", Subgraphs: "+graphs.toString();							
		*/
	}

	/**
	 * Returns a list of all sub graphs. 
	 * @return the subgraphs
	 */
	public ArrayList<Graph> getSubgraphs() {
		return graphs;
	}

	/** 
	 * Adds a sub graph to a graph
	 * @param graph
	 */
	public void addSubgraph(Graph graph) {
		this.graphs.add(graph);
	}
	
	/**
	 * Tries to find a node of a Graph depending
	 * on the given Id object.
	 * @param id the id object to identify the node
	 * @return the node or nulll if not found
	 */
	public Node findNode(Id id) {
		Node n = null;
		Id nid = null;
				
		for(int i=0; i<nodes.size(); i++) {
			n = nodes.get(i);
			nid = n.getId();
			if(nid.isEqual(id)) {
				return n;
			}
		}
		Graph g;
		for(int i=0; i<graphs.size(); i++) {
			g = graphs.get(i);
			n = g.findNode(id);
			if(n!=null) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Returns all Nodes of the graph.
	 * 
	 * If the graph contains subgraphs, you can use
	 * the thisOnly parameter to decide if nodes defined in subgraphs
	 * should be added to the list also.
	 * 
	 * @param thisOnly if true, also include nodes in subgraphs also, else exclude them
	 * @return the nodes of the graph
	 */
	public ArrayList<Node> getNodes(boolean thisOnly) {
		if(thisOnly) {
			return this.nodes;
		} else {
			ArrayList<Node> n = new ArrayList<Node>(this.nodes);
			Graph g = null;
			for(int i=0;i<this.graphs.size();i++) {
				g = graphs.get(i);
				n.addAll(g.getNodes(false));
			}
			return n;
		}
	}
	
	/**
	 * Returns all attributes of the graph.
	 * @return the attributes
	 */
	public Hashtable<String, String> getAttributes() {
		return this.attributes;
	}
	
	/**
	 * Returns all edges of this graph.
	 * @return a list of Edge objects
	 */
	public ArrayList<Edge> getEdges() {
		return this.edges;
	}
}
