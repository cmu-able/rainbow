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

import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

import java.util.Collection;

/**
 * The interface through which a gauge can be queried.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeQueryPort extends IDisposablePort {
    /**
     * Returns the entire state of this Gauge via the supplied lists.
     * 
     * @return A gauge state representing all the setup and config params of the gauge, as well as issued commands
     * 
     */
    IGaugeState queryGaugeState ();

    /**
     * Queries for a command identified by the command name.
     * 
     * @param commandName
     *            the name of the command to get information for
     * @return A representation of the command, including the model it affects, the target, and the parameters last
     *         issued
     */
    IRainbowOperation queryCommand (String commandName);

    /**
     * Queries for all of the commands reported by this Gauge.
     * 
     * @return Collection<IRainbowModelCommandRepresentation> A collection of all the commands last issued, one per
     *         command command mapping, including the target and parameters used.
     */
    Collection<IRainbowOperation> queryAllCommands ();


}
