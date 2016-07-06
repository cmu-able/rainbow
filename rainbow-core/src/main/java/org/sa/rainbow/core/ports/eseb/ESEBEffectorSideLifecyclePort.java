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
package org.sa.rainbow.core.ports.eseb;


import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.effectors.IEffectorProtocol;

import java.io.IOException;
import java.util.List;

public class ESEBEffectorSideLifecyclePort extends AbstractESEBDisposablePort implements IEffectorLifecycleBusPort {


    public ESEBEffectorSideLifecyclePort () throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.HEALTH);
    }

    @Override
    public void reportCreated (IEffectorIdentifier effector) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_CREATED);
        setCommonEffectorProperties (effector, msg);
        getConnectionRole().publish (msg);
    }

    private void setCommonEffectorProperties (IEffectorIdentifier effector, RainbowESEBMessage msg) {
        msg.setProperty (IEffectorProtocol.ID, effector.id ());
        msg.setProperty (IEffectorProtocol.SERVICE, effector.service ());
        msg.setProperty (IEffectorProtocol.KIND, effector.kind ().name ());
        msg.setProperty (IEffectorProtocol.LOCATION, Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION));
    }

    @Override
    public void reportDeleted (IEffectorIdentifier effector) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_DELETED);
        setCommonEffectorProperties (effector, msg);
        getConnectionRole().publish (msg);
    }

    @Override
    public void reportExecuted (IEffectorIdentifier effector, Outcome outcome, List<String> args) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_EXECUTED);
        setCommonEffectorProperties (effector, msg);
        msg.setProperty (IEffectorProtocol.OUTCOME, outcome.name ());
        msg.setProperty (IEffectorProtocol.ARGUMENT + IEffectorProtocol.SIZE, args.size ());
        for (int i = 0; i < args.size (); i++) {
            msg.setProperty (IEffectorProtocol.ARGUMENT + i, args.get (i));
        }
        getConnectionRole().publish (msg);
    }


}
