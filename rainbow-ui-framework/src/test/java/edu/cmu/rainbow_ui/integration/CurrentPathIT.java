/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.cmu.rainbow_ui.integration;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.AccessibleAcmeSystemViewProvider;
import edu.cmu.rainbow_ui.display.AccessibleApplicationCore;
import edu.cmu.rainbow_ui.display.AcmeConversionException;
import edu.cmu.rainbow_ui.display.AcmeConverterSupport;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.MockApplicationCore;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.ui.DummyTestUI;
import edu.cmu.rainbow_ui.ingestion.EventProcessingException;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;
import edu.cmu.rainbow_ui.storage.IHistoryProvider;
import edu.cmu.rainbow_ui.storage.MockDatabaseConnector;

/**
 * Integration Tests for displaying and updating the current model and events
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 * 
 */
public class CurrentPathIT {

    /**
     * Location of system settings file
     */
    private final static String SYSTEM_CONFIG_FILE = "src/test/resources/system_test.properties";
    private static IRuntimeAggregator<?> runtimeAgg;
    private static ISystemConfiguration systemConfig;
    private static AccessibleAcmeSystemViewProvider systemViewProvider;

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @BeforeClass
    public static void setUp() throws Exception {
        MockApplicationCore mockAppCore = MockApplicationCore.getInstance();
        mockAppCore.setUseMockRainbow(false);
        mockAppCore.startup(SYSTEM_CONFIG_FILE);
        systemConfig = new SystemConfiguration(SYSTEM_CONFIG_FILE);
        runtimeAgg = mockAppCore.getRuntimeAggregator();
        runtimeAgg.start();
        DummyTestUI ui = new DummyTestUI();
        ui.init(null);
        systemViewProvider = new AccessibleAcmeSystemViewProvider(
                (IRuntimeAggregator<IAcmeSystem>) runtimeAgg, ui, systemConfig);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void EventProcessing() throws EventProcessingException,
            RainbowException {
        systemViewProvider.setUseCurrent();
        Date[] eventTimes = { new Date(1000), new Date(2000), new Date(3000) };

        IntegrationTestHelper.processStreamingEvents(eventTimes, runtimeAgg,
                new MockDatabaseConnector());
        systemViewProvider.update();
        List<IRainbowMessage> newEvents = systemViewProvider.getNewEvents();
        assertNotNull(newEvents);
        assertEquals(3, newEvents.size());
        assertEquals(
                eventTimes[0],
                new Date(Long.parseLong((String) newEvents.get(0).getProperty(
                        ESEBConstants.MSG_SENT))));
    }

    @Test
    public void modelUpdating() throws EventProcessingException,
            RainbowException, AcmeConversionException {
        systemViewProvider.setUseCurrent();
        Date[] eventTimes = { new Date(1000) };

        IntegrationTestHelper.processModelEvents(eventTimes, runtimeAgg,
                new MockDatabaseConnector());
        IModelInstance<IAcmeSystem> runtimeAggModel = (IModelInstance<IAcmeSystem>) runtimeAgg
                .copyInternalModel();
        IAcmeProperty property = (IAcmeProperty) ModelHelper
                .getElementFromQualifiedName(
                        runtimeAggModel.getModelInstance(),
                        "ZNewsSys.Server0.load");

        systemViewProvider.update();
        assertEquals(AcmeConverterSupport.convertToDataValue(property),
                systemViewProvider.getValue("ZNewsSys.Server0.load"));
    }

}
