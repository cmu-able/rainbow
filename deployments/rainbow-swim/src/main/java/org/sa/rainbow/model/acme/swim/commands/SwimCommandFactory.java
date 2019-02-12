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
package org.sa.rainbow.model.acme.swim.commands;

import java.io.InputStream;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import incubator.pval.Ensure;

public class SwimCommandFactory extends AcmeModelCommandFactory {


    public static SwimLoadModelCommand loadCommand (ModelsManager modelsManager,
                                                   String modelName,
                                                   InputStream stream,
                                                   String source) {
        return new SwimLoadModelCommand (modelName, modelsManager, stream, source);
    }

    public SwimCommandFactory (AcmeModelInstance modelInstance) {
        super (modelInstance);
    }

    @Override
    protected void fillInCommandMap () {
        super.fillInCommandMap ();
        m_commandMap.put("SetDimmer".toLowerCase (), SetDimmerCmd.class);
        m_commandMap.put("SetLoad".toLowerCase (), SetLoadCmd.class);
        m_commandMap.put("SetArrivalRate".toLowerCase (), SetArrivalRateCmd.class);
        m_commandMap.put("SetBasicResponseTime".toLowerCase (), SetBasicResponseTimeCmd.class);
        m_commandMap.put("SetOptResponseTime".toLowerCase (), SetOptResponseTimeCmd.class);
        m_commandMap.put("SetBasicThroughput".toLowerCase (), SetBasicThroughputCmd.class);
        m_commandMap.put("SetOptThroughput".toLowerCase (), SetOptThroughputCmd.class);
        m_commandMap.put("SetOptThroughput".toLowerCase (), SetOptThroughputCmd.class);
        m_commandMap.put("EnableServer".toLowerCase (), EnableServerCmd.class);
        m_commandMap.put("ActivateServer".toLowerCase (), ActivateServerCmd.class);
        m_commandMap.put("AddServer".toLowerCase (), AddServerCmd.class);
        m_commandMap.put("RemoveServer".toLowerCase (), RemoveServerCmd.class);
    }


    public SetDimmerCmd setDimmerCmd (IAcmeComponent loadBalancer, double dimmer) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetDimmerCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (dimmer));
    }

    public SetLoadCmd setLoadCmd (IAcmeComponent server, double value) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLoadCmd ((AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                   Double.toString (value));
    }

    public SetArrivalRateCmd setArrivalRateCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetArrivalRateCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    public SetBasicResponseTimeCmd setBasicResponseTimeCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetBasicResponseTimeCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    public SetOptResponseTimeCmd setOptResponseTimeCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetOptResponseTimeCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    public SetBasicThroughputCmd setBasicThroughputCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetBasicThroughputCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    public SetOptThroughputCmd setOptThroughputCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetOptThroughputCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    public EnableServerCmd enableServerCmd (IAcmeComponent server, boolean enabled) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new EnableServerCmd ((AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                    Boolean.toString (enabled));
    }

    public ActivateServerCmd activateServerCmd (IAcmeComponent server, boolean enabled) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new ActivateServerCmd ((AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                    Boolean.toString (enabled));
    }

    public AddServerCmd addServerCmd (IAcmeComponent loadBalancer, IAcmeComponent server) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new AddServerCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
        		server.getQualifiedName());
    }

    public RemoveServerCmd removeServerCmd (IAcmeComponent loadBalancer, IAcmeComponent server) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new RemoveServerCmd ((AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
        		server.getQualifiedName());
    }

}
