/* The MIT License
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
package edu.cmu.rainbow_ui.integration;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import edu.cmu.rainbow_ui.display.widgets.DummyWidget;
import edu.cmu.rainbow_ui.display.widgets.Widget;
import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;
import edu.cmu.rainbow_ui.display.widgets.WidgetFactory;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for widget library, widget description and widget factory
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class WidgetInfrastructureIT {

    private final WidgetFactory dummyFactory = new WidgetFactory() {

        @Override
        public Widget getInstance(String mapping) {
            return new DummyWidget(mapping);
        }
    };

    /**
     * Sets up tests to be run
     */
    @Before
    public void set_up() {
    }

    /**
     * Resets state after tests are run
     */
    @After
    public void tearDown() {
    }

    /**
     * Test that a widget description can be created
     */
    @Test
    public void createWidgetDescription() {

        WidgetDescription dummyDescription = new WidgetDescription("TestName",
                "TestDescr", "TestType", dummyFactory, null);
        assertEquals("TestName", dummyDescription.getName());
        assertEquals("TestDescr", dummyDescription.getDescription());
        assertEquals("TestType", dummyDescription.getType());
        assertEquals(dummyFactory, dummyDescription.getFactory());
    }

    /**
     * Test that a widget description can be registered with the widget library
     */
    @Test
    public void registerWidgetDescription() {
        WidgetDescription dummyDescription = new WidgetDescription("TestName",
                "TestDescr", "TestType", dummyFactory, null);
        WidgetLibrary.register(dummyDescription);
        Map<String, WidgetDescription> descriptions = WidgetLibrary
                .getWidgetDescriptions();
        assertNotNull(descriptions);
        assertEquals(descriptions.get("TestName"), dummyDescription);
    }

    /**
     * Test that a widget factory can produce an instance of a widget
     */
    @Test
    public void createInstance() throws ValueConversionException {
        DummyWidget dummyWidget = (DummyWidget) dummyFactory.getInstance("Mapping");
        assertNotNull(dummyWidget);
        String name = "Test";
        String description = "Description";
        Object value = "TEST";
        boolean active = false;
        String mapping = "Mapping";
        assertEquals(name, dummyWidget.getWidgetDescription().getName());
        assertEquals(description, dummyWidget.getWidgetDescription().getDescription());
        assertEquals(value, DataValueSupport.converter.to_java(dummyWidget.getValue(), String.class));
        assertEquals(active, dummyWidget.isActive());
        assertEquals(mapping, dummyWidget.getMapping());
    }
}
