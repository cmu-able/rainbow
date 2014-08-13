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

import com.vaadin.server.VaadinRequest;
import edu.cmu.rainbow_ui.display.DummySystemViewProvider;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.graph.Graph;
import edu.cmu.rainbow_ui.display.graph.Graph.Edge;
import edu.cmu.rainbow_ui.display.graph.Graph.Node;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.ui.MockRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.ingestion.AcmeInternalModelInstance;
import java.io.IOException;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Unit tests for AcmeGraph class.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeGraphTest {

    /**
     * System view provider used to test the AcmeGraph instance
     */
    private ISystemViewProvider svp;

    /**
     * View configuration for graph creation.
     */
    private ViewConfiguration viewConfig;
    
    /**
     * Dummy UI
     */
    private AbstractRainbowVaadinUI dummyUI;

    public AcmeGraphTest() {
    }

    @Before
    public void setUp() throws IOException, ParsingFailureException {
        /**
         * Setup the dummy SVP that is able to supply a model
         */
        StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString(
                "ZNewsSys.acme");
        AcmeSystem sys = resource.getModel().getSystems().iterator().next();
        final IModelInstance<IAcmeSystem> instance = new AcmeInternalModelInstance(sys, "Acme");

        svp = new DummySystemViewProvider() {
            @Override
            public IModelInstance<?> getView() {
                return instance;
            }
        };

        /* Empty view configuration */
        viewConfig = new ViewConfiguration();
        
        dummyUI = new AbstractRainbowVaadinUI() {
            @Override
            public ISystemViewProvider getSystemViewProvider() {
                return svp;
            }

            @Override
            public ViewConfiguration getViewConfiguraion() {
                return viewConfig;
            }

            @Override
            public ViewControl getViewControl() {
                return null;
            }

            @Override
            public void pushUpdate() {
                // Do nothing
            }

            @Override
            public void saveViewConfiguration() {
                // Do nothing
            }

            @Override
            protected void init(VaadinRequest request) {
                // Do nothing
            }

            @Override
            public void setViewConfiguration(ViewConfiguration viewConfig) {
                // TODO Auto-generated method stub
                
            }
        };

    }

    /**
     * Test of subUpdate method, of class AcmeGraph.
     * @throws Exception 
     */
    @Test
    public void testSubUpdate() throws Exception {
        System.out.println("subUpdate");

        AcmeGraph instance = new AcmeGraph(dummyUI);

        /**
         * The subupdate should be called within the update in order to load the model.
         */
        instance.update();

        /**
         * Check the constructed graph
         */
        Graph graph = instance.getGraphLayer().getGraph();
        Node server0 = graph.nodes.get("ZNewsSys.Server0");
        assertNotNull(server0);
        Node server1 = graph.nodes.get("ZNewsSys.Server1");
        assertNotNull(server1);
        String port = server0.ports.get(0);
        assertEquals("ZNewsSys.Server0.http0", port);
        Node connector = graph.nodes.get("ZNewsSys.httpConn_0_0");
        assertNotNull(connector);
        Edge attachment = graph.edges.get("ZNewsSys.Client1.p0 to httpConn_0_0.req");
        assertNotNull(attachment);
    }

    /**
     * Test of get/set RootComponent method, of class AcmeGraph.
     * @throws Exception 
     */
    @Test
    public void testRootComponent() throws Exception {
        System.out.println("get/set RootComponent");

        AcmeGraph instance = new AcmeGraph(dummyUI);

        String expResult = "ZNewsSys";
        IAcmeSystem result = instance.getCurrentSystem();
        assertNotNull(result);
        assertEquals(expResult, result.getName());

        /**
         * TODO: add more
         */
    }
}
