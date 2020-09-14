package edu.cmu.rainbow_ui.display.widgets;

import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.rainbow_ui.common.DataValueSupport;

public class BooleanLED extends Widget {

    private static final WidgetDescription widgetDescription;

    static {
        String name = "2 color LED";
        String description = "An LED that displays a value (boolean or integer) as a color";
        String type = "boolean";
        WidgetFactory factory = new WidgetFactory () {

            @Override
            public IWidget getInstance (String mapping) {
                return new BooleanLED (mapping);
            }
        };

        Map<String, WidgetPropertyDescription> propDescr = new HashMap<> ();
        propDescr.put ("name", new WidgetPropertyDescription (String.class, "Visual name of the property", ""));
        propDescr.put ("greenValue", new WidgetPropertyDescription (Boolean.class,
                "Boolean value that should show green", true));
        propDescr.put ("redValue", new WidgetPropertyDescription (Boolean.class, "Boolean value that should be red",
                false));
        widgetDescription = new WidgetDescription (name, description, type, factory, propDescr);
    }

    public static WidgetDescription register () {
        return widgetDescription;
    }

    protected Label                        label;
    protected Label                        led;
    protected String                       visualName;


    public BooleanLED (String mapping) {
        super (mapping);
        label = new Label (visualName);
        led = new Label ();
        led.setWidth (20, Unit.PIXELS);
        led.setHeight (20, Unit.PIXELS);
        led.addStyleName ("led");
        getRoot ().addComponent (new VerticalLayout (label, led));
        Styles styles = Page.getCurrent ().getStyles ();
        styles.add (".rainbow-theme .led { border : 1px black solid;}");
        styles.add (".rainbow-theme .led-green {background : palegreen;}");
        styles.add (".rainbow-theme .led-red {background : red;}");
    }

    @Override
    public WidgetDescription getWidgetDescription () {
        return widgetDescription;
    }

    @Override
    public void update () {
        boolean val;
        try {
            val = DataValueSupport.converter.to_java (value, Boolean.class);
        }
        catch (Throwable e) {
            val = false;
        }
        boolean redValue = (Boolean )getProperty ("redValue");
        boolean greenValue = (Boolean )getProperty ("greenValue");
        if (redValue == val) {
            led.removeStyleName ("led-green");
            led.addStyleName ("led-red");
        }
        if (greenValue == val) {
            led.removeStyleName ("led-red");
            led.addStyleName ("led-green");
        }

    }

    @Override
    public IWidget getClone () {
        BooleanLED clone = new BooleanLED (mapping);
        clone.setProperties (this.getProperties ());
        return clone;
    }

    @Override
    protected void onPropertyUpdate () {
        String vName = (String )properties.get ("name");
        visualName = vName;
        label.setValue (visualName);
        update ();
    }

}
