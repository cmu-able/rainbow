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

import edu.cmu.rainbow_ui.display.graph.Graph.Edge;
import edu.cmu.rainbow_ui.display.graph.Graph.Node;

/**
 * Static Layouter for the Graph.
 *
 * <p>
 * Make a layout based on statically specified properties in the graph nodes.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class StaticLayouter {

    /**
     * Do layout for a given graph and store it in the specified layout.
     *
     * @param graph graph to make layout for
     * @param layout layout to copy result in
     */
    public void doLayout(Graph graph, GraphLayout layout) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (Node node : graph.nodes.values()) {
            int x = 0, y = 0, width = 0, height = 0;
            try {
                x = Integer.parseInt(node.properties.get("x"));
                y = Integer.parseInt(node.properties.get("y"));
                width = Integer.parseInt(node.properties.get("width"));
                height = Integer.parseInt(node.properties.get("height"));
            } catch (NullPointerException | NumberFormatException ex) {
                // Do nothing, leave default values
            }
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
            layout.setNodeLayout(node.id, new NodeLayout(x, y, width, height));
        }

        /**
         * Canonize layout
         */
        for (Node node : graph.nodes.values()) {
            NodeLayout nodeLayout = layout.getNodeLayout(node.id);
            nodeLayout.setX(nodeLayout.getX() - minX);
            nodeLayout.setY(nodeLayout.getY() - minY);
        }

        for (Edge edge : graph.edges.values()) {
            NodeLayout sourceNodeLayout = layout.getNodeLayout(graph.ports.get(edge.sourcePort).node);
            int sourceX = sourceNodeLayout.getX();
            int sourceY = sourceNodeLayout.getY();
            NodeLayout endNodeLayout = layout.getNodeLayout(graph.ports.get(edge.endPort).node);
            int endX = endNodeLayout.getX();
            int endY = endNodeLayout.getY();
            layout.setEdgeLayout(edge.id, new EdgeLayout(sourceX, sourceY, endX, endY));
        }
    }
}
