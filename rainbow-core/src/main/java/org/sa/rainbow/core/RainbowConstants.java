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
 * Created April 1, 2007.
 */
package org.sa.rainbow.core;

/**
 * This interface holds all the important literal value definitions of Rainbow in one place.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface RainbowConstants {

    /** Exit value to indicate self-destruct. */
    int EXIT_VALUE_DESTRUCT = 0;
    /** Exit value to indicate restart. */
    int EXIT_VALUE_RESTART = 8;
    /** Exit value to indicate sleep. */
    int EXIT_VALUE_SLEEP = 10;
    /** Exit value to indicate ABORT. */
    int EXIT_VALUE_ABORT = -9;
//
//    /** Rainbow Service Port number. */
//    public static final int RAINBOW_SERVICE_PORT = 9210;
//    /** Sets the byte arrays to the usual buffer size of 8 KB. */
//    public static final int MAX_BYTES = 8*1024;
    /**
     * This number determines how many times the Rainbow Runtime will tolerate unexpected errors before propagating it
     * up, e.g., to the user.
     */
    int MAX_ERROR_CNT = 10;
//
//    /** Usual newline sequence of character. */
//    public static final String NEWLINE = "\n";
//    /** Windows newline sequence of characters. */
//    public static final String NEWLINE_WIN = "\r\n";
//    public static final String USCORE = "_";

//    /** The path name of the Rainbow deployment directory storing the binary JARs. */
//    public static final String RAINBOW_BIN_DIR = "drops";
    /** The path name of the Rainbow deployment directory storing contributed java jars. */
    String RAINBOW_JAR_CONTRIB_DIR = "lib";
    /** The path name of the Rainbow deployment directory storing update files. */
    String RAINBOW_UPDATE_DIR = ".update";

    /** The template name of the master Rainbow runtime configuration file */
    String CONFIG_FILE_TEMPLATE = "rainbow-<host.suffix>.properties";
    /** The string portion of the Rainbow config file name to replace per host */
    String CONFIG_FILE_STUB_NAME = "<host.suffix>";
    /** The path name of the runtime deployment directory storing target configurations. */
    String RAINBOW_CONFIG_PATH = "targets";
    /** The default name of the master Rainbow runtime configuration file */
    String DEFAULT_CONFIG_FILE = "rainbow.properties";
    /** The default target configuration path name */
    String DEFAULT_TARGET_NAME = "default";
    /** Property key for the Rainbow configuration base path */
    String PROPKEY_BIN_PATH = "rainbow.binary";
    /** Property key for the Rainbow configuration base path */
    String PROPKEY_CONFIG_PATH = "rainbow.config";
    /** Property key for the Rainbow configuration file */
    String PROPKEY_CONFIG_FILE = "rainbow.propfile";
    /** Property key for the Rainbow configuration target name */
    String PROPKEY_TARGET_NAME = "rainbow.target";
    /** Property key for the Rainbow target configuration path */
    String PROPKEY_TARGET_PATH = "rainbow.path";

    /** The logger level of details */
    String PROPKEY_LOG_LEVEL = "logging.level";
    /** Directory path of the event log files, mainly for queue-based implementation */
    String PROPKEY_EVENT_LOG_PATH = "event.log.path";
    /** The ending segment(s) of the log path */
    String PROPKEY_LOG_PATH = "logging.path";
    /** Data log file name */
    String PROPKEY_DATA_LOG_PATH = "monitoring.log.path";
    /** The log output pattern */
    String PROPKEY_LOG_PATTERN = "logging.pattern";
    /** The maximum size of any log file */
    String PROPKEY_LOG_MAX_SIZE = "logging.max.size";
    /** The maximum number of backups when rotating log files */
    String PROPKEY_LOG_MAX_BACKUPS = "logging.max.backups";

    /** Hostname or IP at which the current deployment is located, important for IDs */
    String PROPKEY_DEPLOYMENT_LOCATION = "rainbow.deployment.location";
    /** Hostname of the location of the Master Rainbow component */
    String PROPKEY_MASTER_LOCATION = "rainbow.master.location.host";
    /** Rainbow service port */
    String PROPKEY_SERVICE_PORT = "rainbow.service.port";
    /** RMI port of the location of the Master Rainbow component, only useful if RMI event is used */
    String PROPKEY_MASTER_LOCATION_PORT = "rainbow.master.location.port";
//    /** Deployment {@link org.sa.rainbow.core.Rainbow.Environment <code>Environment</code>} */
    String PROPKEY_DEPLOYMENT_ENVIRONMENT = "rainbow.deployment.environment";
//    /** Rainbow event service name */
//    public static final String PROPKEY_EVENT_SERVICE = "rainbow.event.service";
//    /** JMS contact factory class name */
//    public static final String PROPKEY_EVENT_CONTEXT_FACTORY = "event.context.factory";
//    /** URL of the JMS server */
//    public static final String PROPKEY_EVENT_PROVIDER_URL = "event.provider.url";
//    /** Java Naming package prefixes */
//    public static final String PROPKEY_EVENT_URL_PREFIXES = "event.url.prefixes";

    /** Rainbow customization parameter: Path to the Rainbow-Acme model */
    String PROPKEY_MODEL_NUMBER = "rainbow.model.number";
    String PROPKEY_MODEL_PATH_PREFIX = "rainbow.model.path_";
    String PROPKEY_MODEL_CLASS_PREFIX = "rainbow.model.class_";
    String PROPKEY_MODEL_NAME_PREFIX = "rainbow.model.name_";
    String PROPKEY_MODEL_TYPE_PREFIX = "rainbow.model.type_";
    String PROPKEY_MODEL_LOAD_CLASS_PREFIX = "rainbow.model.load.class_";
    String PROPKEY_MODEL_SAVE_PREFIX = "rainbow.model.saveOnClose_";
    String RAINBOW_MODEL_SAVE_LOCATION_PREFIX = "rainbow.model.saveLocation_";

    String PROPKEY_MODEL_PATH = "customize.model.path";
    /** Rainbow customization parameter: Flag indicating whether to persist model at end of run */
    String PROPKEY_MODEL_PERSIST = "customize.model.persist";
    /** Rainbow customization parameter: Path to the Rainbow-Environment model */
    String PROPKEY_ENV_MODEL_PATH = "customize.env.model.path";
    /**
     * The default Rainbow-Environment model path that is used if none is specified for {@link PROPKEY_MODEL_PATH}
     */
    String DEFAULT_ENV_MODEL = "model/TargetEnv.acme";
//    /** Rainbow customization parameter:  Amount of time to wait between each model evaluation */
    String PROPKEY_MODEL_EVAL_PERIOD = "customize.model.evaluate.period";
    String PROPKEY_ARCH_EVALUATOR_EXTENSIONS = "rainbow.evaluations";
    String PROPKEY_ANALYSIS_COMPONENTS = "rainbow.analyses";
    String PROPKEY_ANALYSIS_COMPONENT_SIZE = "rainbow.analyses.size";
//    /** Rainbow customization parameter:  Alpha value of exponential average for
//     *  model properties, expAvg = (1 - alpha) * expAvg + alpha * newVal */
    String PROPKEY_MODEL_ALPHA = "customize.model.expavg.alpha";
//    /** Rainbow customization parameter: Gauge Description file path */
    String PROPKEY_GAUGES_PATH = "customize.gauges.path";
//    /** Rainbow customization parameter: Probe Description file path */
    String PROPKEY_PROBES_PATH = "customize.probes.path";
//    /** Rainbow customization parameter:  Path to the architectural operator mapping file */
//    public static final String PROPKEY_OP_MAP_PATH = "customize.archop.map.path";
//    /** Rainbow customization parameter: Effector Description file path */
    String PROPKEY_EFFECTORS_PATH = "customize.effectors.path";
//    /** Rainbow customization parameter:  Path to the Stitch scripts */
    String PROPKEY_SCRIPT_PATH = "customize.scripts.path";
//    /** Rainbow customization parameter:  Path to the file for persiting Tactic execution time profile */
//    public static final String PROPKEY_TACTIC_PROFILE_PATH = "customize.profile.tactic.duration.path";
    /** Rainbow customization parameter: Utility Preference Description file path */
    String PROPKEY_UTILITY_PATH = "customize.utility.path";
//    /** Rainbow customization parameter:  The utility dimension that represents
//     *  Strategy failure utility, set this only if tracking failure count */
    String PROPKEY_TRACK_STRATEGY = "customize.utility.trackStrategy";
//    /** Rainbow customization parameter:  Identifier of utility scenario to use */
    String PROPKEY_SCENARIO = "customize.utility.scenario";
//    /** Rainbow customization parameter:  The minimum threshold of utility score below which a strategy is not considered */
    String PROPKEY_UTILITY_MINSCORE_THRESHOLD = "customize.utility.score.minimum.threshold";
//    /** Rainbow customization parameter:  Duration into the future to predict for utility computation */
    String PROPKEY_UTILITY_PREDICTION_DURATION = "customize.utility.prediction.duration";
//    /** Rainbow customization parameter:  Flag to turn on prediction capability */
    String PROPKEY_ENABLE_PREDICTION = "customize.prediction.enable";

    /** Rainbow customization parameter: System target host location */
    String PROPKEY_TARGET_LOCATION = "customize.system.target";
    /** Rainbow customization parameter: Simulation info, set to simulation path if using simulation */
    String PROPKEY_SIM_PATH = "customize.system.sim.path";
    /** Rainbow customization parameter: System architecture element locations **/
    String PROPKEY_ACME_ELEMENT_LOCATIONS = "customize.acme.components";

    String PROPKEY_DELEGATE_BEACONPERIOD = "rainbow.delegate.beaconperiod";
    String PROPKEY_DELEGATE_ID = "rainbow.delegate.id";
    String PROPKEY_PORT_FACTORY = "rainbow.deployment.factory.class";
    String PROPKEY_MASTER_DEPLOYMENT_PORT = "rainbow.master.deployment.port";
    String PROPKEY_MASTER_CONNECTION_PORT = "rainbow.master.connection.port";
    String PROPKEY_WAIT_FOR_GAUGES = "rainbow.waitforallgauges";

    String PROPKEY_ADAPTATION_MANAGER_CLASS = "rainbow.adaptation.manager.class";
    String PROPKEY_ADAPTATION_MANAGER_SIZE = "rainbow.adaptation.manager.size";
    String PROPKEY_ADAPTATION_MANAGER_MODEL = "rainbow.adaptation.manager.model";
    String PROPKEY_ADAPTATION_EXECUTOR_CLASS = "rainbow.adaptation.executor.class";
    String PROPKEY_ADAPTATION_EXECUTOR_SIZE = "rainbow.adaptation.executor.size";
    String PROPKEY_ADAPTATION_EXECUTOR_MODEL = "rainbow.adaptation.executor.model";
    String PROPKEY_EFFECTOR_MANAGER_COMPONENT_SIZE = "rainbow.effector.manager.size";
    String PROPKEY_EFFECTOR_MANAGER_COMPONENT = "rainbow.effector.manager.class";

    String PROPKEY_PORT_TIMEOUT = "rainbow.port.timeout";
}
