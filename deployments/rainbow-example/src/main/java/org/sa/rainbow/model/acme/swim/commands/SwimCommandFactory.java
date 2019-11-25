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
import org.sa.rainbow.core.error.RainbowException;
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

    public SwimCommandFactory (AcmeModelInstance modelInstance) throws RainbowException {
        super (modelInstance);
    }

    public static final String SET_DIMMER_CMD = "SetDimmer";
    public static final String SET_LOAD_CMD = "SetLoad";
    public static final String SET_ARRIVAL_RATE_CMD = "SetArrivalRate";
    public static final String SET_BASIC_RESPONSE_TIME_CMD = "SetBasicResponseTime";
    public static final String SET_OPT_RESPONSE_TYPE_CMD = "SetOptResponseTime";
    public static final String SET_BASIC_THROUGHPUT_CMD = "SetBasicThroughput";
    public static final String SET_OPT_THROUGHPUT_CMD = "SetOptThroughput";
    public static final String ENABLE_SERVER_CMD = "EnableServer";
    public static final String ACTIVATE_SERVER_CMD = "ActivateServer";
    public static final String ADD_SERVER_CMD = "AddServer";
    public static final String REMOVE_SERVER_CMD = "RemoveServer";
    
    @Operation(name=SET_DIMMER_CMD)
    public SetDimmerCmd setDimmerCmd (IAcmeComponent loadBalancer, double dimmer) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetDimmerCmd (SET_DIMMER_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (dimmer));
    }
    
    @Operation(name = SET_LOAD_CMD)
    public SetLoadCmd setLoadCmd (IAcmeComponent server, double value) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLoadCmd (SET_LOAD_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                   Double.toString (value));
    }

    @Operation(name = SET_ARRIVAL_RATE_CMD)
    public SetArrivalRateCmd setArrivalRateCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetArrivalRateCmd (SET_ARRIVAL_RATE_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    @Operation(name = SET_BASIC_RESPONSE_TIME_CMD)
    public SetBasicResponseTimeCmd setBasicResponseTimeCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetBasicResponseTimeCmd (SET_BASIC_RESPONSE_TIME_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }
    
    @Operation(name=SET_OPT_RESPONSE_TYPE_CMD)
    public SetOptResponseTimeCmd setOptResponseTimeCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetOptResponseTimeCmd (SET_OPT_RESPONSE_TYPE_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    @Operation(name=SET_BASIC_THROUGHPUT_CMD)
    public SetBasicThroughputCmd setBasicThroughputCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetBasicThroughputCmd (SET_BASIC_THROUGHPUT_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    @Operation(name=SET_OPT_THROUGHPUT_CMD)
    public SetOptThroughputCmd setOptThroughputCmd (IAcmeComponent loadBalancer, double value) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetOptThroughputCmd (SET_OPT_THROUGHPUT_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
                                   Double.toString (value));
    }

    @Operation(name=ENABLE_SERVER_CMD)
    public EnableServerCmd enableServerCmd (IAcmeComponent server, boolean enabled) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new EnableServerCmd (ENABLE_SERVER_CMD,(AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                    Boolean.toString (enabled));
    }

    @Operation(name=ACTIVATE_SERVER_CMD)
    public ActivateServerCmd activateServerCmd (IAcmeComponent server, boolean enabled) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new ActivateServerCmd (ACTIVATE_SERVER_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                    Boolean.toString (enabled));
    }

    @Operation(name=ADD_SERVER_CMD)
    public AddServerCmd addServerCmd (IAcmeComponent loadBalancer, IAcmeComponent server) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new AddServerCmd (ADD_SERVER_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
        		server.getQualifiedName());
    }

    @Operation(name=REMOVE_SERVER_CMD)
    public RemoveServerCmd removeServerCmd (IAcmeComponent loadBalancer, IAcmeComponent server) {
        Ensure.is_true (loadBalancer.declaresType ("LoadBalancerT"));
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (loadBalancer) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new RemoveServerCmd (REMOVE_SERVER_CMD, (AcmeModelInstance) m_modelInstance, loadBalancer.getQualifiedName (),
        		server.getQualifiedName());
    }

}
