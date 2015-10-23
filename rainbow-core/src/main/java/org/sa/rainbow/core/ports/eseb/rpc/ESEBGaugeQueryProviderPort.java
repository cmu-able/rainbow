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
package org.sa.rainbow.core.ports.eseb.rpc;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import java.io.IOException;
import java.util.Collection;

public class ESEBGaugeQueryProviderPort extends AbstractESEBDisposableRPCPort implements IESEBGaugeQueryRemoteInterface {

    private IGauge           m_gauge;

    public ESEBGaugeQueryProviderPort (IGauge gauge) throws IOException, ParticipantException {

        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), gauge.id ());
        m_gauge = gauge;
        getConnectionRole().createRegistryWrapper (IESEBGaugeQueryRemoteInterface.class, this, gauge.id ()
                + IESEBGaugeQueryRemoteInterface.class.getSimpleName ());
    }

    @Override
    public Collection<IRainbowOperation> queryAllCommands () {
        return m_gauge.queryAllCommands ();

    }

    @Override
    public IRainbowOperation queryCommand (String commandName) {
        return m_gauge.queryCommand (commandName);
    }

    @Override
    public IGaugeState queryGaugeState () {
        return m_gauge.queryGaugeState ();
    }

}
