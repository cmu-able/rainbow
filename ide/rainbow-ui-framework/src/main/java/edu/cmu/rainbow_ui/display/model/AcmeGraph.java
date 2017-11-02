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

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.cmu.rainbow_ui.display.config.ElementConfiguration;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.graph.Graph;
import edu.cmu.rainbow_ui.display.graph.Graph.Edge;
import edu.cmu.rainbow_ui.display.graph.Graph.Node;
import edu.cmu.rainbow_ui.display.graph.Graph.Port;
import edu.cmu.rainbow_ui.display.graph.LayoutType;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeAttachment;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.representation.IAcmeRepresentation;
import org.acmestudio.acme.element.representation.IAcmeRepresentationBearer;
import org.vaadin.risto.stepper.FloatStepper;

/**
 * Acme Graph Vaadin UI component.
 *
 * <p>
 * The Acme Graph is responsible for drawing of the graph and placement of interactive components on
 * top of it.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeGraph extends AcmeModelView {

    /**
     * Scrollable panel for graph
     */
    private final Panel panel;

    /**
     * Layer responsible for graph drawing
     */
    private final GraphLayer graphLayer;

    /**
     * Root system ID for the currently displayed part of the model. The graph will show only
     * component that are in the displayed system.
     */
    private IAcmeSystem currentSystem;

    /**
     * Bread crumbs component to show currently displayed system. This is a horizontal layout of
     * buttons and a combobox to display a hierarchy of displayed systems.
     */
    private final HorizontalLayout breadCrumbs;

    /**
     * View control to create widgets.
     */
    private final ViewControl viewControl;
    /**
     * View configuration to customize graph look.
     */
    private final ViewConfiguration viewConfig;

    /**
     * CSS Stylenames
     */
    static final String GRAPH_WIDGET_STYLE = "graph-widget";
    static final String GRAPH_WIDGET_HIGHLIGHTED_STYLE = "highlighted";

    /**
     * Vaadin UI to show subwindows
     */
    private final AbstractRainbowVaadinUI ui;

    /**
     * Constructor that creates default layer structure.
     *
     * <p>
     * The constructor requires SystemViewProvider, ViewControl and ViewConfiguration to be
     * initialized prior to component creation.
     * </p>
     *
     * @param ui reference to the Rainbow UI
     */
    public AcmeGraph(AbstractRainbowVaadinUI ui) {
        super(ui.getSystemViewProvider());
        this.ui = ui;
        viewControl = ui.getViewControl();
        viewConfig = ui.getViewConfiguraion();
        graphLayer = new GraphLayer();
        graphLayer.setSizeUndefined();
        graphLayer.setLayoutType(LayoutType.STATIC);

        VerticalLayout alignWrapper = new VerticalLayout(graphLayer);
        alignWrapper.setComponentAlignment(graphLayer, Alignment.MIDDLE_CENTER);
        alignWrapper.setSizeFull();

        HorizontalLayout controlBar = new HorizontalLayout();
        controlBar.setSpacing(true);

        /* Create force layout switch */
        final CheckBox forceLayoutCheck = new CheckBox("Force Layout");
        forceLayoutCheck.setValue(false);
        forceLayoutCheck.setDescription("Add force layouting");
        forceLayoutCheck.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (forceLayoutCheck.getValue()) {
                    graphLayer.setLayoutType(LayoutType.FORCE);
                } else {
                    graphLayer.setLayoutType(LayoutType.STATIC);
                }
            }
        });
        controlBar.addComponent(forceLayoutCheck);

        /* Create node distance multiplier stepper */
        final FloatStepper distStepper = new FloatStepper();
        distStepper.setValue((float) 1.0);
        distStepper.setImmediate(true);
        distStepper.setMaxValue((float) 10.0);
        distStepper.setMinValue((float) 0.1);
        distStepper.setStepAmount((float) 0.1);
        distStepper.setNumberOfDecimals(3);
        distStepper.setManualInputAllowed(true);
        distStepper.setWidth(50, Unit.PIXELS);
        distStepper.setDescription("Node distance multiplier");

        distStepper.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                graphLayer.setNodeDistMultiplier(distStepper.getValue());
            }
        });

        controlBar.addComponent(distStepper);

        /* Create a search control for the graph */
        TextField searchField = new TextField();
        searchField.setValue("");
        searchField.setInputPrompt("Search");
        searchField.setDescription("Search by element name");
        searchField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String searchString = (String) event.getProperty().getValue();
                AcmeGraph.this.resetNodesHighlight();
                if (!searchString.equals("")) {
                    AcmeGraph.this.highlightMathcingNodes(searchString);
                }
            }
        });
        controlBar.addComponent(searchField);

        /* Breadcrumbs */
        breadCrumbs = new HorizontalLayout();
        breadCrumbs.addStyleName("breadcrumbs");
        breadCrumbs.setSpacing(true);
        controlBar.addComponentAsFirst(breadCrumbs);
        controlBar.setExpandRatio(breadCrumbs, 1.0f);
        controlBar.setComponentAlignment(breadCrumbs, Alignment.MIDDLE_LEFT);

        controlBar.setComponentAlignment(searchField, Alignment.MIDDLE_RIGHT);
        controlBar.setWidth(100, Unit.PERCENTAGE);

        panel = new Panel(alignWrapper);
        panel.setSizeFull();

        VerticalLayout vLayout = new VerticalLayout();
        vLayout.addComponent(controlBar);
        vLayout.addComponent(panel);
        vLayout.setExpandRatio(panel, 1.0f);
        vLayout.setSizeFull();

        this.setCompositionRoot(vLayout);
        this.setHeight("100%");
        /* Set root by default to the model level */
        this.setCurrentSystem((IAcmeSystem) systemViewProvider.getView().getModelInstance());
    }

    @Override
    protected void subUpdate() {
        /**
         * The model may have been updated. Lookup for the system in the new model.
         */
        IAcmeSystem newSystem = (IAcmeSystem) ModelHelper
                .getElementFromQualifiedName(model, currentSystem.getQualifiedName());
        if (newSystem != null) {
            currentSystem = newSystem;
        } else {
            /* The system doesn't exist anymore. Reset to top-level system */
            currentSystem = model;
            Logger.getLogger(AcmeGraph.class.getName()).log(Level.INFO,
                    "The system {0} doesn''t exists. "
                    + "Switch to top-level system.",
                    currentSystem.getQualifiedName());
        }
        
        /**
         * TODO: this approach will eventually lead to memory leaks.
         * The previous graph should be disposed appropriately with destroy of
         * all elements and their internal widgets.
         */
        Graph graph = createGraph();
        graphLayer.setGraph(graph);
    }

    /**
     * Create the graph from the scratch based on the Acme model.
     *
     * @return created graph
     */
    private Graph createGraph() {
        /**
         * TODO: implement graph creation based on the root component selection.
         */
        Graph graph = new Graph();

        /**
         * Create components
         */
        for (IAcmeComponent component : currentSystem.getComponents()) {
            Node node = new Node();
            node.id = component.getQualifiedName();

            /**
             * Read element properties from the model metadata.
             */
            fillVisInformationFromModelMetadata(node, component);

            /**
             * Read element properties from the view configuration. This will override conflicting
             * information obtained with the model.
             */
            fillVisInformationFromViewConfig(node, component);

            Component widgetComponent = createWidgetForAcmeComponent(component);
            node.properties.put("width", (int) widgetComponent.getWidth() + "");
            node.properties.put("height", (int) widgetComponent.getHeight() + "");

            for (IAcmePort port : component.getPorts()) {
                Port graphPort = new Port();
                graphPort.id = port.getQualifiedName();
                graphPort.node = node.id;

                graph.ports.put(graphPort.id, graphPort);
                node.ports.add(graphPort.id);
            }

            graph.nodes.put(node.id, node);
            graphLayer.addComponent(widgetComponent, node.id);
        }

        /**
         * Create connectors. From the graph viewpoint they are just nodes.
         */
        for (IAcmeConnector connector : currentSystem.getConnectors()) {
            Node node = new Node();
            node.id = connector.getQualifiedName();

            /**
             * Read element properties from the model metadata.
             */
            fillVisInformationFromModelMetadata(node, connector);

            /**
             * Read element properties from the view configuration. This will override conflicting
             * information obtained with the model.
             */
            fillVisInformationFromViewConfig(node, connector);

            Component widgetComponent = createWidgetForAcmeConnector(connector);
            node.properties.put("width", (int) widgetComponent.getWidth() + "");
            node.properties.put("height", (int) widgetComponent.getHeight() + "");

            for (IAcmeRole role : connector.getRoles()) {
                Port graphPort = new Port();
                graphPort.id = role.getQualifiedName();
                graphPort.node = node.id;

                graph.ports.put(graphPort.id, graphPort);
                node.ports.add(graphPort.id);
            }

            graph.nodes.put(node.id, node);
            graphLayer.addComponent(widgetComponent, node.id);
        }

        /**
         * Create attachments, ie edges.
         */
        for (IAcmeAttachment attachment : currentSystem.getAttachments()) {
            Edge edge = new Edge();
            edge.id = attachment.getQualifiedName();
            edge.ordered = false;
            edge.sourcePort = attachment.getPort().getQualifiedName();
            edge.endPort = attachment.getRole().getQualifiedName();
            /* Add edge to ports */
            graph.ports.get(edge.sourcePort).edge = edge.id;
            graph.ports.get(edge.endPort).edge = edge.id;
            /* Add edge to the list */
            graph.edges.put(edge.id, edge);
        }
        return graph;
    }

    /**
     * Read element properties from the model metadata to the node properties. Note: this will
     * override any values for existing keys.
     *
     * @param node graph node
     * @param element acme element
     */
    private void fillVisInformationFromModelMetadata(Node node, IAcmeElement element) {
        /**
         * TODO: implement
         */
    }

    /**
     * Read element properties from the view configuration to the node properties. Note: this will
     * override any values for existing keys.
     *
     * @param node graph node
     * @param element acme element
     */
    private void fillVisInformationFromViewConfig(Node node, IAcmeElement element) {
        ElementConfiguration elemConfig = viewConfig.graph.elements.get(element.getQualifiedName());

        if (elemConfig != null) {
            for (String key : elemConfig.visinformation.keySet()) {
                node.properties.put(key, elemConfig.visinformation.get(key));
            }
        }
    }

    /**
     * Constraints on widget sizes. In order to make predictable layouts widgets sized should be
     * fixed.
     */
    /**
     * Construct an interactive widget for an Acme component
     *
     * @param component Acme component
     * @return created widget as Vaadin component
     */
    private Component createWidgetForAcmeComponent(final IAcmeComponent component) {
        return new AcmeComponentNode(component, ui, this);
    }

    /**
     * Construct an interactive widget for an Acme connector
     *
     * @param component Acme connector
     * @return created widget as Vaadin component
     */
    private Component createWidgetForAcmeConnector(IAcmeConnector connector) {
        return new AcmeConnectorNode(connector, ui, this);
    }

    /**
     * Get the current root system.
     *
     * @return root system
     */
    public IAcmeSystem getCurrentSystem() {
        return currentSystem;
    }

    /**
     * Set the current root system.
     *
     * @param system the current system
     */
    public void setCurrentSystem(IAcmeSystem system) {
        this.currentSystem = system;
        /**
         * Update the breadcrumbs component.
         */
        breadCrumbs.removeAllComponents();

        /**
         * If current system is an alternative representation of its parent then show it as a
         * combobox.
         */
        if ((currentSystem.getParent() != null)
                && (currentSystem.getParent().getParent() != null)
                && (currentSystem.getParent().getParent() instanceof IAcmeRepresentationBearer)) {
            final IAcmeRepresentationBearer parentComp = (IAcmeRepresentationBearer) currentSystem.getParent().getParent();
            Set<? extends IAcmeRepresentation> reprs = parentComp.getRepresentations();
            if (reprs.size() > 1) {
                final ComboBox reprSelector = new ComboBox();
                reprSelector.setNullSelectionAllowed(false);
                for (IAcmeRepresentation repr : reprs) {
                    reprSelector.addItem(repr.getSystem().getName());
                }
                /* Current system is the default selection */
                reprSelector.setValue(currentSystem.getName());
                reprSelector.addValueChangeListener(new Property.ValueChangeListener() {

                    @Override
                    public void valueChange(Property.ValueChangeEvent event) {
                        AcmeGraph.this.setCurrentSystem(
                                parentComp.getRepresentation(reprSelector.getValue().toString()).getSystem()
                        );
                    }
                });
                breadCrumbs.addComponent(reprSelector);
                breadCrumbs.setComponentAlignment(reprSelector, Alignment.MIDDLE_LEFT);
            } else {
                Label sysLabel = new Label(currentSystem.getName());
                breadCrumbs.addComponent(sysLabel);
                breadCrumbs.setComponentAlignment(sysLabel, Alignment.MIDDLE_LEFT);
            }
        } else {
            /* Top-level */
            Label sysLabel = new Label(currentSystem.getName());
            breadCrumbs.addComponent(sysLabel);
            breadCrumbs.setComponentAlignment(sysLabel, Alignment.MIDDLE_LEFT);
        }

        IAcmeElement elem = currentSystem.getParent();
        while (elem != null) {
            final IAcmeElement curElem = elem;
            if (elem instanceof IAcmeSystem) {
                Button sysButton = new Button(elem.getName());
                sysButton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        AcmeGraph.this.setCurrentSystem((IAcmeSystem) curElem);
                    }
                });

                breadCrumbs.addComponentAsFirst(sysButton);
                breadCrumbs.setComponentAlignment(sysButton, Alignment.MIDDLE_LEFT);
            } else if ((elem instanceof IAcmeComponent)
                    || (elem instanceof IAcmeConnector)) {

                /**
                 * If it is currently viewed component then display it as a label. Since current
                 * element is a component the current system should always has a parent component.
                 */
                if (currentSystem.getParent().getParent() == elem) {
                    Label compLabel = new Label(elem.getName());
                    breadCrumbs.addComponentAsFirst(compLabel);
                    breadCrumbs.setComponentAlignment(compLabel, Alignment.MIDDLE_LEFT);
                } else {
                    Button compButton = new Button(elem.getName());
                    compButton.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            Set<? extends IAcmeRepresentation> reprs = ((IAcmeRepresentationBearer) curElem)
                                    .getRepresentations();
                            /* If the component is on the hierarchy then it should have representations */
                            /**
                             * TODO: modify to select by default previously selected representation.
                             */
                            if (reprs.size() > 0) {
                                AcmeGraph.this.setCurrentSystem(((IAcmeRepresentation) reprs.iterator().next()).getSystem());
                            }
                        }
                    });
                    breadCrumbs.addComponentAsFirst(compButton);
                    breadCrumbs.setComponentAlignment(compButton, Alignment.MIDDLE_LEFT);
                }
            } else {
                // Skip other element types
            }

            /* Top-level returns itself as a parent */
            elem = elem.getParent();
        }
        /**
         * Redraw the whole graph
         */
        this.subUpdate();
    }

    /**
     * Get the graphLayer component. Package scoped for testing purposes.
     *
     * @return graph layer component
     */
    GraphLayer getGraphLayer() {
        return graphLayer;
    }

    /**
     * Reset the highlighting of graph nodes
     */
    private void resetNodesHighlight() {
        for (Node node : graphLayer.getGraph().nodes.values()) {
            Component component = graphLayer.getGraphElementComponent(node.id);
            if (component != null) {
                if (component.getStyleName().contains(GRAPH_WIDGET_HIGHLIGHTED_STYLE)) {
                    component.removeStyleName(GRAPH_WIDGET_HIGHLIGHTED_STYLE);
                }
            }
        }
    }

    /**
     * Highlight nodes with ID matching a given string.
     *
     * <p>
     * Performs a search over the graph nodes and finds whether an ID of the node contains a given
     * match string. Comparison is case-insensitive.
     * </p>
     *
     * <p>
     * Note: may be slow on big graphs.
     * </p>
     *
     * @param matchStr string to match
     */
    private void highlightMathcingNodes(String matchStr) {
        for (Node node : graphLayer.getGraph().nodes.values()) {
            if (node.id.toLowerCase().contains(matchStr.toLowerCase())) {
                Component component = graphLayer.getGraphElementComponent(node.id);
                if (component != null) {
                    if (!component.getStyleName().contains(GRAPH_WIDGET_HIGHLIGHTED_STYLE)) {
                        component.addStyleName(GRAPH_WIDGET_HIGHLIGHTED_STYLE);
                    }
                }
            }
        }
    }

    /**
     * Update size for the existing node and communicate it to the client.
     */
    void updateNodeWidthAndHeight(String nodeId) {
        Node node = graphLayer.getGraph().nodes.get(nodeId);

        if (node == null) {
            Logger.getLogger(AcmeGraph.class.getName()).warning("Cannot update size of non-exisiting node: " + nodeId);
        } else {
            Component comp = graphLayer.getGraphElementComponent(nodeId);
            node.properties.put("width", (int) comp.getWidth() + "");
            node.properties.put("height", (int) comp.getHeight() + "");

            /* Update the graph in the state */
            graphLayer.forceUpdate();
        }
    }
    
    /**
     * Reset the graph. Make it from the scratch.
     */
    public void resetGraph() {
        this.subUpdate();
    }
}
