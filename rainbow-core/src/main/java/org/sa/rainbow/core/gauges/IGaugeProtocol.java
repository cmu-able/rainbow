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
/**
 * Created December 7, 2006.
 */
package org.sa.rainbow.core.gauges;


/**
 * This interface defines common strings and commands required for the Gauge Protocol.
 * <p>
 * There are eleven Gauge Protocol Actions:
 * <ul>
 * <li>CREATE - Creates a Gauge at a defined <em>location</em> of the given type and name, for the given model type and
 * name.<br>
 * This must be the initial protocol message for any Gauge, and the REPORT_CREATED message received before the other
 * actions make sense.
 * <li>REPORT_CREATED - A Gauge has been created, a <em>uid</em> is returned, with a <em>beacon period</em>.
 * <li>DELETE - Deletes a Gauge, destroying it.
 * <li>REPORT_DELETED - A Gauge has been deleted, no more report should come from it.
 * <li>CONFIGURE - Configures a Gauge of <em>uid</em> using a set of propertyName-propertyValue pairs.
 * <li>REPORT_CONFIGURED - A Gauge has been configured, with particular <em>uid</em>.
 * <li>SUBSCRIBE - Subcribe to a Gauge of a particular <em>uid</em> for a specified <em>duration</em>.
 * <li>UNSUBSCRIBE - Unsubscribe from a Gauge of a particular <em>uid</em>.<br>
 * After the specified subscription duration expires, unsubscription automatically occurs.
 * <li>ISSUE_COMMAND - A command representation issued by the gauge, with <em>uid</em>, <em>gauge-type</em>,
 * <em>gauge-name</em>, <em>model-type</em>, and <em>model-name</em>.
 * <li>ISSUE_COMMANDS - A list of commands (with values) issued by the Gauge, with <em>uid</em>, <em>gauge-type</em>,
 * <em>gauge-name</em>, <em>model-type</em>, and <em>model-name</em>.
 * <li>BEACON - A beacon of <em>uid</em>, <em>gauge-type</em>, <em>gauge-name</em>, <em>model-type</em>,
 * <em>model-name</em> is sent by the Gauge.<br>
 * If this message is not received within the beacon period, the Gauge is considered to have failed/died.
 * </ul>
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IGaugeProtocol {

    public static final String ID = "uid";
    public static final String UID                = "luid";
    public static final String GAUGE_TYPE = "gaugeType";
    public static final String GAUGE_NAME = "gaugeName";
    public static final String MODEL_TYPE = "modelType";
    public static final String MODEL_NAME = "modelName";
    public static final String LOCATION = "location";
    public static final String SUBSCRIPTION_DUR = "subsDur";
    public static final String BEACON_PERIOD = "beaconPer";
    public static final String SETUP_PARAM = "setupParam";  // name[:type][=value]
    public static final String CONFIG_PARAM = "configParam";  // name[:type][=value]
    public static final String META_CONFIG_PARAM = "metaConfigParam";  // name:type
    public static final String META_VALUE = "metaValue";  // name:type
    public static final String SIZE              = "_size";
    public static final String GAUGE_HEARTBEAT   = "gauge-heartbeat";
    public static final String CONFIG_PARAM_VALUE = "configParamValue";
    public static final String CONFIG_PARAM_TYPE  = "configParamType";
    public static final String CONFIG_PARAM_NAME  = "configParam";
    public static final String GAUGE_CONFIGURED = "GAUGE_CONFIGURED";
    public static final String GAUGE_DELETED = "GAUGE_DELETED";
    public static final String GAUGE_CREATED = "GAUGE_CREATED";

}
