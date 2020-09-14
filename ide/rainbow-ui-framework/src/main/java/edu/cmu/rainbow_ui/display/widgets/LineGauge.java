package edu.cmu.rainbow_ui.display.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;

public class LineGauge extends Widget {

    private static final WidgetDescription widgetDescription;
    static {
        String name = "LineGauge";
        String description = "Shows a moving line display of the value and its history";
        String type = "float";
        WidgetFactory factory = new WidgetFactory () {

            @Override
            public IWidget getInstance (String mapping) {
                return new LineGauge (mapping);
            }

        };
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<> ();
        propDescr.put ("name", new WidgetPropertyDescription (String.class, "Visual name of the property", ""));
        propDescr.put ("max", new WidgetPropertyDescription (Double.class, "The maximum value that will be displayed",
                Double.valueOf (1.0)));
        propDescr.put ("min", new WidgetPropertyDescription (Double.class,
                "The minimum value that will be displayed by the widget", Double.valueOf (0)));
        propDescr.put ("window", new WidgetPropertyDescription (Integer.class, "The number of samples to display",
                Double.valueOf (10)));
        propDescr.put ("yLabel", new WidgetPropertyDescription (String.class, "The label to use for the Y axis", ""));
        propDescr.put ("threshold", new WidgetPropertyDescription (Double.class,
                "The value to display as the threshold", Double.valueOf (-1.0)));
        widgetDescription = new WidgetDescription (name, description, type, factory, propDescr);
    }

    static WidgetDescription register () {
        return widgetDescription;
    }

    private Label m_label;
    private LineGaugeComponent m_gauge;

    public LineGauge (String mapping) {
        super (mapping);

        m_label = new Label ();
        m_gauge = new LineGaugeComponent ();

        VerticalLayout layout = new VerticalLayout ();
        layout.setWidth ("100%");
        layout.addComponent (m_gauge);
        layout.addComponent (m_label);
        layout.setExpandRatio (m_gauge, 1.0f);
        layout.setComponentAlignment (m_gauge, Alignment.MIDDLE_CENTER);

        getRoot ().addComponent (layout);
    }

    @Override
    public IWidget getClone () {
        LineGauge clone = new LineGauge (mapping);
        clone.setProperties (this.getProperties ());
        return clone;
    }

    @Override
    public void update () {
        Double val = 0.0;
        try {
            val = DataValueSupport.converter.to_java (value, Double.class);
        }
        catch (ValueConversionException ex) {
            try {
                val = (double )DataValueSupport.converter.to_java (value, Float.class);
            }
            catch (ValueConversionException e) {
                Logger.getLogger (LineGauge.class.getName ()).log (Level.SEVERE, null, ex);

            }

        }
        m_gauge.updateValue (val);
    }

    @Override
    public WidgetDescription getWidgetDescription () {
        return widgetDescription;
    }

    @Override
    protected void onPropertyUpdate () {
        String vName = (String )properties.get ("name");
        m_label.setValue (vName);
    }

    @Override
    public void activate () {
        super.activate ();
        m_gauge.configure (this.getProperties ());
        m_gauge.start ();
    }

    @Override
    public void deactivate () {
        super.deactivate ();
        m_gauge.stop ();
    }

}
