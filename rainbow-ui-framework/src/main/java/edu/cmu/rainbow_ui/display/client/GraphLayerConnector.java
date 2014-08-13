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
package edu.cmu.rainbow_ui.display.client;

import edu.cmu.rainbow_ui.display.graph.Graph;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.shared.ui.Connect;
import edu.cmu.rainbow_ui.display.graph.Graph.Node;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.rainbow_ui.display.model.GraphLayer;

/**
 * Client side connector for GraphLayer Vaadin UI component.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
@Connect(GraphLayer.class)
public class GraphLayerConnector extends AbstractComponentContainerConnector {

    /**
     * RPC connector
     */
    private final GraphLayerServerRpc rpc = RpcProxy.create(
            GraphLayerServerRpc.class, this);

    /**
     * Map of known widgets
     */
    /**
     * Default constructor.
     *
     * Establishes RPC connections
     */
    public GraphLayerConnector() {
        super();

        registerRpc(GraphLayerClientRpc.class, new GraphLayerClientRpc() {
            // TODO: Override client-side RPC methods, if needed
        });

        // TODO: Implement calls to server RPC functions here
    }

    @Override
    public GraphLayerWidget getWidget() {
        return (GraphLayerWidget) super.getWidget();
    }

    @Override
    public GraphLayerState getState() {
        return (GraphLayerState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        /**
         * Update layout
         */
        getWidget().setLayoutType(getState().layout);
        
        getWidget().setNodeDistanceMultiplier(getState().nodeDistMultiplier);
        
        /**
         * Update graph
         */
        super.onStateChanged(stateChangeEvent);
        Graph graph = getState().graph;
        if (graph == null) {
            return;
        }
        Map<String, Widget> mapping = new HashMap<>();

        for (Node node : graph.nodes.values()) {
            mapping.put(node.id, ((ComponentConnector) getState().graphToWidgets.get(node.id)).getWidget());
        }

        getWidget().setGraphToWidgets(mapping);
        getWidget().setGraph(graph);
        getWidget().updateGraph();
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        //Do nothing
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
    }
}
