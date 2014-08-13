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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.common.DataValueSupport;
import edu.cmu.rainbow_ui.display.DummySystemViewProvider;
import edu.cmu.rainbow_ui.display.ui.DummyTestUI;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.display.widgets.DummyWidget;
import edu.cmu.rainbow_ui.display.widgets.Widget;

/**
 * Tests for widgets and widget control
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class ViewControlIT {

    private DummySystemViewProvider provider;
    private ViewControl control;

    /**
     * Sets up tests to be run
     *
     * @throws Exception if there are any errors
     */
    @Before
    public void set_up() throws Exception {
        DummyTestUI ui = new DummyTestUI();
        ui.init(null);
        provider = new DummySystemViewProvider();
        control = new ViewControl(ui);
    }

    /**
     * Resets state after tests are run
     *
     * @throws Exception if there are any errors
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test the creation of a widget
     *
     * @throws ValueConversionException if the datavalues cannot be converted
     */
    @Test
    public void widgetCreation() throws ValueConversionException {
        String mapping = "Mapping";
        DummyWidget widget = new DummyWidget(mapping);
        assertNotNull(widget);
        String name = "Test";
        String description = "Description";
        Object value = new String("TEST");
        boolean active = false;
        assertEquals(name, widget.getWidgetDescription().getName());
        assertEquals(description, widget.getWidgetDescription().getDescription());
        assertEquals(value, DataValueSupport.converter.to_java(widget.getValue(), String.class));
        assertEquals(widget.isActive(), active);
        assertEquals(widget.getMapping(), mapping);
    }

    /**
     * Test that a widget's value can be set
     */
    @Test
    public void setValue() {
        String mapping = "Mapping";
        DummyWidget widget = new DummyWidget(mapping);
        assertNotNull(widget);
        DataValue value = DataValueSupport.pscope.string().make("TEST");
        widget.setValue(value);
        assertEquals(widget.getValue(), value);
    }

    /**
     * Test that a widget can be determined to be active or not
     */
    @Test
    public void isActive() {
        String mapping = "Mapping";
        DummyWidget widget = new DummyWidget(mapping);
        assertNotNull(widget);
        assertEquals(widget.isActive(), false);
    }

    /**
     * Test that a widget can be activated and deactivate
     */
    @Test
    public void activation() {
        String mapping = "Mapping";
        DummyWidget widget = new DummyWidget(mapping);
        assertNotNull(widget);
        widget.activate();
        assertEquals(widget.isActive(), true);
        widget.deactivate();
        assertEquals(widget.isActive(), false);
    }

    /**
     * Test that a widget can be added to the widget control, that the widget control's list can be
     * gotten, and that the widget can be removed from the widget control
     */
    @Test
    public void widgetControl() {
        String mapping = "Mapping";
        DummyWidget widget = new DummyWidget(mapping);
        assertNotNull(widget);
        control.addWidget(widget);
        List<Widget> widgets = control.getWidgetList();
        assertNotNull(widgets);
        assertEquals(widgets.get(0), widget);
        control.removeWidget(widget);
        widgets = control.getWidgetList();
        assertNotNull(widgets);
        assertEquals(widgets.size(), 0);
    }

    /**
     * Test that a widget can be updated and pushed to the UI
     */
    @Test
    public void pushUpdate() {
        String mapping = "Mapping";
        DummyTestUI dummyUi = new DummyTestUI();
        DummyWidget widget = new DummyWidget(mapping);
        assertNotNull(widget);
        control.addWidget(widget);
        List<Widget> widgets = control.getWidgetList();
        assertNotNull(widgets);
        assertEquals(widgets.get(0), widget);

        control.pushUpdate();

        assertEquals(widget.getValue(), provider.getValue(mapping));
    }
}
