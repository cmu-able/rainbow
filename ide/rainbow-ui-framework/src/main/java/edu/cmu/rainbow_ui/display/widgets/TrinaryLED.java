package edu.cmu.rainbow_ui.display.widgets;

import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;

import edu.cmu.rainbow_ui.common.DataValueSupport;

public class TrinaryLED extends BooleanLED {

    private static final WidgetDescription widgetDescription;

    static {
        String name = "3 color LED";
        String description = "An LED that displays a value (integer) as a color";
        String type = "integer";
        WidgetFactory factory = new WidgetFactory () {

            @Override
            public IWidget getInstance (String mapping) {
                return new BooleanLED (mapping);
            }
        };

        Map<String, WidgetPropertyDescription> propDescr = new HashMap<> ();
        propDescr.put ("name", new WidgetPropertyDescription (String.class, "Visual name of the property", ""));
        propDescr.put ("greenValue", new WidgetPropertyDescription (Integer.class,
                "Boolean value that should show green", 1));
        propDescr.put ("redValue",
                new WidgetPropertyDescription (Integer.class, "Boolean value that should be red", -1));
        propDescr.put ("whiteValue", new WidgetPropertyDescription (Integer.class, "Boolean value that should be red",
                0));
        widgetDescription = new WidgetDescription (name, description, type, factory, propDescr);
    }

    public static WidgetDescription register () {
        return widgetDescription;
    }

    public TrinaryLED (String mapping) {
        super (mapping);
        Styles styles = Page.getCurrent ().getStyles ();
        styles.add (".rainbow-theme .led-white {background : white;}");

    }

    @Override
    public WidgetDescription getWidgetDescription () {
        return widgetDescription;
    }

    @Override
    public void update () {
        int val;
        try {
            val = DataValueSupport.converter.to_java (value, Integer.class);
        }
        catch (Throwable e) {
            val = 0;
        }
        int redValue = (Integer )getProperty ("redValue");
        int greenValue = (Integer )getProperty ("greenValue");
        int whiteValue = (Integer )getProperty ("whiteValue");
        led.removeStyleName ("led-green");
        led.removeStyleName ("led-red");
        led.removeStyleName ("led-white");
        if (redValue == val) {
            led.addStyleName ("led-red");
        }
        if (greenValue == val) {
            led.addStyleName ("led-green");
        }
        if (whiteValue == val) {
            led.addStyleName ("led-white");
        }
    }

}
