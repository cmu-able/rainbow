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

package com.alexmerz.graphviz;

/**
 * Error helper class to create nicer lexer/parser messages
 * 
 * @author Alexander Merz
 * @version $Id: GraphvizTokenMgrError.java,v 1.3 2006/03/20 16:45:40 Alexander Exp $
 *
 */
public class GraphvizTokenMgrError {
	
	/**
	 * Error code, if a directed edge operator was detected
	 * in a graph.
	 */
	public static final int DIRECTED_EDGE_NOT_ALLOWED = 1;

	/**
	 * Error code, if a undirected edge operator was detected
	 * in a digraph.
	 */
	public static final int UNDIRECTED_EDGE_NOT_ALLOWED = 2;
	
	/**
	 * Creates a new Token Error.
	 * 
	 * @param code The error code
	 * @param line The line where the error occured
	 * @param col The column of the line where the error occured
	 * @return an new TokenMgrError
	 */
	public static TokenMgrError create(int code, int line, int col) {
		switch(code) {
			case DIRECTED_EDGE_NOT_ALLOWED :
				return createDirectedEdgeError(line, col);
			case UNDIRECTED_EDGE_NOT_ALLOWED :
				return createUndirectedEdgeError(line, col);
			default :
				return new TokenMgrError("Unspecific error", TokenMgrError.LEXICAL_ERROR);
		}
	}
	
	private static TokenMgrError createDirectedEdgeError(int line, int col) {
		String msg = "A directed edge is not allowed in an undirected graph. Use an \"--\" instead \"->\".";
		return createError(msg, line, col);
	}
	
	private static TokenMgrError createUndirectedEdgeError(int line, int col) {
		String msg = "A undirected edge is not allowed in a directed graph. Use an \"->\" instead \"--\".";
		return createError(msg, line, col);
	}
	
	private static TokenMgrError createError(String msg, int line, int col) {
		StringBuffer sb = new StringBuffer(msg);
		sb.append(" Error at Line "+Integer.toString(line));
		sb.append(", Column "+Integer.toString(col));
		return new TokenMgrError(sb.toString(), TokenMgrError.LEXICAL_ERROR);
	}
	
}
