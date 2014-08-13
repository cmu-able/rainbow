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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Graph structure to communicate from a server to a client.
 *
 * <p>
 * All fields should be serializable and have getters/setter or be public.
 * All classes should have default constructor.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class Graph implements Serializable {

    public String id;
    public Map<String, Node> nodes = new HashMap<>();
    public Map<String, Edge> edges = new HashMap<>();
    public Map<String, Port> ports = new HashMap<>();

    public Graph() {
    }

    /**
     * Graph copy constructor
     * @param graph graph to copy
     */
    public Graph(Graph graph) {
        this();
        this.id = graph.id;
        for (String nodeId : graph.nodes.keySet()) {
            this.nodes.put(nodeId, new Node(graph.nodes.get(nodeId)));
        }
        for (String edgeId : graph.edges.keySet()) {
            this.edges.put(edgeId, new Edge(graph.edges.get(edgeId)));
        }
        for (String portId : graph.ports.keySet()) {
            this.ports.put(portId, new Port(graph.ports.get(portId)));
        }
    }

    /**
     * Node class
     */
    public static class Node implements Serializable {

        public String id;
        public List<String> ports = new LinkedList<>();
        public Map<String, String> properties = new HashMap<>();

        public Node() {
        }

        /**
         * Node copy constructor
         * @param node node to copy
         */
        public Node(Node node) {
            this.id = node.id;
            this.ports = node.ports;
            this.properties = new HashMap<>(node.properties);
        }

    }

    /**
     * Edge class
     */
    public static class Edge implements Serializable {

        public String id;
        public boolean ordered = false;
        public String sourcePort;
        public String endPort;
        public Map<String, String> properties = new HashMap<>();

        public Edge() {
        }

        public Edge(Edge edge) {
            this.sourcePort = edge.sourcePort;
            this.endPort = edge.endPort;
            this.id = edge.id;
            this.ordered = edge.ordered;
        }
    }

    /**
     * Port class
     */
    public static class Port implements Serializable {

        public String id;
        public String node;
        public String edge;

        public Port() {
        }

        /**
         * Port copy constructor
         * @param port port to copy
         */
        public Port(Port port) {
            this.id = port.id;
            this.node = port.node;
            this.edge = port.edge;
        }
    }
}
