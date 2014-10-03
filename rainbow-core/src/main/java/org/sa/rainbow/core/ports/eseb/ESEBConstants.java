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
    public static final String REPORT_TYPE_KEY                       = ESEB_PREFIX + "REPORT_TYPE";
    public static final String MSG_TYPE_UI_REPORT                    = ESEB_PREFIX + "UI_REPORT";
    public static final String REPORT_MSG_KEY                        = ESEB_PREFIX + "MSG";
    public static final String MSG_PROBE_LOCATION_KEY                = ESEB_PREFIX + "probe_location";
    public static final String MSG_PROBE_TYPE_KEY                    = ESEB_PREFIX + "probe_type";
    public static final String COMPONENT_TYPE_KEY                    = ESEB_PREFIX + "component_type";
    public static final String REPORT_MSG_ADDITIONAL_INFO            = ESEB_PREFIX + "ADDITIONAL";
    public static final String MSG_UPDATE_MODEL_REPLY                = ESEB_PREFIX + "UPDATE_MODEL_REPLY";
    public static final String MSG_TRANSACTION                       = ESEB_PREFIX + "TRANSACTION";
    public static final String COMMAND_ORIGIN                        = ESEB_PREFIX + "ORIGIN";

}
