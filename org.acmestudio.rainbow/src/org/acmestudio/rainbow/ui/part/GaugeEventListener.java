package org.acmestudio.rainbow.ui.part;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeRecordValue;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.event.AcmeEventListenerAdapter;
import org.acmestudio.acme.model.event.AcmePropertyEvent;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;

public class GaugeEventListener extends AcmeEventListenerAdapter {

    private final IAcmeProperty m_property;
    private final IAcmeProperty m_gaugeDescription;
    private final Browser m_browser;

    public GaugeEventListener(IAcmeProperty property, IAcmeProperty gd, Browser browser) {
        m_property = property;
        m_gaugeDescription = gd;
        m_browser = browser;
    }
    
    @Override
    public void propertyValueSet (AcmePropertyEvent event) {
        Display.getDefault().asyncExec (new Runnable() {
            public void run () {
                // Update the browser gauge with the new value
                String gaugeId = getGaugeId();
                float propertyValue = PropertyHelper.<Float >toJavaVal(m_property.getValue(), Float.class);
                String script = "updateGauge ('" + gaugeId + "', " + Float.toString(propertyValue) + ");";
                m_browser.execute(script);
            }
        });
    }

    private String getGaugeId () {
        IAcmeRecordValue gd = (IAcmeRecordValue )m_gaugeDescription.getValue();
        String gaugeId = (String) PropertyHelper.toJavaVal(gd.getField("name").getValue());
        return gaugeId;
    }

    @Override
    public void propertyDeleted (AcmePropertyEvent event) {
        if (event.getProperty() == m_gaugeDescription && event.getPropertyBearer() == m_property) {
            m_property.removeEventListener(this);
            m_browser.execute("removeRainbowGauge (" + getGaugeId () + ");");
        }
    }
}
