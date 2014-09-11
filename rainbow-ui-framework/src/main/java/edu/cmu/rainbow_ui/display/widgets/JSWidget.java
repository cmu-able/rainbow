package edu.cmu.rainbow_ui.display.widgets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.Component;

import edu.cmu.cs.able.typelib.type.DataValue;

public abstract class JSWidget extends AbstractJavaScriptComponent implements IWidget, IUpdatableWidget {

    protected String              mapping;

    protected DataValue           value;

    protected boolean             active;

    protected boolean             configured;

    protected Map<String, Object> properties = new HashMap<> ();

    private String                propertyMonitoring;

    private IHandler              closeHandler;

    public JSWidget (String mapping) {
        this.mapping = mapping;
        active = false;
        configured = false;
        /* Fill default properties */
        for (String prop : getWidgetDescription ().getProperties ().keySet ()) {
            properties.put (prop, getWidgetDescription ().getProperties ().get (prop).getDefaultValue ());
        }
    }

    @Override
    public Map<String, Object> getProperties () {
        return Collections.unmodifiableMap (properties);
    }

    @Override
    public Object getProperty (String propName) {
        return properties.get (propName);
    }

    /**
     * Set all properties.
     *
     * @param props
     *            properties map
     */
    protected void setProperties (Map<String, Object> props) {
        properties = props;
        onPropertyUpdate ();
    }


    @Override
    public void setProperty (String propName, Object propValue) throws IllegalArgumentException {
        WidgetPropertyDescription propDescr = getWidgetDescription ().getProperties ().get (propName);
        if (propDescr == null) throw new IllegalArgumentException ("Widget doesn't have a property: " + propName);
        if (!propDescr.getValueClass ().isAssignableFrom (propValue.getClass ()))
            throw new IllegalArgumentException ("Widget property value class missmatch." + "\n    expected: "
                    + propDescr.getValueClass ().toString () + "\n    actual: " + propValue.getClass ().toString ());
        properties.put (propName, propValue);
        onPropertyUpdate ();
    }

    @Override
    public abstract IWidget getClone ();

    @Override
    public void deactivate () {
        active = false;
        configured = false;
    }

    @Override
    public void activate () {
        active = true;
        if (!configured) {
            configure ();
        }
    }

    protected void configure () {
        configured = true;
    }

    @Override
    public boolean isActive () {
        return active;
    }

    @Override
    public abstract void update ();

    @Override
    public void setValue (DataValue value) {
        this.value = value;
    }

    @Override
    public DataValue getValue () {
        return value;
    }

    @Override
    public abstract WidgetDescription getWidgetDescription ();

    @Override
    public String getMapping () {
        return mapping;
    }

    /**
     * Signals the widget implementation that properties were updated.
     */
    protected abstract void onPropertyUpdate ();

    @Override
    public Component getAsComponent () {
        return this;
    }

    @Override
    public void setPropertyMonitoring (String propertyName) {
        propertyMonitoring = propertyName;
    }

    @Override
    public String getPropertyMonitoring () {
        return propertyMonitoring;
    }

    @Override
    public void setCloseHandler (IHandler closeHandler) {
        this.closeHandler = closeHandler;
    }

}
