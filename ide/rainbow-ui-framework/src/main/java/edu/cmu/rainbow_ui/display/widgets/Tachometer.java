package edu.cmu.rainbow_ui.display.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.annotations.JavaScript;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;
import edu.cmu.rainbow_ui.display.widgets.state.TachometerState;

@JavaScript ({ "http://d3js.org/d3.v2.js", "gauge4.js" })
public class Tachometer extends JSWidget {

    private static final WidgetDescription widgetDescription;
    private String                         visualName = "";

    static {
        String name = "Tachometer";
        String description = "Shows a tachometer display of a value";
        String type = "float";
        WidgetFactory factory = new WidgetFactory () {

            @Override
            public IWidget getInstance (String mapping) {
                return new Tachometer (mapping);
            }
        };
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<> ();
        propDescr.put ("name", new WidgetPropertyDescription (String.class, "Visual name of the property", ""));
        propDescr.put ("max", new WidgetPropertyDescription (Double.class, "The maximum value that will be displayed",
                Double.valueOf (1.0)));
        propDescr.put ("min", new WidgetPropertyDescription (Double.class,
                "The minimum value that will be displayed by the widget", Double.valueOf (0)));
        propDescr.put ("startYellow", new WidgetPropertyDescription (Integer.class,
                "The percentage at which the value displayed will be yellow", Integer.valueOf (-1))); // No yellow zone by default
        propDescr.put ("startRed", new WidgetPropertyDescription (Integer.class,
                "The percentage at which the value will go into red", Integer.valueOf (90)));
        widgetDescription = new WidgetDescription (name, description, type, factory, propDescr);

    }

    static WidgetDescription register () {
        return widgetDescription;
    }

    public Tachometer (String mapping) {
        super (mapping);
    }

    @Override
    public IWidget getClone () {
        Tachometer clone = new Tachometer (mapping);
        clone.setProperties (this.getProperties ());
        return clone;
    }

    @Override
    public void update () {
        try {
            Float val;
            try {
                val = DataValueSupport.converter.to_java (value, Float.class);
            }
            catch (ValueConversionException e) {
                val = (float )DataValueSupport.converter.to_java (value, Integer.class);
            }

            getState ().value = val;
        }
        catch (ValueConversionException e) {
            Logger.getLogger (Tachometer.class.getName ()).log (Level.SEVERE, null, e);
        }

    }

    @Override
    protected TachometerState getState () {
        return (TachometerState )super.getState ();
    }

    @Override
    public WidgetDescription getWidgetDescription () {
        return widgetDescription;
    }

    @Override
    protected void onPropertyUpdate () {
        // Do this later; can the javascript update?
    }


    @Override
    protected void configure () {
        callFunction ("createGauge", "notneeded", getProperty ("name"), getProperty ("min"), getProperty ("max"),
                ((Integer )getProperty ("startRed")) / 100.0, ((Integer )getProperty ("startYellow")) / 100.0);
        super.configure ();
    }

}
