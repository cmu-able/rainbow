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
package edu.cmu.rainbow_ui.display.widgets;

import com.vaadin.ui.CustomComponent;
import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Common functions for widgets and a framework to build custom widgets.
 *
 * <p>
 * Each Widget is attached to a certain property identified by {@code mapping} and receives its
 * updates as {@code value}. Widget may be in {@code active} or {@code inactive} state. For widget
 * customizations it may hold {@code properties}.
 * </p>
 *
 * <h1> Widget construction </h1>
 * <p>
 * Widgets are indented to be discovered dynamically through reflection and registered in the
 * {@link WidgetLibrary}. Widget creation is done through {@link WidgetFactory}. For examples see
 * existing widget implementations.
 * </p>
 *
 * <h1> Widget value and activity state </h1>
 * <p>
 * Each widget receives updates of its internal value through {@link #setValue(edu.cmu.cs.able.typelib.type.DataValue)
 * }. The processing(i.e. display) of this value should take place in the {@link #update()} method
 * which is defined in a widget implementation. The update method is called by {@link ViewControl}
 * at the specified rate.
 * </p>
 * <p>
 * To decrease the bandwidth of the server-client communication widgets that are currently not
 * visible on the screen may be deactivated. If the {@link #isActive()} method return {@code false}
 * for the widget its {@link #update()} method is not called.
 * </p>
 *
 * <h1> Widget properties </h1>
 * <p>
 * For customization of behavior widgets may hold properties. Only properties defined in a widget's
 * description may be set through the {@link #setProperty(java.lang.String, java.lang.Object) }
 * method. For more details see {@link WidgetDescription} and {@link WidgetPropertyDescription}
 * </p>
 *
 * <h1> How to create a widget </h1>
 * <p>
 * Valid widget should extend this class and be placed in the
 * {@link edu.cmu.rainbow_ui.display.widgets} package. This will allow it to be discoverable at the
 * system startup. In order to be registered the widget implementation should provide a static
 * method with the following signature:
 * <pre>
 * static WidgetDescription register();
 * </pre> This method should return a valid widget description that will be added into the
 * {@link WidgetLibrary}.
 * </p>
 *
 * @see WidgetLibrary
 * @see WidgetDescription
 * @see WidgetPropertyDescription
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public abstract class Widget extends CustomComponent implements IUpdatableWidget {

    /**
     * Identifier of the property displayed by this widget.
     */
    protected String mapping;
    /**
     * Value of the property. Updated via {@link #setValue(edu.cmu.cs.able.typelib.type.DataValue)}
     */
    protected DataValue value;
    /**
     * Current activity state.
     */
    protected boolean active;

    /**
     * Map of widget properties.
     */
    protected Map<String, Object> properties = new HashMap<>();

    /**
     * Constructor for a widget
     *
     * @param mapping a string which uniquely identifies a property in the model
     */
    public Widget(String mapping) {
        this.mapping = mapping;
        active = false;
        /* Fill default properties */
        for (String prop : getWidgetDescription().getProperties().keySet()) {
            properties.put(prop, getWidgetDescription().getProperties().get(prop).getDefaultValue());
        }
    }

    /**
     * Getter for the mapping property of the widget
     *
     * @return the mapping of the widget
     */
    public String getMapping() {
        return mapping;
    }

    /**
     * Get the description of the widget.
     *
     * @return widget description
     */
    public abstract WidgetDescription getWidgetDescription();

    /**
     * Getter for the value property of the widget
     *
     * @return the value of the widget
     */
    public DataValue getValue() {
        return value;
    }

    /**
     * Setter for the value property of the widget
     *
     * @param value the value of the widget
     */
    public void setValue(DataValue value) {
        this.value = value;
    }

    @Override
    public abstract void update();

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public void deactivate() {
        active = false;
    }

    /**
     * Get a clone of the widget
     *
     * @return a clone of the widget
     */
    public abstract Widget getClone();

    /**
     * Set a property.
     *
     * @param propName property name
     * @param propValue property value
     */
    public void setProperty(String propName, Object propValue) throws IllegalArgumentException {
        WidgetPropertyDescription propDescr = getWidgetDescription().getProperties().get(propName);
        if (propDescr == null) {
            throw new IllegalArgumentException("Widget doesn't have a property: " + propName);
        }
        if (!propDescr.getValueClass().isAssignableFrom(propValue.getClass())) {
            throw new IllegalArgumentException("Widget property value class missmatch."
                    + "\n    expected: " + propDescr.getValueClass().toString()
                    + "\n    actual: " + propValue.getClass().toString()
            );
        }
        properties.put(propName, propValue);
        onPropertyUpdate();
    }

    /**
     * Set all properties.
     *
     * @param props properties map
     */
    protected void setProperties(Map<String, Object> props) {
        properties = props;
        onPropertyUpdate();
    }

    /**
     * Get a property.
     *
     * @param propName property name
     * @return property value
     */
    public Object getProperty(String propName) {
        return properties.get(propName);
    }

    /**
     * Get all properties.
     *
     * @return properties map
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Signals the widget implementation that properties were updated.
     */
    protected abstract void onPropertyUpdate();

}
