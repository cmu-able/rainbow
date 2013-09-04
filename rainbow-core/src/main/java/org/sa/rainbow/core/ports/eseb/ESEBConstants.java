package org.sa.rainbow.core.ports.eseb;

public interface ESEBConstants {

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
    public static final String TARGET                                = ESEB_PREFIX + "TARGET";
    public static final String PROPKEY_ESEB_COMMAND_CLASS            = ESEB_PREFIX + "_COMMAND_CLASS";
    public static final String MSG_TYPE_UPDATE_MODEL                 = ESEB_PREFIX + "_UPDATE_MODEL";

    public static final String COMMAND_PARAMETER_KEY                 = ESEBConstants.ESEB_PREFIX + "parameter_";
    public static final String COMMAND_TARGET_KEY                    = ESEBConstants.ESEB_PREFIX + "target";
    public static final String MODEL_TYPE_KEY                        = ESEBConstants.ESEB_PREFIX + "modelType";
    public static final String COMMAND_NAME_KEY                      = ESEBConstants.ESEB_PREFIX + "commandName";
    public static final String MODEL_NAME_KEY                        = ESEBConstants.ESEB_PREFIX + "modelName";
    public static final String MSG_CHANNEL_KEY                       = ESEB_PREFIX + "CHANNEL";
    public static final String MSG_SENT                              = ESEB_PREFIX + "msg-sent";
    public static final String MSG_TYPE_PROBE_REPORT                 = ESEB_PREFIX + "PROBE_REPORT";
    public static final String MSG_PROBE_ID_KEY                      = ESEB_PREFIX + "probe_id";
    public static final String MSG_DATA_KEY                          = ESEB_PREFIX + "data";

}
