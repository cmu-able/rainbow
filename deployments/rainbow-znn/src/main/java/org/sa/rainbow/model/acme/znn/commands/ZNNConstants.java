package org.sa.rainbow.model.acme.znn.commands;

import java.util.Collections;
import java.util.List;

/**
 * Created by schmerl on 2/4/2016.
 */
public class ZNNConstants {
    public static final String SERVER_T_NAME             = "ServerT";
    public static final String PROXY_CONN_T_NAME         = "ProxyConnT";
    public static final String PROXY_FORWARD_PORT_T_NAME = "ProxyForwardPortT";
    public static final String HTTP_PORT_T_NAME          = "HttpPortT";

    static final List<String> SERVER_TYPE        = Collections.singletonList (SERVER_T_NAME);
    static final List<String> HTTP_CONN_T        = Collections.singletonList (PROXY_CONN_T_NAME);
    static final List<String> PROXY_FORWARD_PORT = Collections.singletonList (PROXY_FORWARD_PORT_T_NAME);
    static final List<String> HTTP_PORT          = Collections.singletonList (HTTP_PORT_T_NAME);
}
