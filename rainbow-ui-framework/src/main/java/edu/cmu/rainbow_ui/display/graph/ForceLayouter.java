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
import java.util.HashMap;
import java.util.Map;

/**
 * Layouter based on force-directed algorithm.
 *
 * <p>
 * The layouter takes into account forces acting on the nodes:
 * <ul>
 * <li> Repulsion force of other nodes. The force is based on columbus' law for electrical
 * charges</li>
 *
 * <li> Attraction force of other connected nodes. The force is based on hooke's law for elastic
 * spring </li>
 *
 * <li> Friction force. The friction force restrict movement of nodes id the total force is less
 * than friction force </li>
 *
 * <li> Viscosity force. The viscosity force limits the maximum displacement of nodes. </li>
 *
 * </ul>
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class ForceLayouter {

    /**
     * Coefficient of attractive force
     */
    private double hooke;

    /**
     * Coefficient of repulsive force
     */
    private double columbus;

    /**
     * Minimum friction force
     */
    private double friction;

    /**
     * Viscosity speed limit
     */
    private double viscosity;

    private Graph workingGraph;
    private GraphLayout workingLayout;

    /**
     * @return the hooke
     */
    public double getHooke() {
        return hooke;
    }

    /**
     * @param hooke the hooke to set
     */
    public void setHooke(double hooke) {
        this.hooke = hooke;
    }

    /**
     * @return the columbus
     */
    public double getColumbus() {
        return columbus;
    }

    /**
     * @param columbus the columbus to set
     */
    public void setColumbus(double columbus) {
        this.columbus = columbus;
    }

    /**
     * @return the friction
     */
    public double getFriction() {
        return friction;
    }

    /**
     * @param friction the friction to set
     */
    public void setFriction(double friction) {
        this.friction = friction;
    }

    /**
     * @return the viscosity
     */
    public double getViscosity() {
        return viscosity;
    }

    /**
     * @param viscosity the viscosity to set
     */
    public void setViscosity(double viscosity) {
        this.viscosity = viscosity;
    }

    /**
     * Constructor with initial forces coefficient.
     *
     * @param hooke hooke coefficient
     * @param columbus columbus coefficient
     * @param friction friction coefficient
     * @param viscosity viscosity coefficient
     */
    public ForceLayouter(double hooke, double columbus, double friction, double viscosity) {
        this.hooke = hooke;
        this.columbus = columbus;
        this.friction = friction;
        this.viscosity = viscosity;
    }

    /**
     * Create node layouts if there are some missing.
     *
     * @param graph graph to read nodes from
     * @param layout output layout
     */
    public void createNodeLayouts(Graph graph, GraphLayout layout) {
        workingGraph = graph;
        workingLayout = layout;

        for (Node node : graph.nodes.values()) {
            NodeLayout nodeLayout = layout.getNodeLayout(node.id);

            /**
             * Even if the node exists, it may has been updated with new width or height
             */
            int width = 0, height = 0;
            try {
                width = Integer.parseInt(node.properties.get("width"));
                height = Integer.parseInt(node.properties.get("height"));
            } catch (NullPointerException | NumberFormatException ex) {
                // Do nothing, leave default values
            }

            /*
             * If there is already existing node layout(eg from static information) then
             * leave its position.
             * Otherwise try to load static information.
             * Otherwise assign random position.
             */
            if (nodeLayout == null) {

                /**
                 * TODO: Make initial position more intelligent
                 */
                int x = (int) (Math.random() * 100);
                int y = (int) (Math.random() * 100);

                try {
                    x = Integer.parseInt(node.properties.get("x"));
                    y = Integer.parseInt(node.properties.get("y"));
                } catch (NullPointerException | NumberFormatException ex) {
                    // Do nothing, leave default values
                }

                nodeLayout = new NodeLayout(x, y, width, height);
                layout.setNodeLayout(node.id, nodeLayout);
            } else {
                nodeLayout.setWidth(width);
                nodeLayout.setHeight(height);
            }
        }
    }

    /**
     * Make a single layout step for the given graph and store the result in the given layout.
     *
     * <p>
     * If there were changes in node placement the method will return true, otherwise false.
     * </p>
     *
     * @param graph
     * @param layout
     * @return whether there were changes in layout
     */
    public boolean doLayoutStep(Graph graph, GraphLayout layout) {
        Map<String, SimpleVector> nodeDisplacements = new HashMap<>();

        /**
         * Compute repulsion
         */
        for (Node node : graph.nodes.values()) {
            SimpleVector displ = new SimpleVector(0, 0);
            nodeDisplacements.put(node.id, displ);

            for (Node thatNode : graph.nodes.values()) {
                if (thatNode != node) {
                    displ.add(computeRepulsion(node.id, thatNode.id));
                }
            }
        }

        /**
         * Compute attraction
         */
        for (Edge edge : graph.edges.values()) {
            String node1 = graph.ports.get(edge.sourcePort).node;
            String node2 = graph.ports.get(edge.endPort).node;
            SimpleVector displ1 = nodeDisplacements.get(node1);
            SimpleVector displ2 = nodeDisplacements.get(node2);
            SimpleVector attr = computeAttraction(node1, node2);
            displ1.add(attr);
            displ2.sub(attr);
        }

        /**
         * Apply displacements
         */
        boolean moved = false;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (Node node : graph.nodes.values()) {
            SimpleVector displ = nodeDisplacements.get(node.id);
            NodeLayout nodeLayout = layout.getNodeLayout(node.id);

            int length = displ.length();

            if (length > friction) {
                moved = true;

                if (length > viscosity) {
                    displ.div(length / viscosity);
                }

                nodeLayout.setX(nodeLayout.getX() + displ.getX());
                nodeLayout.setY(nodeLayout.getY() + displ.getY());
            }

            if (nodeLayout.getX() < minX) {
                minX = nodeLayout.getX();
            }
            if (nodeLayout.getY() < minY) {
                minY = nodeLayout.getY();
            }
        }

        /**
         * Canonize layout
         */
        for (Node node : graph.nodes.values()) {
            NodeLayout nodeLayout = layout.getNodeLayout(node.id);
            nodeLayout.setX(nodeLayout.getX() - minX);
            nodeLayout.setY(nodeLayout.getY() - minY);
        }

        /**
         * Make edges layout
         */
        for (Edge edge : graph.edges.values()) {
            NodeLayout sourceNodeLayout = layout.getNodeLayout(graph.ports.get(edge.sourcePort).node);
            int sourceX = sourceNodeLayout.getX();
            int sourceY = sourceNodeLayout.getY();
            NodeLayout endNodeLayout = layout.getNodeLayout(graph.ports.get(edge.endPort).node);
            int endX = endNodeLayout.getX();
            int endY = endNodeLayout.getY();
            layout.setEdgeLayout(edge.id, new EdgeLayout(sourceX, sourceY, endX, endY));
        }

        return moved;
    }

    /**
     * Compute attraction force between two nodes.
     *
     * <p>
     * Fa = hooke * R12 </p>
     *
     * @param thisNodeId the node with force acting on
     * @param thatNodeId the other acting node
     * @return attractive force vector
     */
    private SimpleVector computeAttraction(String thisNodeId, String thatNodeId) {
        NodeLayout nodeLayout1 = workingLayout.getNodeLayout(thisNodeId);
        NodeLayout nodeLayout2 = workingLayout.getNodeLayout(thatNodeId);

        if (nodeLayout1 == null
                | nodeLayout2 == null) {
            // Missing nodeLayouts. Should be updated.
            createNodeLayouts(workingGraph, workingLayout);
            nodeLayout1 = workingLayout.getNodeLayout(thisNodeId);
            nodeLayout2 = workingLayout.getNodeLayout(thatNodeId);
        }

        /**
         * Fa = hooke * R12
         */
        int xDispl = (int) ((nodeLayout2.getX() - nodeLayout1.getX()) * hooke);
        int yDispl = (int) ((nodeLayout2.getY() - nodeLayout1.getY()) * hooke);

        return new SimpleVector(xDispl, yDispl);
    }

    /**
     * Compute repulsion force between two nodes.
     *
     * <p>
     * Fr = - columbus * 1 / |R21|^2 * R21 / |R21| </p>
     *
     * @param thisNodeId the node with force acting on
     * @param thatNodeId the other acting node
     * @return repulsive force vector
     */
    private SimpleVector computeRepulsion(String thisNodeId, String thatNodeId) {
        NodeLayout nodeLayout1 = workingLayout.getNodeLayout(thisNodeId);
        NodeLayout nodeLayout2 = workingLayout.getNodeLayout(thatNodeId);

        if (nodeLayout1 == null
                | nodeLayout2 == null) {
            // Missing nodeLayouts. Should be updated.
            createNodeLayouts(workingGraph, workingLayout);
            nodeLayout1 = workingLayout.getNodeLayout(thisNodeId);
            nodeLayout2 = workingLayout.getNodeLayout(thatNodeId);
        }

        int xDist = nodeLayout2.getX() - nodeLayout1.getX();
        int yDist = nodeLayout2.getY() - nodeLayout1.getY();

        if ((xDist == 0) && (yDist == 0)) {
            xDist = 1;
            yDist = 1;
        }

        int dist = (int) Math.sqrt(xDist * xDist + yDist * yDist);

        /**
         * Fr = - columbus * 1 / |R21|^2 * R21 / |R21|
         */
        int xDispl = (int) (-xDist * columbus / (Math.pow(dist, 3)));
        int yDispl = (int) (-yDist * columbus / (Math.pow(dist, 3)));

        return new SimpleVector(xDispl, yDispl);
    }

    /**
     * Class to represent simple vector and operation on it.
     */
    private static class SimpleVector {

        /**
         * Coordinates
         */
        private int x;
        private int y;

        /**
         * Construct vector
         *
         * @param x the x coordinate
         * @param y the y coordinate
         */
        public SimpleVector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Add another vector to this vector.
         *
         * @param vector the vector to add
         */
        public void add(SimpleVector vector) {
            x += vector.getX();
            y += vector.getY();
        }

        /**
         * Substract another vector from this vector.
         *
         * @param vector the vector to substract
         */
        public void sub(SimpleVector vector) {
            x -= vector.getX();
            y -= vector.getY();
        }

        /**
         * Compute vector length.
         *
         * @return vector length
         */
        public int length() {
            return (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        }

        /**
         * Divide the vector by a number, ie reduce its length.
         *
         * @param d number to divide by
         */
        private void div(double d) {
            x = (int) (x / d);
            y = (int) (y / d);
        }

        /**
         * @return the x
         */
        public int getX() {
            return x;
        }

        /**
         * @return the y
         */
        public int getY() {
            return y;
        }
    }

}
