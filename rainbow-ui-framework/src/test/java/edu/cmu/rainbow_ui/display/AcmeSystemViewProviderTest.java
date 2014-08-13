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

package edu.cmu.rainbow_ui.display;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.common.DataValueSupport;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.ui.DummyTestUI;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.DummyDatabaseConnector;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;
import java.util.Date;
import org.junit.After;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * This class contains unit tests for testing the Acme System View Provider component
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class AcmeSystemViewProviderTest {

    SystemConfiguration sysConfig;
    IRuntimeAggregator runtimeAggregator;
    IDatabaseConnector databaseCon;
    AccessibleAcmeSystemViewProvider provider;

    @Before
    public void setUp() throws Exception {
        sysConfig = new SystemConfiguration(
                "system.properties");
        databaseCon = new DummyDatabaseConnector();
        runtimeAggregator = new MockAcmeRuntimeAggregator(
                sysConfig, databaseCon);
        DummyTestUI ui = new DummyTestUI();
        ui.init(null);
        provider = new AccessibleAcmeSystemViewProvider(
                runtimeAggregator, ui, sysConfig);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test the creation of an acmesystemviewprovider
     */
    @Test
    public void createSVP() {
        assertNotNull(provider.getHistoryProvider());
        assertNotNull(provider.getTimer());
        assertNotNull(provider.getSystemConfig());
        assertNotNull(provider.getEventStore());
        assertNotNull(provider.getUi());
        assertEquals(true, provider.isCurrent());
        assertEquals(sysConfig.getUpdateRate(), provider.getRefreshRate());
    }

    /**
     * Test switching between current and historical views
     */
    @Test
    public void setUseHistoricalAndCurrent() {
        /**
         * TODO: fix here. there is no data available at this time point.
         */
        Date date = new Date();
        try {
            provider.setUseHistorical(date);
        } catch (SystemViewProviderException ex) {
            // Expected exception since Dummy DBC returns null for latest snapshot
        }
        
        assertEquals(true, provider.isCurrent());

        provider.setUseCurrent();
        assertEquals(true, provider.isCurrent());
        assertNull(provider.getHistoricalTime());
    }


    /**
     * Test getting a value from a mapping
     */
    @Test
    public void getValue() throws AcmeConversionException, ValueConversionException {
        DataValue value = provider.getValue("Znn.Server0.load");
        assertEquals(new Float(0.0), DataValueSupport.converter.to_java(value, Float.class));

        value = provider.getValue("Znn.Server0.fidelity");
        assertEquals(new Integer(5), DataValueSupport.converter.to_java(value, Integer.class));
    }

}
