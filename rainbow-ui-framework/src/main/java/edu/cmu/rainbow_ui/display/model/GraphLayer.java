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
package edu.cmu.rainbow_ui.display.model;

import com.google.gwt.layout.client.Layout;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import edu.cmu.rainbow_ui.display.graph.Graph;
import edu.cmu.rainbow_ui.display.client.GraphLayerConnector;
import edu.cmu.rainbow_ui.display.client.GraphLayerServerRpc;
import edu.cmu.rainbow_ui.display.client.GraphLayerState;
import edu.cmu.rainbow_ui.display.client.GraphLayerWidget;
import edu.cmu.rainbow_ui.display.graph.LayoutType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Vaadin UI component responsible for drawing of graphs.
 *
 * <p>
 * This is a server-side part of a custom Vaadin widget. It maintains server-side functions and
 * state and communicates it to client-side GWT implementation via shared state and connectors.
 *
 * @see GraphLayerWidget
 * @see GraphLayerState
 * @see GraphLayerConnector
 * </p>
 * <p>
 * Server-side component is able to communicate both ways with the client-side widget via RPC
 * connection.
 * @see GraphLayerServerRpc
 * @see GraphLayerClientRpc
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class GraphLayer extends AbstractComponentContainer {

    List<Component> children = new ArrayList<>();

    /**
     * RPC handler to do client-server communication
     */
    private final GraphLayerServerRpc rpc = new GraphLayerServerRpc() {
    };

    /**
     * Default constructor
     */
    public GraphLayer() {
        super();
        registerRpc(rpc);
    }

    @Override
    public GraphLayerState getState() {
        return (GraphLayerState) super.getState();
    }

    /**
     * {@inheritDoc}
     *
     * Adding component doesn't trigger state update. It is updated later on graph update.
     */
    @Override
    public void addComponent(Component c) {
        children.add(c);
        super.addComponent(c);
    }

    /**
     * Add component to the layout and associate it with a graph element by id.
     *
     * @param c component
     * @param id id of the graph element
     */
    public void addComponent(Component c, String id) {
        this.addComponent(c);
        this.getState().graphToWidgets.put(id, c);
    }
    
    /**
     * Return an UI component for a given graph element
     * 
     * @param id string id of the graph element
     * @return component or null if element has no UI component
     */
    public Component getGraphElementComponent(String id) {
        return (Component)this.getState().graphToWidgets.get(id);
    }

    /**
     * {@inheritDoc}
     *
     * Removing component doesn't trigger state update. It is updated later on graph update.
     */
    @Override
    public void removeComponent(Component c) {
        children.remove(c);
        super.removeComponent(c);
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        int index = children.indexOf(oldComponent);
        if (index != -1) {
            children.remove(index);
            children.add(index, newComponent);
            fireComponentDetachEvent(oldComponent);
            fireComponentAttachEvent(newComponent);
            /* Replacing component means updated state */
            markAsDirty();
        }
    }

    @Override
    public int getComponentCount() {
        return children.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return children.iterator();
    }

    /**
     * Set the graph.
     * @param graph new graph
     */
    public void setGraph(Graph graph) {
        getState().graph = graph;
        /* We mark the state as updated on graph structure updates */
        markAsDirty();
    }
    
    /**
     * Return the graph
     * @return current graph
     */
    public Graph getGraph() {
        return getState().graph;
    }
    
    /**
     * Set the layout type of the graph.
     * 
     * @param type new layout type
     */
    public void setLayoutType(LayoutType type) {
        getState().layout = type;
    }
    
    /**
     * Get the layout type of the graph.
     * 
     * @return layout type
     */
    public LayoutType getLayoutType() {
        return getState().layout;
    }
    
    /**
     * Force the state to be pushed to the client.
     */
    public void forceUpdate() {
        markAsDirty();
    }
    
    /**
     * Set node distance multiplier.
     * 
     * @param m distance multiplier
     */
    public void setNodeDistMultiplier(float m) {
        this.getState().nodeDistMultiplier = m;
    }
    
    /**
     * Get node distance multiplier.
     * 
     * @return distance multiplier
     */
    public float getNodeDistMultiplier() {
        return this.getState().nodeDistMultiplier;
    }
}
