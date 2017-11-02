/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
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
package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.List;

/**
 * The API through which gauges report their lifeculce (from creation, configuration, to deletion)
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeLifecycleBusPort extends IDisposablePort {

    /**
     * Reports that a gauge has been created, giving it's id, type, and associated model
     * 
     * @param gauge
     */
    void reportCreated (IGaugeIdentifier gauge);

    /**
     * Reports that a gauge has been deleted, giving it's id, type, and associated model
     * 
     * @param gauge
     */
    void reportDeleted (IGaugeIdentifier gauge);

    /**
     * Reports that a gauge has been configured, along with the configuration parameters
     * 
     * @param gauge
     *            The gauge configured
     * @param configParams
     *            The parameters with which it was configured
     */
    void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams);

    /**
     * The method through which a gauge sends its heartbeat, or beacon, so that listeners can be sure that it is still
     * alive
     * 
     * @param gauge
     *            The gauge sending the beacon
     */
    void sendBeacon (IGaugeIdentifier gauge);

}
