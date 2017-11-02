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
package edu.cmu.rainbow_ui.ingestion;

import edu.cmu.rainbow_ui.storage.DummyDatabaseConnector;
import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;

/**
 * Unit testing for AcmeRuntimeAggregator.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeRuntimeAggregatorTest {

    private ISystemConfiguration systemConfig;
    private IRainbowMessage testMsg;

    public AcmeRuntimeAggregatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        systemConfig = new SystemConfiguration("src/test/resources/system_default.properties");
        testMsg = new RainbowESEBMessage();

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getInternalModel method, of class AcmeRuntimeAggregator.
     *
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    @Test
    public void testGetInternalModel() throws RuntimeAggregatorException {
        System.out.println("getInternalModel");
        AcmeRuntimeAggregator instance = new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector());
        IModelInstance<IAcmeSystem> modelInstance = instance.getInternalModel();
        assertNotNull(modelInstance);
        assertEquals("Acme", modelInstance.getModelType());
        assertEquals("ZNewsSys", modelInstance.getModelName());
        IAcmeSystem system = modelInstance.getModelInstance();
        assertNotNull(system);
        assertEquals("ZNewsSys", system.getName());
    }

    /**
     * Test of copyInternalModel method, of class AcmeRuntimeAggregator.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCopyInternalModel() throws Exception {
        System.out.println("copyInternalModel");
        AcmeRuntimeAggregator instance = new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector());

        double value1 = 99.9;
        double value2 = 0.0;

        /* Set the first value */
        IModelInstance<IAcmeSystem> modelInstance = instance.getInternalModel();
        IAcmeSystem system = modelInstance.getModelInstance();
        IAcmeProperty prop = (IAcmeProperty) system.getComponent("Server0").getProperty("load");
        assertNotNull(prop);
        IAcmePropertyValue val1 = StandaloneLanguagePackHelper.defaultLanguageHelper().propertyValueFromString(
                Double.toString(value1), new RegionManager());
        IAcmeCommand command = system.getCommandFactory().propertyValueSetCommand(prop, val1);
        command.execute();

        /* Copy and change value in the original */
        IModelInstance<IAcmeSystem> copyModelInstance = instance.copyInternalModel();
        assertNotNull(copyModelInstance);
        IAcmePropertyValue val2 = StandaloneLanguagePackHelper.defaultLanguageHelper().propertyValueFromString(
                Double.toString(value2), new RegionManager());
        command = system.getCommandFactory().propertyValueSetCommand(prop, val2);
        command.execute();

        /* Get values from original and copy */
        IAcmePropertyValue origValue = prop.getValue();
        assertNotNull(origValue);
        IAcmeSystem copySystem = copyModelInstance.getModelInstance();
        assertNotNull(copySystem);
        IAcmeProperty copyProp = (IAcmeProperty) copySystem.getComponent("Server0").getProperty("load");
        assertNotNull(copyProp);
        IAcmePropertyValue copyVal = copyProp.getValue();
        assertNotNull(copyVal);

        /* The value in copy should not change */
        assertEquals(val2, origValue);
        assertEquals(val1, copyVal);

    }

    /**
     * Test of getEventBuffer method, of class AcmeRuntimeAggregator.
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    @Test
    public void testGetEventBuffer() throws RuntimeAggregatorException {
        System.out.println("getEventBuffer");
        AcmeRuntimeAggregator instance = new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector());
        IEventBuffer buffer = instance.getEventBuffer();
        assertNotNull(buffer);
    }

    /**
     * Test of processEvent method, of class AcmeRuntimeAggregator.
     *
     * @throws edu.cmu.rainbow_ui.ingestion.EventProcessingException
     * @throws org.sa.rainbow.core.error.RainbowException
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    @Test
    public void testProcessEvent() throws EventProcessingException, RainbowException, RuntimeAggregatorException {
        System.out.println("processEvent");
        AcmeRuntimeAggregator instance = new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector());
        String channel = "MODEL_CHANGE";
        Float expRes = (float) 99.9;
        IRainbowMessage event = new RainbowESEBMessage();
        event.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP, AcmeModelEventType.SET_PROPERTY_VALUE.toString());
        event.setProperty(AcmeModelOperation.PROPERTY_PROP, "ZNewsSys.Server0.load");
        event.setProperty(AcmeModelOperation.VALUE_PROP, expRes.toString());
        event.setProperty(ESEBConstants.MSG_SENT, System.currentTimeMillis() + "");

        /* RA should be started to process events */
        instance.start();
        instance.processEvent(channel, event);

        /* Get property value */
        IModelInstance<IAcmeSystem> modelInstance = instance.getInternalModel();
        assertNotNull(modelInstance);
        IAcmeSystem system = modelInstance.getModelInstance();
        assertNotNull(system);
        IAcmeProperty prop = (IAcmeProperty) system.getComponent("Server0").getProperty("load");
        assertNotNull(prop);
        IAcmePropertyValue val = prop.getValue();
        assertNotNull(val);
        Float res = PropertyHelper.toJavaVal((IAcmeFloatValue) val);

        /* After event processing the value should be updated */
        assertEquals(expRes, res);

        /* Test processing from unknown channel*/
        try {
            instance.processEvent("UNKNOWN", event);
        } catch (EventProcessingException ex) {
            // Do nothing
        }

        /* Test RA stop */
        instance.stop();
    }

    /**
     * Test of getEventTimestamp method, of class AcmeRuntimeAggregator.
     *
     * @throws RainbowException
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    @Test
    public void testTimestampCoversion() throws RainbowException, RuntimeAggregatorException {
        System.out.println("getEventTimestamp");
        AcmeRuntimeAggregator instance = new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector());
        IRainbowMessage event = new RainbowESEBMessage();
        Date expResult = new Date(System.currentTimeMillis());
        event.setProperty(ESEBConstants.MSG_SENT, expResult.getTime() + "");
        Date result = instance.getEventTimestamp(event);
        assertEquals(expResult, result);
    }

    /**
     * Test processing of streaming events by processEvents method, of class AcmeRuntimeAggregator.
     *
     * Tests that the processed event goes to the event buffer.
     *
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     * @throws org.sa.rainbow.core.error.RainbowException
     * @throws edu.cmu.rainbow_ui.ingestion.EventProcessingException
     */
    @Test
    public void testProcessStreamingEvent() throws RuntimeAggregatorException, RainbowException, EventProcessingException {
        System.out.println("processStreamingEvent");
        AcmeRuntimeAggregator instance = new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector());
        instance.start();
        IEventBuffer buffer = instance.getEventBuffer();
        assertNotNull(buffer);
        buffer.activate();
        assertEquals(true, buffer.isActive());
        IRainbowMessage event = new RainbowESEBMessage();
        event.setProperty(ESEBConstants.MSG_CHANNEL_KEY, "MODEL_DS");
        event.setProperty(ESEBConstants.MSG_SENT, System.currentTimeMillis() + "");
        instance.processEvent("MODEL_DS", event);
        List<IRainbowMessage> bufList = new ArrayList<>();
        int num = buffer.drainToCollection(bufList);
        assertEquals(1, num);
        IRainbowMessage result = bufList.get(0);
        assertEquals(event.toString(), result.toString());
    }
}
