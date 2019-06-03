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
 * This class represents a node in a graph.
 * 
 * @author Alexander Merz
 * @version $Id: Node.java,v 1.4 2006/04/05 22:39:30 Alexander Exp $
 *
 */
public class Node {

	/**
	 * The id object
	 */
	private Id id = null;
	
	/**
	 * Holds the attributes of the node
	 */
	private Hashtable<String, String> attr = new Hashtable<String, String>(); 
	
	/**
	 * If true this node represents a subgraph
	 */
	private boolean isSubgraph = false;
	
	/**
	 * Returns the id object for the node
	 * @return the Id object
	 */
	public Id getId() {
		return id;
	}
	
	/**
	 * Sets the Id object for this node
	 * @param id
	 */
	public void setId(Id id) {
		this.id = id;
	}

	/**
	 * Returns a string representation of this node
	 * If the node is a subgraph, an empty string will be returned
	 * @return the string representation
	 */
	public String toString() {
		if(isSubgraph()) {
			return "";
		}
		StringBuffer r = new StringBuffer("");
		if(!id.getId().equals("")) {
			r.append(id.getId());
		} else if(!id.getLabel().equals("")) {
			r.append("\"");
			r.append(id.getLabel());
			r.append("\"");
		}		
		if(attr.size()>0) {
			r.append(" [");
			Enumeration<String> e = attr.keys();
			while(e.hasMoreElements()) {
				String k = e.nextElement();
				r.append(k);
				if(!attr.get(k).equals("")) {
					r.append("=");
					if(-1 == attr.get(k).indexOf(" ")) {
						r.append(attr.get(k));
					} else {
						r.append("\"");
						r.append(attr.get(k));
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
	 * Returns the attribute of the node
	 * @param key the name of the attribute
	 * @return the value of the attribute
	 */
	public String getAttribute(String key) {
		return attr.get(key);
	}

	/**
	 * Sets the attribute of the node
	 * @param key the name of the attribute
	 * @param value the value of the attribute
	 */
	public void setAttribute(String key, String value) {
		this.attr.put(key, value);
	}

	/**
	 * Returns true, if the node object represents 
	 * a subgraph.
	 * @return true or false
	 */
	public boolean isSubgraph() {
		return isSubgraph;
	}

	/**
	 * Sets, if the node represents a subgraph
	 * @param isSubgraph true if subgraph is used in
	 *        edge operation
	 */
	public void representsSubgraph(boolean isSubgraph) {
		this.isSubgraph = isSubgraph;
	}
	
	/**
	 * Returns all attributes of the edge.
	 * @return the edge attributes.
	 */
	public Hashtable<String,String> getAttributes() {
		return this.attr;
	}
}
