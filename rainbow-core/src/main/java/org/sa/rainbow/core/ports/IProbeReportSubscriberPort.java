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

public interface IProbeReportSubscriberPort extends IDisposablePort {

    /**
     * Indicate interest in reports from a particular probe, indicated by type and location. This is cumulative, i.e.,
     * if the port is already subscribed to a probe and this method is called with a different probe, then the original
     * probe(s) will still report information.
     * 
     * @param probeType
     *            The type of the probe to express interest in
     * @param location
     *            The location of the probe to express interest in. If this is null, then probes of the indicated type
     *            reporting from any location are subscribed to.
     */
    void subscribeToProbe (String probeType, String location);

    /**
     * Indicates that reports should no longer be received from the indicated probe at the location, through this port
     * 
     * @param probeType
     *            The type of the probe no longer interested in
     * @param location
     *            The location of the probe no longer interested in. If the original subscription specified null for
     *            this probe (meaning it was interested in all locations), then probes at all other locations will still
     *            be subscribed to. If this value is null, then probe types from all locations will be unsubscribed.
     */
    void unsubscribeToProbe (String probeType, String location);


}
