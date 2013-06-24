package org.sa.rainbow.ports.eseb;

public class ESEBConstants {

    public static final String ESEB_PREFIX                           = "__ESEB_";
    public static final String MSG_TYPE_KEY                          = ESEB_PREFIX + "MSG_TYPE";
    public static final String MSG_DELEGATE_ID_KEY                   = ESEB_PREFIX + "DID";
    public static final String PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT = "eseb.delegate.deployment.port";
    public static final String PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST = "eseb.delegate.deployment.host";
    public static final String PROPKEY_ESEB_DELEGATE_CONNECTION_PORT = "eseb.delegate.connection.port";
    public static final String MSG_TYPE_CONNECT_DELEGATE             = ESEB_PREFIX + "CONNECT_DELEGATE";
    public static final String PROPKEY_ESEB_REPLY_HOST               = ESEB_PREFIX + "REPLY_HOST";
    public static final String PROPKEY_ESEB_REPLY_PORT               = ESEB_PREFIX + "REPLY_PORT";
    public static final String MSG_REPLY_KEY                         = ESEB_PREFIX + "REPLY_KEY";
    public static final String MSG_TYPE_DISCONNECT_DELEGATE          = ESEB_PREFIX + "DISCONNECT_DELEGATE";
    public static final String MSG_REPLY_OK                          = "OK";
    public static final String MSG_CONNECT_REPLY                     = ESEB_PREFIX + "CONNECT_REPLY";
    public static final String MSG_TYPE_REPLY                        = ESEB_PREFIX + "REPLY";
    public static final String MSG_REPLY_VALUE                       = ESEB_PREFIX + "REPLY_VALUE";

}
