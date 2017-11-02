/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.cmu.rainbow_ui.display.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Data structure to store graph layouts.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class GraphLayout {

    private final Map<String, NodeLayout> nodeLayouts;
    private final Map<String, EdgeLayout> edgeLayouts;

    /**
     * Default constructor
     */
    public GraphLayout() {
        this.nodeLayouts = new HashMap<>();
        this.edgeLayouts = new HashMap<>();
    }

    /**
     * Get the layout for the given node.
     *
     * @param nodeId string id of the node
     * @return node layout
     */
    public NodeLayout getNodeLayout(String nodeId) {
        return nodeLayouts.get(nodeId);
    }

    /**
     * Get the layout for the given edge
     *
     * @param edgeId string id of the edge
     * @return edge layout
     */
    public EdgeLayout getEdgeLayout(String edgeId) {
        return edgeLayouts.get(edgeId);
    }

    /**
     * Set the layout for the given node
     * @param nodeId string id of the node
     * @param layout new layout
     */
    public void setNodeLayout(String nodeId, NodeLayout layout) {
        nodeLayouts.put(nodeId, layout);
    }

    /**
     * Set the layout for the given edge
     * @param edgeId string id of the edge
     * @param layout new layout
     */
    public void setEdgeLayout(String edgeId, EdgeLayout layout) {
        edgeLayouts.put(edgeId, layout);
    }

}
