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

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import edu.cmu.rainbow_ui.display.graph.EdgeLayout;
import edu.cmu.rainbow_ui.display.graph.ForceLayouter;
import edu.cmu.rainbow_ui.display.graph.Graph;
import edu.cmu.rainbow_ui.display.graph.Graph.Edge;
import edu.cmu.rainbow_ui.display.graph.Graph.Node;
import edu.cmu.rainbow_ui.display.graph.GraphLayout;
import edu.cmu.rainbow_ui.display.graph.LayoutType;
import edu.cmu.rainbow_ui.display.graph.NodeLayout;
import edu.cmu.rainbow_ui.display.graph.StaticLayouter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Line;

/**
 * Client-side widgetId for GraphLayer Vaadin UI component.
 *
 * <p>
 * The widget is based on GWT graphics and is able to display graphs using simple graphical
 * primitives.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class GraphLayerWidget extends Composite {

    /**
     * GWT widgets
     */
    public static final String CLASSNAME = "graphlayer";
    private final AbsolutePanel panel;
    private final DrawingArea graphCanvas;
    private final AbsolutePanel widgetsCanvas;

    /**
     * Graph structure
     */
    private Graph graph;
    private Map<String, Widget> graphToWidget;

    /**
     * Graph layouters
     */
    private final GraphLayout layout;
    private final StaticLayouter staticLayouter;
    private final ForceLayouter forceLayouter;
    private LayoutType layoutType;
    private final Timer forceLayouterTimer;
    private final static int TIMER_DELAY = 40;
    
    /**
     * Node distance multiplier. Increases the coordinate grid proportional to this
     * number.
     */
    private float nodeDistanceMultiplier = (float)1.0;

    /**
     * Graph canvas margins in pixels
     */
    public static final int GRAPH_MARGINS = 50;

    /**
     * Default constructor. Create the graphical component and init data structures.
     */
    public GraphLayerWidget() {
        /**
         * Init data structures
         */
        super();
        graphToWidget = new HashMap<>();
        /**
         * Init layouters
         */
        layout = new GraphLayout();
        staticLayouter = new StaticLayouter();
        forceLayouter = new ForceLayouter(0.001, 50000, 2, 7);
        forceLayouterTimer = new Timer() {

            @Override
            public void run() {
                boolean cont = forceLayouter.doLayoutStep(graph, layout);
                drawGraph();
                if (cont) {
                    this.schedule(TIMER_DELAY);
                }
            }
        };
        /**
         * Init graphical components
         */
        panel = new AbsolutePanel();
        panel.getElement().getStyle().setMargin(GRAPH_MARGINS, Style.Unit.PX);
        graphCanvas = new DrawingArea(0, 0);
        widgetsCanvas = new AbsolutePanel();
        widgetsCanvas.getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        graphCanvas.getElement().getStyle().setZIndex(0);
        widgetsCanvas.getElement().getStyle().setZIndex(1);
        panel.add(graphCanvas, 0, 0);
        panel.add(widgetsCanvas, 0, 0);
        initWidget(panel);
        this.getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        setCanvasesSize(1000, 1000);
        setStyleName(CLASSNAME);
    }

    /**
     * Set the graph canvas.
     *
     * @param w width
     * @param h height
     */
    public void setCanvasesSize(int w, int h) {
        panel.setSize(w + "px", h + "px");
        widgetsCanvas.setSize(w + "px", h + "px");
        graphCanvas.setSize(w + "px", h + "px");
    }

    /**
     * Update the graph reference.
     *
     * @param newGraph new graph
     */
    public void setGraph(Graph newGraph) {
        this.graph = newGraph;
    }

    /**
     * Set the layout type.
     *
     * @param layoutType the type of layout
     */
    public void setLayoutType(LayoutType layoutType) {
        this.layoutType = layoutType;
    }

    /**
     * Set mapping from graph elements to widgets.
     *
     * @param mapping mapping from element ID to widget
     */
    public void setGraphToWidgets(Map<String, Widget> mapping) {
        graphToWidget = mapping;
    }
    
    /**
     * Set node distance multiplier.
     * 
     * @param m multiplier
     */
    public void setNodeDistanceMultiplier(float m) {
        nodeDistanceMultiplier = m;
    }

    /**
     * Update the graph, ie run the layouter and graph drawing.
     */
    public void updateGraph() {
        /**
         * First do graph layout.
         */
        switch (layoutType) {
            case STATIC:
                staticLayouter.doLayout(graph, layout);
                drawGraph();
                break;
            case FORCE:
                forceLayouterTimer.cancel();
                forceLayouter.createNodeLayouts(graph, layout);
                forceLayouter.doLayoutStep(graph, layout);
                drawGraph();
                forceLayouterTimer.schedule(TIMER_DELAY);
                break;
            default:
                // This should never happen.
                Logger logger = Logger.getLogger("GraphWidgetLogger");
                logger.log(Level.SEVERE, "Unsupported type of graph layout. Giving up graph drawing");
        }

    }

    /**
     * Redraw the graph from scratch.
     */
    private void drawGraph() {
        widgetsCanvas.clear();
        graphCanvas.clear();

        /* Determine canvas boundaries */
        int maxX = 0;
        int maxY = 0;

        /* Draw widgets */
        for (Node node : graph.nodes.values()) {
            Widget widget = graphToWidget.get(node.id);
            widgetsCanvas.add(widget);
            NodeLayout nodeLayout = layout.getNodeLayout(node.id);

            widgetsCanvas.setWidgetPosition(widget,
                    (int)(nodeLayout.getX() * nodeDistanceMultiplier) - nodeLayout.getWidth() / 2,
                    (int)(nodeLayout.getY() * nodeDistanceMultiplier) - nodeLayout.getHeight() / 2);

            if (nodeLayout.getX() > maxX) {
                maxX = nodeLayout.getX();
            }
            if (nodeLayout.getY() > maxY) {
                maxY = nodeLayout.getY();
            }
        }

        /* Draw edges */
        for (Edge edge : graph.edges.values()) {
            EdgeLayout edgeLayout = layout.getEdgeLayout(edge.id);
            Line edgeLine = new Line(
                    (int)(edgeLayout.getSourceX() * nodeDistanceMultiplier),
                    (int)(edgeLayout.getSourceY() * nodeDistanceMultiplier),
                    (int)(edgeLayout.getEndX() * nodeDistanceMultiplier),
                    (int)(edgeLayout.getEndY() * nodeDistanceMultiplier)
            );
            graphCanvas.add(edgeLine);
        }

        /* Set the canvas size to fit the graph */
        setCanvasesSize((int)(maxX * nodeDistanceMultiplier), (int)(maxY * nodeDistanceMultiplier));

    }
}
