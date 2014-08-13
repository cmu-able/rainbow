package edu.cmu.rainbow_ui.display.widgets;

import java.util.Map;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

import edu.cmu.rainbow_ui.display.widgets.state.LineGaugeState;

@JavaScript ({ "http://d3js.org/d3.v2.js", "line-gauge5.js" })
public class LineGaugeComponent extends AbstractJavaScriptComponent {

    public LineGaugeComponent () {
        super ();
    }

    public void updateValue (Double val) {
        getState ().value = val;
    }

    public void configure (Map<String, Object> properties) {
        callFunction ("createGauge", "notneeded", properties.get ("name"), properties.get ("min"),
                properties.get ("max"), properties.get ("window"), properties.get ("yLabel"),
                properties.get ("threshold"));
    }

    public void start () {
        callFunction ("activate");
    }

    public void stop () {
        callFunction ("deactivate");
    }

    @Override
    protected LineGaugeState getState () {
        return (LineGaugeState )super.getState ();
    }

}
