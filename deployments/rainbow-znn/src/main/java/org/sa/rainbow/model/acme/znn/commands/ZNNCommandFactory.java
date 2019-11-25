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
package org.sa.rainbow.model.acme.znn.commands;

import incubator.pval.Ensure;
import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.io.InputStream;
import java.util.Set;

import static incubator.pval.Ensure.isTrue;

public class ZNNCommandFactory extends AcmeModelCommandFactory {


    private static final String CREATE_DISCONNECTED_SERVER_CMD = "createDisconnectedServer";
	private static final String CONNECT_SERVER_CMD = "connectServer";
	private static final String SET_SYSTEM_PROPERTIES_CMD = "setSystemProperties";
	private static final String DELETE_CLIENT_CMD = "DeleteClient";
	private static final String ADD_CLIENT_CMD = "AddClient";
	private static final String SET_AUTHENTICATION_RESPONSE_CMD = "SetAuthenticationResponse";
	private static final String SET_CAPTCHA_RESPONSE_CMD = "SetCaptchaResponse";
	private static final String FORCE_REAUTHENTICATION_CMD = "forceReauthentication";
	private static final String SET_THROTTLED_CMD = "SetThrottled";
	private static final String SET_FIDELITY_CMD = "SetFidelity";
	private static final String ENABLE_SERVER_CMD = "EnableServer";
	private static final String SET_CLIENT_REQUEST_RATE_CMD = "SetClientRequestRate";
	private static final String SET_MALICIOUSNESS_CMD = "SetMaliciousness";
	private static final String SET_CAPTCHA_ENABLED_CMD = "SetCaptchaEnabled";
	private static final String SET_BLACKHOLED_CMD = "SetBlackholed";
	private static final String SET_NUM_SUCCESSFUL_REQUESTS_CMD = "SetNumSuccessfulRequests";
	private static final String SET_NUM_REQUESTS_SERVER_ERROR_CMD = "SetNumRequestsServerError";
	private static final String SET_NUM_REQUESTS_CLIENT_ERROR_CMD = "setNumRequestsClientError";
	private static final String SET_NUM_REDIRECTED_REQUESTS_CMD = "setNumRedirectedRequests";
	private static final String SET_LAST_PAGE_HIT_CMD = "setLastPageHit";
	private static final String SET_LATENCY_CMD = "setLatency";
	private static final String SET_LATENCY_RATE_CMD = "setLatencyRate";
	private static final String SET_BYTE_SERVICE_RATE_CMD = "setByteServiceRate";
	private static final String SET_REQ_SERVICE_RATE_CMD = "setReqServiceRate";
	private static final String DELETE_SERVER_CMD = "deleteServer";
	private static final String CONNECT_NEW_SERVER_CMD = "connectNewServer";
	private static final String SET_LOAD_CMD = "setLoad";
	private static final String SET_RESPONSE_TIME_CMD = "setResponseTime";

	@LoadOperation
	public static ZNNLoadModelCommand loadCommand (ModelsManager modelsManager,
                                                   String modelName,
                                                   InputStream stream,
                                                   String source) {
        return new ZNNLoadModelCommand (modelName, modelsManager, stream, source);
    }

    public ZNNCommandFactory (AcmeModelInstance modelInstance) throws RainbowException {
        super (modelInstance);
    }

    @Operation(name=CREATE_DISCONNECTED_SERVER_CMD)
    public CreateDisconnectedServerCmd createDiconnectedServer (IAcmeSystem system, String name) {
        if (ModelHelper.getAcmeSystem (system) != m_modelInstance.getModelInstance ()) {
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        }
        return new CreateDisconnectedServerCmd (CREATE_DISCONNECTED_SERVER_CMD, (AcmeModelInstance) m_modelInstance, system.getName (), name);
    }

    @Operation(name=CONNECT_SERVER_CMD)
    public ConnectServerCmd connectServer (IAcmeComponent lb, String name, String deploymentLocation,
                                           String port) {
        if (ModelHelper.getAcmeSystem (lb) != m_modelInstance.getModelInstance ()) {
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        }
        return new ConnectServerCmd (CONNECT_SERVER_CMD, (AcmeModelInstance) m_modelInstance, lb.getQualifiedName (), name,
                                     deploymentLocation, port);
    }

    @Operation(name=SET_SYSTEM_PROPERTIES_CMD)
    public SetSystemPropertiesCmd setSystemProperties (IAcmeSystem sys, float avgRt, float perMalicious, boolean
            highResponseTime, boolean aboveMalicious) {
        if (ModelHelper.getAcmeSystem (sys) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new SetSystemPropertiesCmd (SET_SYSTEM_PROPERTIES_CMD,(AcmeModelInstance) m_modelInstance, sys.getQualifiedName (), Float
                .toString (avgRt), Float.toString (perMalicious), Boolean.toString (highResponseTime), Boolean
                                                   .toString (aboveMalicious));
    }

    @Operation(name=SET_RESPONSE_TIME_CMD)
    public SetResponseTimeCmd setResponseTimeCmd (IAcmeComponent client, float rt) {
        assert client.declaresType ("ClientT");
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetResponseTimeCmd (SET_RESPONSE_TIME_CMD, (AcmeModelInstance) m_modelInstance,
                                       client.getQualifiedName (), Float.toString (rt));
    }

    @Operation(name=SET_LOAD_CMD)
    public SetLoadCmd setLoadCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLoadCmd (SET_LOAD_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                               Float.toString (load));
    }

    @Operation(name=SET_LATENCY_RATE_CMD)
    public SetLatencyRateCmd setLatencyRateCmd (IAcmeConnector conn, float latencyRate) {
        assert conn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (conn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLatencyRateCmd (SET_LATENCY_RATE_CMD, (AcmeModelInstance) m_modelInstance, conn.getQualifiedName (),
                                      Float.toString (latencyRate));
    }

    @Operation(name=SET_LAST_PAGE_HIT_CMD)
    public SetLastPageHitCmd setLastPageHitCmd (IAcmeComponent server, String page) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLastPageHitCmd (SET_LAST_PAGE_HIT_CMD, (AcmeModelInstance) m_modelInstance,
                                      server.getQualifiedName (), page);
    }

    @Operation(name=SET_CLIENT_REQUEST_RATE_CMD)
    public SetClientRequestRateCmd setClientRequestRateCmd (IAcmeComponent client, float reqRate) {
        Ensure.is_true (client.declaresType ("ClientT"));
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetClientRequestRateCmd (SET_CLIENT_REQUEST_RATE_CMD, (AcmeModelInstance) m_modelInstance,
                                            client.getQualifiedName (), Float.toString (reqRate));
    }

    @Operation(name=SET_BYTE_SERVICE_RATE_CMD)
    public SetByteServiceRateCmd setByteServiceRateCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetByteServiceRateCmd (SET_BYTE_SERVICE_RATE_CMD, (AcmeModelInstance) m_modelInstance,
                                          server.getQualifiedName (),
                                          Float.toString (load));
    }

    @Operation(name=SET_REQ_SERVICE_RATE_CMD)
    public SetReqServiceRateCmd setReqServiceRateCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetReqServiceRateCmd (SET_REQ_SERVICE_RATE_CMD, (AcmeModelInstance) m_modelInstance,
                                         server.getQualifiedName (),
                                         Float.toString (load));
    }

    @Operation(name=SET_LATENCY_CMD)
    public SetLatencyCmd setLatencyCmd (IAcmeConnector httpConn, float latency) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLatencyCmd (SET_LATENCY_CMD, (AcmeModelInstance) m_modelInstance, httpConn.getQualifiedName (),
                                  Float.toString (latency));
    }

    @Operation(name=SET_NUM_REDIRECTED_REQUESTS_CMD)
    public SetNumRedirectedRequestsCmd setNumRedirectedRequestsCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumRedirectedRequestsCmd (SET_NUM_REDIRECTED_REQUESTS_CMD, (AcmeModelInstance) m_modelInstance,
                                                httpConn.getQualifiedName (),
                                                Float.toString (serviceRate));
    }

    @Operation(name=SET_NUM_REQUESTS_CLIENT_ERROR_CMD)
    public SetNumRequestsClientErrorCmd setNumRequestsClientErrorCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumRequestsClientErrorCmd (SET_NUM_REQUESTS_CLIENT_ERROR_CMD, (AcmeModelInstance) m_modelInstance,
                                                 httpConn.getQualifiedName (),
                                                 Float.toString (serviceRate));
    }

    @Operation(name=SET_NUM_REQUESTS_SERVER_ERROR_CMD)
    public SetNumRequestsServerErrorCmd setNumRequestsServerErrorCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumRequestsServerErrorCmd (SET_NUM_REQUESTS_SERVER_ERROR_CMD, (AcmeModelInstance) m_modelInstance,
                                                 httpConn.getQualifiedName (),
                                                 Float.toString (serviceRate));
    }

    @Operation(name=SET_NUM_SUCCESSFUL_REQUESTS_CMD)
    public SetNumSuccessfulRequestsCmd setNumSuccessfulRequestsCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumSuccessfulRequestsCmd (SET_NUM_SUCCESSFUL_REQUESTS_CMD, (AcmeModelInstance) m_modelInstance,
                                                httpConn.getQualifiedName (),
                                                Float.toString (serviceRate));
    }

    @Operation(name=SET_CAPTCHA_ENABLED_CMD)
    public SetCaptchaEnabledCmd setCaptchaEnabledCmd (IAcmeComponent lb, boolean enabled) {
        assert lb.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (lb) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetCaptchaEnabledCmd (SET_CAPTCHA_ENABLED_CMD, (AcmeModelInstance) m_modelInstance,
                                         lb.getQualifiedName (),
                                         Boolean.toString (enabled));
    }
    
    @Operation(name=SET_BLACKHOLED_CMD)
    public SetBlackholedCmd setBlackholedCmd (IAcmeComponent server, Set<String> blackholdIps) {
        assert server.declaresType ("BlackholerT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        StringBuilder sb = new StringBuilder ();
        if (!blackholdIps.isEmpty ()) {
            for (String ip : blackholdIps) {
                sb.append (ip);
                sb.append (",");
            }
            sb.deleteCharAt (sb.length () - 1);
        }
        return new SetBlackholedCmd (SET_BLACKHOLED_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                     sb.toString ());
    }

    @Operation(name=SET_THROTTLED_CMD)
    public SetThrottledCmd setThrottledCmd (IAcmeComponent server, Set<String> throttledIPs) {
        isTrue ("Server should declare the type 'ThrottlerT'", server.declaresType ("ThrottlerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        StringBuilder sb = new StringBuilder ();
        if (!throttledIPs.isEmpty ()) {
            for (String ip : throttledIPs) {
                sb.append (ip);
                sb.append (",");
            }
            sb.deleteCharAt (sb.length () - 1);
        }
        return new SetThrottledCmd (SET_THROTTLED_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                    sb.toString ());
    }

    @Operation(name=FORCE_REAUTHENTICATION_CMD)
    public ForceReauthenticationCmd forceReauthenticationCmd (IAcmeComponent server) {
        isTrue ("server should declare the type 'ServerT'", server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new ForceReauthenticationCmd (FORCE_REAUTHENTICATION_CMD, (AcmeModelInstance) m_modelInstance,
                                             server.getQualifiedName ());
    }

    @Operation(name=SET_MALICIOUSNESS_CMD)
    public SetMaliciousnessCmd setMaliciousnessCmd (IAcmeComponent client, float maliciousness) {
        Ensure.is_true (client.declaresType ("PotentiallyMaliciousT"));
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetMaliciousnessCmd (SET_MALICIOUSNESS_CMD, (AcmeModelInstance) m_modelInstance,
                                        client.getQualifiedName (),
                                        Float.toString (maliciousness));
    }

    @Operation(name=CONNECT_NEW_SERVER_CMD)
    public NewServerCmd connectNewServerCmd (IAcmeComponent proxy,
                                             String name,
                                             String deploymentLocation,
                                             String port) {
        assert proxy.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (proxy) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new NewServerCmd (CONNECT_NEW_SERVER_CMD, (AcmeModelInstance) m_modelInstance, proxy.getQualifiedName (),
                                 name,
                                 deploymentLocation, port);
    }

    @Operation(name=DELETE_SERVER_CMD)
    public RemoveServerCmd deleteServerCmd (IAcmeComponent server) {
        assert server.declaresType ("ServerT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot delete a server that is not part of this model");
        return new RemoveServerCmd (DELETE_SERVER_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName ());
    }

    @Operation(name=ENABLE_SERVER_CMD)
    public EnableServerCmd enableServerCmd (IAcmeComponent server, boolean enabled) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new EnableServerCmd (ENABLE_SERVER_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                    Boolean.toString (enabled));
    }

    public SetFidelityCmd setFidelityCmd (IAcmeComponent server, int fidelity) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetFidelityCmd (ENABLE_SERVER_CMD, (AcmeModelInstance) m_modelInstance, server.getQualifiedName (),
                                   Integer.toString (fidelity));
    }

    @Operation(name=SET_CAPTCHA_RESPONSE_CMD)
    public SetCaptchaResponseCmd setCaptchaResponseCmd (IAcmeComponent client, int response) {
        Ensure.is_true (client.declaresType ("CaptchaHandlerT"));
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetCaptchaResponseCmd (SET_CAPTCHA_RESPONSE_CMD, (AcmeModelInstance) m_modelInstance,
                                          client.getQualifiedName (),
                                          Integer.toString (response));
    }
    
    @Operation(name=SET_AUTHENTICATION_RESPONSE_CMD)
    public SetAuthenticationResponseCmd setAuthenticationResponseCmd (IAcmeComponent client, int response) {
        Ensure.is_true (client.declaresType ("AuthenticationHandlerT"));
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetAuthenticationResponseCmd (SET_AUTHENTICATION_RESPONSE_CMD, (AcmeModelInstance) m_modelInstance,
                                                 client.getQualifiedName (), Integer.toString (response));
    }

    @Operation(name=ADD_CLIENT_CMD)
    public AddClientCmd addClientCmd (IAcmeSystem sys, IAcmeComponent lb, String deploymentLocation) {
        Ensure.is_true (lb.declaresType ("ProxyT"));
        if (ModelHelper.getAcmeSystem (lb) != m_modelInstance.getModelInstance ()) throw new IllegalArgumentException (
                "Cannot create a command for a new client that is not part of the system");
        return new AddClientCmd (ADD_CLIENT_CMD, (AcmeModelInstance) m_modelInstance, sys.getQualifiedName (),
                                 lb.getQualifiedName (),
                                 deploymentLocation);
    }

    @Operation(name=DELETE_CLIENT_CMD)
    public RemoveClientCmd deleteClientCmd (IAcmeSystem sys, IAcmeComponent client) {
        assert client.declaresType ("ClientT");
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot delete a server that is not part of this model");
        return new RemoveClientCmd (DELETE_CLIENT_CMD, (AcmeModelInstance) m_modelInstance, sys.getQualifiedName (),
                                    client.getQualifiedName ());
    }


}
