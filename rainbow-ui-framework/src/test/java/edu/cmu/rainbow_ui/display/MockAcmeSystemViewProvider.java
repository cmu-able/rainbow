package edu.cmu.rainbow_ui.display;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;

public class MockAcmeSystemViewProvider extends
        AccessibleAcmeSystemViewProvider {
    String session;

    public MockAcmeSystemViewProvider(
            IRuntimeAggregator<IAcmeSystem> runtimeAggregator,
            AbstractRainbowVaadinUI ui, ISystemConfiguration systemConfig) {
        super(runtimeAggregator, ui, systemConfig);
        session = "test";
    }
    
    /**
     * {@inheritDoc}
     *
     * Empty the event store and start the scheduled update task
     */
    @Override
    public void setUseCurrent() {
        isCurrent = true;
    }

    /**
     * {@inheritDoc}
     *
     * Empty the event store and load historical events. Also cancel the update task
     */
    @Override
    public void setUseHistorical(Date time) throws SystemViewProviderException {
        isCurrent = false;
    }
    
    @Override
    public void setSession(String session) {
        this.session = session;
    }
    
    @Override
    public String getSession() {
        return this.session;
    }

}
