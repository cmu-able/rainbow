package edu.cmu.rainbow_ui.display;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.ingestion.AcmeRuntimeAggregator;
import edu.cmu.rainbow_ui.ingestion.EventBuffer;
import edu.cmu.rainbow_ui.ingestion.EventProcessingException;
import edu.cmu.rainbow_ui.ingestion.IEventBuffer;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;

public class MockAcmeRuntimeAggregator extends AcmeRuntimeAggregator {

    public MockAcmeRuntimeAggregator(ISystemConfiguration config,
            IDatabaseConnector dbConn) throws RuntimeAggregatorException {
        super(config, dbConn);
    }
    
    @Override
    public void start() throws RuntimeAggregatorException { 
        super.isRunning = true;
    }

    @Override
    public void stop() { 
        super.isRunning = false;
    }
}
