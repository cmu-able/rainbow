package edu.cmu.rainbow_ui.display.widgets;

import java.util.Map;

import com.vaadin.ui.Component;

import edu.cmu.cs.able.typelib.type.DataValue;

public interface IWidget {


    public abstract Map<String, Object> getProperties ();

    public abstract Object getProperty (String propName);

    public abstract void setProperty (String propName, Object propValue) throws IllegalArgumentException;

    public abstract IWidget getClone ();

    public abstract void deactivate ();

    public abstract void activate ();

    public abstract boolean isActive ();

    public abstract void update ();

    public abstract void setValue (DataValue value);

    public abstract DataValue getValue ();

    public abstract WidgetDescription getWidgetDescription ();

    public abstract String getMapping ();

    public abstract Component getAsComponent ();

}
