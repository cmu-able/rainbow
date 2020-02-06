package edu.cmu.rainbow_ui.display.widgets;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;

public class BooleanState extends Widget {

    private static final WidgetDescription widgetDescription;

    static {
        String name = "BooleanState";
        String description = "A widget that displays a boolean as a green (true) or red (false) circle";
        String type = "boolean";
        WidgetFactory factory = new WidgetFactory () {

            @Override
            public IWidget getInstance (String mapping) {
                return new BooleanState (mapping);
            }
        };
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<> ();
        propDescr.put ("name", new WidgetPropertyDescription (String.class, "Visual name of the property", ""));
        widgetDescription = new WidgetDescription (name, description, type, factory, propDescr);
    }

    public static WidgetDescription register () {
        return widgetDescription;
    }

    private Label        label;
    private FileResource noValResource;
    private FileResource trueValResource;
    private FileResource falseValResource;
    private Button       valLabel;

    public BooleanState (String mapping) {
        super (mapping);
        getImages ();
        label = new Label ();
        valLabel = new Button ();
        HorizontalLayout layout = new HorizontalLayout ();
        layout.setWidth ("100%");
        layout.addComponent (label);
        layout.addComponent (valLabel);
        layout.setExpandRatio (label, 1.0f);
        layout.setComponentAlignment (valLabel, Alignment.MIDDLE_RIGHT);
        getRoot ().addComponent (layout);
    }

    private void getImages () {
        String basepath = VaadinService.getCurrent ().getBaseDirectory ().getAbsolutePath ();
        noValResource = new FileResource (new File (basepath + "/WEB-INF/images/boolean-state/no-value.png"));
        trueValResource = new FileResource (new File (basepath + "/WEB-INF/images/boolean-state/true-value.png"));
        falseValResource = new FileResource (new File (basepath + "/WEB-INF/images/boolean-state/false-value.png"));
    }

    @Override
    public WidgetDescription getWidgetDescription () {
        return widgetDescription;
    }

    @Override
    public void update () {
        try {
            Boolean val = DataValueSupport.converter.to_java (value, Boolean.class);
            if (val) {
                valLabel.setIcon (trueValResource);
            }
            else {
                valLabel.setIcon (falseValResource);
            }
        }
        catch (ValueConversionException e) {
            Logger.getLogger (this.getClass ().getName ()).log (Level.SEVERE, null, e);
        }
    }

    @Override
    public IWidget getClone () {
        BooleanState clone = new BooleanState (mapping);
        clone.setProperties (this.getProperties ());
        return clone;
    }

    @Override
    protected void onPropertyUpdate () {
        String vName = (String )properties.get ("name");
        label.setValue (vName);
    }

}
