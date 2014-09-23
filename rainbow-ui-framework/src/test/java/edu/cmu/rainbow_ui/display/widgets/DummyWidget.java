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
package edu.cmu.rainbow_ui.display.widgets;

import com.google.common.collect.HashBiMap;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import edu.cmu.rainbow_ui.display.widgets.Widget;
import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;
import edu.cmu.rainbow_ui.display.widgets.WidgetFactory;
import edu.cmu.rainbow_ui.display.widgets.WidgetPropertyDescription;
import java.util.HashMap;
import java.util.Map;

/**
 * Dummy widget used for testing
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class DummyWidget extends Widget {

    private static final WidgetDescription widgetDescription;

    public Component displayComponent;

    static {
        String name = "Test";
        String description = "Description";
        String type = "Integer";
        DummyWidgetFactory factory = new DummyWidget.DummyWidgetFactory();
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<>();
        widgetDescription = new WidgetDescription(name,
                description, type, factory, propDescr);
    }

    public static WidgetDescription register() {
        return widgetDescription;
    }

    public DummyWidget(String mapping) {
        super(mapping);
        active = false;
        value = DataValueSupport.pscope.string().make("TEST");
        displayComponent = new TextField();
        this.setCompositionRoot(displayComponent);
    }

    /**
     * {@inheritDoc}
     *
     * Set the value of the text field to the string held by value
     */
    @Override
    public
            void update() {
        try {
            ((TextField) displayComponent).setValue(DataValueSupport.converter
                    .to_java(value, String.class
                    ));
        } catch (ReadOnlyException | ValueConversionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected
            void onPropertyUpdate() {
        // Do nothing
    }

    @Override
    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

    /**
     * Creates an instance of a dummy widget
     *
     * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
     */
    public static class DummyWidgetFactory implements WidgetFactory {

        /**
         * {@inheritDoc}
         *
         * @return a instance of DummyWidget
         */
        @Override
        public IWidget getInstance(String mapping) {
            return new DummyWidget(mapping);
        }
    }

    @Override
    public IWidget getClone() {
        return new DummyWidget(mapping);
    }
}
