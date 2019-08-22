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


package com.alexmerz.graphviz.objects;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class represents an Edge in a Graph.
 * 
 * Each Edge has a source Node and a target Node.
 * Both nodes may contain port information.
 * 
 * The type attribute is currently a nice-to-have
 * because only directed edges are only allowed
 * in 'Digraphs', and undirected in 'Graphs'.
 * 
 * @author Alexander Merz
 * @version $Id: Edge.java,v 1.4 2006/04/05 22:39:16 Alexander Exp $
 *
 */
public class Edge {

	/**
	 * The source node object
	 */
	private PortNode source = null;
	
	/**
	 * The target node object
	 */
	private PortNode target = null;
	
	/** 
	 * The type of the edge (directed or undirected)
	 * @see Graph.EDGE_DIRECTED
	 * @see Graph.EDGE_UNDIRECTED
	 */
	private int type = 0;
	
	/**
	 * The hasttable having the attributes of this edge
	 */
	private Hashtable<String, String>attributes = new Hashtable<String, String>();
	
	/**
	 * Creates an empty edge object
	 *
	 */
	public Edge() {
		source = null;
		target = null;
		type = 0;		
	}
	
	/**
	 * Creates an Edge 
	 * 
	 * @param source the source Node
	 * @param target the target Node
	 * @param type the edge type
	 */
	public Edge(PortNode source, PortNode target, int type) {
		this.source = source;
		this.target = target;
		this.type = type;
	}
	
	/**
	 * Returns the source node of the edge
	 * @return the source node
	 */
	public PortNode getSource() {
		return source;
	}
	
	/**
	 * Sets the source node of the edge
	 * @param source the source node
	 */
	public void setSource(PortNode source) {
		this.source = source;
	}
	
	/**
	 * Returns the target node of the edge
	 * @return the target node
	 */
	public PortNode getTarget() {
		return target;
	}
	
	/**
	 * Sets the target node of the edge
	 * @param target the target Node
	 */
	public void setTarget(PortNode target) {
		this.target = target;
	}
	
	/**
	 * Returns the type of the edge.
	 * @return the type of the edge
	 * @see com.alexmerz.graphviz.objects.Graph#UNDIRECTED
	 * @see com.alexmerz.graphviz.objects.Graph#DIRECTED
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Sets the type of the edge
	 * @param type the type of the edge
	 * @see com.alexmerz.graphviz.objects.Graph#UNDIRECTED
	 * @see com.alexmerz.graphviz.objects.Graph#DIRECTED	 
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * Returns the String representation of the edge
	 * @return the string representation
	 */
	public String toString() {
		StringBuffer r = new StringBuffer();
		if(!source.getNode().getId().getId().equals("")) {
			r.append(source.getNode().getId().getId());
		} else if(!source.getNode().getId().getLabel().equals("")) {
			r.append("\"");
			r.append(source.getNode().getId().getLabel());
			r.append("\"");
		}
		if(!source.getPort().equals("")) {
			r.append(":\"");
			r.append(source.getPort());
			r.append("\"");
		}
		if(Graph.DIRECTED == getType()) {
			r.append(" -> ");
		} else {
			r.append(" -- ");
		}
		if(!target.getNode().getId().getId().equals("")) {
			r.append(target.getNode().getId().getId());
		} else if(!target.getNode().getId().getLabel().equals("")) {
			r.append("\"");
			r.append(target.getNode().getId().getLabel());
			r.append("\"");
		}
		if(!target.getPort().equals("")) {
			r.append(":\"");
			r.append(target.getPort());
			r.append("\"");
		}		
		if(attributes.size()>0) {
			r.append(" [");
			Enumeration<String> e = attributes.keys();
			while(e.hasMoreElements()) {
				String k = e.nextElement();
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
				r.append(", ");
			}
			r.delete(r.length()-2, r.length());
			r.append("]");
		}		
		r.append(";\n");		
		return r.toString();
	}

	/**
	 * Returns the value of an edge attribute
	 * @param key the name of the attribute
	 * @return the value of the attribute
	 */
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * Sets the value of an attribute
	 * @param key the name of the attribute
	 * @param value the value of the attribute
	 */
	public void setAttribute(String key, String value) {	
		this.attributes.put(key, value);
	}
	
	/**
	 * Returns all attributes of this edge
	 * @return the attributes
	 */
	public Hashtable<String,String> getAttributes() {
		return this.attributes;
	}
	
}
