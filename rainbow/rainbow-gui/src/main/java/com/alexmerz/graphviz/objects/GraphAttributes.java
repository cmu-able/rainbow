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
 * This class will be used in the future
 * to validate graph attributes.
 * 
 * @version $Id: GraphAttributes.java,v 1.4 2006/03/20 16:47:49 Alexander Exp $
 * @author Alexander
 *
 */
public class GraphAttributes {
	
	private String[] names = {
			"damping",
			"k",
			"url",
			"bb",
			"bgcolor",
			"center",
			"charset",
			"clusterrank",
			"colorscheme",
			"comment",
			"compound",
			"concentrate",
			"defaultdist",
			"dim",
			"dpi",
			"epsilon",
			"esep",
			"fontpath",
			"label",
			"labeljust",
			"labelloc",
			"landscape",
			"layers",
			"layersep",
			"levelsgap",
			"lp",
			"margin",
			"maxiter",
			"mclimit",
			"mindist",
			"mode",
			"model",
			"nodesep",
			"nojustify",
			"normalize",
			"nslimit",
			"nslimit1",
			"ordering",
			"orientation",
			"outputorder",
			"overlap",
			"pack",
			"packmode",
			"page",
			"pagedir",
			"pencolor",
			"quantum",
			"rankdir",
			"ranksep",
			"ratio",
			"remincross",
			"resolution",
			"root",
			"rotate",
			"samplepoints",
			"searchsize",
			"sep",
			"showboxes",
			"size",
			"splines",
			"start",
			"stylesheet",
			"target",
			"truecolor",
			"viewport",
			"voro_margin"			
	};

	public String[] getNames() {		
		return names;
	}
		
	
}
