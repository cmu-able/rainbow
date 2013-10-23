package org.sa.rainbow.model.acme.znn.commands;

import incubator.pval.Ensure;

import java.io.InputStream;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommand;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class ZNNCommandFactory extends AcmeModelCommandFactory {



    public static ZNNLoadModelCommand loadCommand (ModelsManager modelsManager,
            String modelName,
            InputStream stream,
            String source) {
        return new ZNNLoadModelCommand (modelName, modelsManager, stream, source);
    }

    public ZNNCommandFactory (AcmeModelInstance modelInstance) {
        super (modelInstance);
    }

    @Override
    protected void fillInCommandMap () {
        super.fillInCommandMap ();
        m_commandMap.put ("setResponseTime".toLowerCase (), SetResponseTimeCmd.class);
        m_commandMap.put ("setLoad".toLowerCase (), SetLoadCmd.class);
        m_commandMap.put ("connectServer".toLowerCase (), NewServerCmd.class);
        m_commandMap.put ("setReqServiceRate".toLowerCase (), SetReqServiceRateCmd.class);
        m_commandMap.put ("setByteServiceRate".toLowerCase (), SetByteServiceRateCmd.class);
        m_commandMap.put ("setLatencyRate".toLowerCase (), SetLatencyRateCmd.class);
        m_commandMap.put ("setLatency".toLowerCase (), SetLatencyCmd.class);
        m_commandMap.put ("setLastPageHit".toLowerCase (), SetLastPageHitCmd.class);
        m_commandMap.put ("setNumRedirectedRequests".toLowerCase (), SetNumRedirectedRequestsCmd.class);
        m_commandMap.put ("setNumRequestsClientError".toLowerCase (), SetNumRequestsClientErrorCmd.class);
        m_commandMap.put ("SetNumRequestsServerError".toLowerCase (), SetNumRequestsServerErrorCmd.class);
        m_commandMap.put ("SetNumSuccessfulRequests".toLowerCase (), SetNumSuccessfulRequestsCmd.class);
        m_commandMap.put ("SetBlackholed".toLowerCase (), SetBlackholedCmd.class);
        m_commandMap.put ("SetCaptchaEnabled".toLowerCase (), SetCaptchaEnabledCmd.class);
        m_commandMap.put ("SetMaliciousness".toLowerCase (), SetMaliciousnessCmd.class);
        m_commandMap.put ("EnableServer".toLowerCase (), EnableServerCmd.class);
        m_commandMap.put ("SetFidelity".toLowerCase (), SetFidelityCmd.class);
        m_commandMap.put ("SetThrottled".toLowerCase (), SetThrottledCmd.class);
        m_commandMap.put ("ForceReauthentication".toLowerCase (), ForceReauthenticationCmd.class);
    }


    public SetResponseTimeCmd setResponseTimeCmd (IAcmeComponent client, float rt) {
        assert client.declaresType ("ClientT");
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        SetResponseTimeCmd cmd = new SetResponseTimeCmd ("setResponseTime", m_modelInstance,
                client.getQualifiedName (), Float.toString (rt));
        return cmd;
    }

    public SetLoadCmd setLoadCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLoadCmd ("setLoad", m_modelInstance, server.getQualifiedName (),
                Float.toString (load));
    }

    public SetLatencyRateCmd setLatencyRateCmd (IAcmeConnector conn, float latencyRate) {
        assert conn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (conn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLatencyRateCmd ("setLoad", m_modelInstance, conn.getQualifiedName (),
                Float.toString (latencyRate));
    }

    public SetLastPageHitCmd setLastPageHitCmd (IAcmeComponent server, String page) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLastPageHitCmd ("setLoad", m_modelInstance, server.getQualifiedName (), page);
    }

    public SetByteServiceRateCmd setByteServiceRateCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetByteServiceRateCmd ("setLoad", m_modelInstance, server.getQualifiedName (), Float.toString (load));
    }

    public SetReqServiceRateCmd setReqServiceRateCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetReqServiceRateCmd ("setLoad", m_modelInstance, server.getQualifiedName (), Float.toString (load));
    }


    public SetLatencyCmd setLatencyCmd (IAcmeConnector httpConn, float latency) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLatencyCmd ("setReqServiceRate", m_modelInstance, httpConn.getQualifiedName (),
                Float.toString (latency));
    }
    public SetNumRedirectedRequestsCmd setNumRedirectedRequestsCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumRedirectedRequestsCmd ("setReqServiceRate", m_modelInstance, httpConn.getQualifiedName (),
                Float.toString (serviceRate));
    }

    public SetNumRequestsClientErrorCmd setNumRequestsClientErrorCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumRequestsClientErrorCmd ("setReqServiceRate", m_modelInstance, httpConn.getQualifiedName (),
                Float.toString (serviceRate));
    }

    public SetNumRequestsServerErrorCmd setNumRequestsServerErrorCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumRequestsServerErrorCmd ("setReqServiceRate", m_modelInstance, httpConn.getQualifiedName (),
                Float.toString (serviceRate));
    }

    public SetNumSuccessfulRequestsCmd setNumSuccessfulRequestsCmd (IAcmeConnector httpConn, float serviceRate) {
        assert httpConn.declaresType ("HttpConnT");
        if (ModelHelper.getAcmeSystem (httpConn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetNumSuccessfulRequestsCmd ("setReqServiceRate", m_modelInstance, httpConn.getQualifiedName (),
                Float.toString (serviceRate));
    }

    public SetCaptchaEnabledCmd setCaptchaEnabledCmd (IAcmeComponent lb, boolean enabled) {
        assert lb.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (lb) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetCaptchaEnabledCmd ("setReqServiceRate", m_modelInstance, lb.getQualifiedName (),
                Boolean.toString (enabled));
    }

    public AcmeModelCommand<IAcmeProperty> setBlackholedCmd (IAcmeComponent server, Set<String> blackholdIps) {
        assert server.declaresType ("BlackholerT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        StringBuffer sb = new StringBuffer ();
        for (String ip : blackholdIps) {
            sb.append (ip);
            sb.append (",");
        }
        sb.deleteCharAt (sb.length () - 1);
        return new SetBlackholedCmd ("setBlackholed", m_modelInstance, server.getQualifiedName (),
                sb.toString ());
    }

    public AcmeModelCommand<IAcmeProperty> setThrottledCmd (IAcmeComponent server, Set<String> throttledIPs) {
        Ensure.isTrue (server.declaresType ("ThrottlerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        StringBuffer sb = new StringBuffer ();
        for (String ip : throttledIPs) {
            sb.append (ip);
            sb.append (",");
        }
        sb.deleteCharAt (sb.length () - 1);
        return new SetThrottledCmd ("setThrottled", m_modelInstance, server.getQualifiedName (), sb.toString ());
    }

    public AcmeModelCommand<IAcmeProperty> forceReauthentication (IAcmeComponent server) {
        Ensure.isTrue (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new ForceReauthenticationCmd ("forceReauthentication", m_modelInstance, server.getQualifiedName ());
    }

    public AcmeModelCommand<IAcmeProperty> setMaliciousnessCmd (IAcmeComponent client, float maliciousness) {
        Ensure.is_true (client.declaresType ("PotentiallyMaliciousT"));
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetMaliciousnessCmd ("setMaliciousness", m_modelInstance, client.getQualifiedName (),
                Float.toString (maliciousness));
    }

    public AcmeModelCommand<IAcmeComponent> connectNewServerCmd (IAcmeComponent proxy, String name) {
        assert proxy.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (proxy) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new NewServerCmd ("connectServer", m_modelInstance, proxy.getQualifiedName (), name);
    }

    public AcmeModelCommand<IAcmeProperty> enableServerCmd (IAcmeComponent server, boolean enabled) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new EnableServerCmd ("enableServer", m_modelInstance, server.getQualifiedName (),
                Boolean.toString (enabled));
    }

    public AcmeModelCommand<IAcmeProperty> setFidelityCmd (IAcmeComponent server, int fidelity) {
        Ensure.is_true (server.declaresType ("ServerT"));
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetFidelityCmd ("setFidelity", m_modelInstance, server.getQualifiedName (),
                Integer.toString (fidelity));
    }





}
