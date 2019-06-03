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

/**
 * This object is used to identify graphs and nodes
 * Note, that an object may contain an id or a label.   
 * 
 * @author Alexander Merz
 * @version $Id: Id.java,v 1.3 2006/03/20 16:45:41 Alexander Exp $
 */

public class Id {

	/**
	 * The id
	 */
	private String id = "";
	
	/**
	 * The label
	 */
	private String label = "";
	
	/**
	 * Returns the Id of the object
	 * Empty string means, that this object has no Id.
	 * @return the object id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the object id.
	 * An empty string deletes the Id.
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the label of the object
	 * @return the object label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label of the object 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}	
	
	/**
	 * Returns the String representation of this Id
	 * @return the string representation
	 */
	public String toString() {
		return "(id="+id+", label="+label+")";
	}
	
	/**
	 * Compares the Ids.
	 * 
	 * Two Id objects are equal, if
	 * <ul>
	 *  <li>both are the same, or</li>
	 *  <li>Id and Label attribute are equal</li>
	 *  <li>both ids are empty and the labels are equal</li>	 
	 * </ul>
	 * 
	 * @param eid
	 * @return true if both Ids are equal
	 */
	public boolean isEqual(Id eid) {
		if(eid==this) {
			return true;
		} else {
			// Id and label are the same
			if(eid.getId().equals(id) && eid.getLabel().equals(label)) {
				return true;
			// both ids are empty, but labels are the same	
			} else if(eid.getId().equals("") && id.equals("") && eid.getLabel().equals(label)) {
				return true;				
			// both ids are empty, and label differs	
			} else if(eid.getId().equals("") && id.equals("") && !eid.getLabel().equals(label)) {
				return false;
			}
			return false;
		}
	}
}
