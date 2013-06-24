/**
 * Created April 1, 2007.
 */
package org.sa.rainbow;


/**
 * This interface holds all the important literal value definitions of Rainbow
 * in one place.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface RainbowConstants {

    /** Exit value to indicate self-destruct. */
    public static final int EXIT_VALUE_DESTRUCT = 0;
    /** Exit value to indicate restart. */
    public static final int EXIT_VALUE_RESTART = 8;
    /** Exit value to indicate sleep. */
    public static final int EXIT_VALUE_SLEEP = 10;
    /** Exit value to indicate ABORT. */
    public static final int EXIT_VALUE_ABORT = -9;

    /** Rainbow Service Port number. */
    public static final int RAINBOW_SERVICE_PORT = 9210;
    /** Sets the byte arrays to the usual buffer size of 8 KB. */
    public static final int MAX_BYTES = 8*1024;
    /**
     * This number determines how many times the Rainbow Runtime will tolerate
     * unexpected errors before propagating it up, e.g., to the user.
     */
    public static final int MAX_ERROR_CNT = 10;

    /** Usual newline sequence of character. */
    public static final String NEWLINE = "\n";
    /** Windows newline sequence of characters. */
    public static final String NEWLINE_WIN = "\r\n";
    public static final String USCORE = "_";

    /** The path name of the Rainbow deployment directory storing the binary JARs. */
    public static final String RAINBOW_BIN_DIR = "drops";
    /** The path name of the Rainbow deployment directory storing contributed java jars. */
    public static final String RAINBOW_JAR_CONTRIB_DIR = "lib";
    /** The path name of the Rainbow deployment directory storing update files. */
    public static final String RAINBOW_UPDATE_DIR = ".update";

    /** The template name of the master Rainbow runtime configuration file */
    public static final String CONFIG_FILE_TEMPLATE = "rainbow-<host.suffix>.properties";
    /** The string portion of the Rainbow config file name to replace per host */
    public static final String CONFIG_FILE_STUB_NAME = "<host.suffix>";
    /** The path name of the runtime deployment directory storing target configurations. */
    public static final String RAINBOW_CONFIG_PATH = "targets";
    /** The default name of the master Rainbow runtime configuration file */
    public static final String DEFAULT_CONFIG_FILE = "rainbow.properties";
    /** The default target configuration path name */
    public static final String DEFAULT_TARGET_NAME = "default";
    /** Property key for the Rainbow configuration base path */
    public static final String PROPKEY_BIN_PATH = "rainbow.binary";
    /** Property key for the Rainbow configuration base path */
    public static final String PROPKEY_CONFIG_PATH = "rainbow.config";
    /** Property key for the Rainbow configuration file */
    public static final String PROPKEY_CONFIG_FILE = "rainbow.propfile";
    /** Property key for the Rainbow configuration target name */
    public static final String PROPKEY_TARGET_NAME = "rainbow.target";
    /** Property key for the Rainbow target configuration path */
    public static final String PROPKEY_TARGET_PATH = "rainbow.path";

    /** The logger level of details */
    public static final String PROPKEY_LOG_LEVEL = "logging.level";
    /** Directory path of the event log files, mainly for queue-based implementation */
    public static final String PROPKEY_EVENT_LOG_PATH = "event.log.path";
    /** The ending segment(s) of the log path */
    public static final String PROPKEY_LOG_PATH = "logging.path";
    /** Data log file name */
    public static final String PROPKEY_DATA_LOG_PATH = "monitoring.log.path";
    /** The log output pattern */
    public static final String PROPKEY_LOG_PATTERN = "logging.pattern";
    /** The maximum size of any log file */
    public static final String PROPKEY_LOG_MAX_SIZE = "logging.max.size";
    /** The maximum number of backups when rotating log files */
    public static final String PROPKEY_LOG_MAX_BACKUPS = "logging.max.backups";
    /** Hostname or IP at which the current deployment is located, important for IDs */
    public static final String PROPKEY_DEPLOYMENT_LOCATION = "rainbow.deployment.location";
    /** Hostname of the location of the Master Rainbow component */
    public static final String PROPKEY_MASTER_LOCATION = "rainbow.master.location.host";
    /** Rainbow service port */
    public static final String PROPKEY_SERVICE_PORT = "rainbow.service.port";
    /** RMI port of the location of the Master Rainbow component, only useful if RMI event is used */
    public static final String PROPKEY_MASTER_LOCATION_PORT = "rainbow.master.location.port";
    /** Deployment {@link org.sa.rainbow.core.Rainbow.Environment <code>Environment</code>} */
    public static final String PROPKEY_DEPLOYMENT_ENVIRONMENT = "rainbow.deployment.environment";
    /** Rainbow event service name */
    public static final String PROPKEY_EVENT_SERVICE = "rainbow.event.service";
    /** JMS contact factory class name */
    public static final String PROPKEY_EVENT_CONTEXT_FACTORY = "event.context.factory";
    /** URL of the JMS server */
    public static final String PROPKEY_EVENT_PROVIDER_URL = "event.provider.url";
    /** Java Naming package prefixes */
    public static final String PROPKEY_EVENT_URL_PREFIXES = "event.url.prefixes";

    /** Rainbow customization parameter: Path to the Rainbow-Acme model */
    public static final String PROPKEY_MODEL_PATH = "customize.model.path";
    /** Rainbow customization parameter: Flag indicating whether to persist model at end of run */
    public static final String PROPKEY_MODEL_PERSIST = "customize.model.persist";
    /** Rainbow customization parameter: Path to the Rainbow-Environment model */
    public static final String PROPKEY_ENV_MODEL_PATH = "customize.env.model.path";
    /** The default Rainbow-Environment model path that is used if none is
     *  specified for {@link PROPKEY_MODEL_PATH} */
    public static final String DEFAULT_ENV_MODEL = "model/TargetEnv.acme";
    /** Rainbow customization parameter:  Amount of time to wait between each model evaluation */
    public static final String PROPKEY_MODEL_EVAL_PERIOD = "customize.model.evaluate.period";
    /** Rainbow customization parameter:  Alpha value of exponential average for
     *  model properties, expAvg = (1 - alpha) * expAvg + alpha * newVal */
    public static final String PROPKEY_MODEL_ALPHA = "customize.model.expavg.alpha";
    /** Rainbow customization parameter: Gauge Description file path */
    public static final String PROPKEY_GAUGES_PATH = "customize.gauges.path";
    /** Rainbow customization parameter: Probe Description file path */
    public static final String PROPKEY_PROBES_PATH = "customize.probes.path";
    /** Rainbow customization parameter:  Path to the architectural operator mapping file */
    public static final String PROPKEY_OP_MAP_PATH = "customize.archop.map.path";
    /** Rainbow customization parameter: Effector Description file path */
    public static final String PROPKEY_EFFECTORS_PATH = "customize.effectors.path";
    /** Rainbow customization parameter:  Path to the Stitch scripts */
    public static final String PROPKEY_SCRIPT_PATH = "customize.scripts.path";
    /** Rainbow customization parameter:  Path to the file for persiting Tactic execution time profile */
    public static final String PROPKEY_TACTIC_PROFILE_PATH = "customize.profile.tactic.duration.path";
    /** Rainbow customization parameter: Utility Preference Description file path */
    public static final String PROPKEY_UTILITY_PATH = "customize.utility.path";
    /** Rainbow customization parameter:  The utility dimension that represents
     *  Strategy failure utility, set this only if tracking failure count */
    public static final String PROPKEY_TRACK_STRATEGY = "customize.utility.trackStrategy";
    /** Rainbow customization parameter:  Identifier of utility scenario to use */
    public static final String PROPKEY_SCENARIO = "customize.utility.scenario";
    /** Rainbow customization parameter:  The minimum threshold of utility score below which a strategy is not considered */
    public static final String PROPKEY_UTILITY_MINSCORE_THRESHOLD = "customize.utility.score.minimum.threshold";
    /** Rainbow customization parameter:  Duration into the future to predict for utility computation */
    public static final String PROPKEY_UTILITY_PREDICTION_DURATION = "customize.utility.prediction.duration";
    /** Rainbow customization parameter:  Flag to turn on prediction capability */
    public static final String PROPKEY_ENABLE_PREDICTION = "customize.prediction.enable";

    /** Rainbow customization parameter: System target host location */
    public static final String PROPKEY_TARGET_LOCATION = "customize.system.target";
    /** Rainbow customization parameter: Simulation info, set to simulation path if using simulation */
    public static final String PROPKEY_SIM_PATH = "customize.system.sim.path";
    /** Rainbow customization parameter: System architecture element locations **/
    public static final String PROPKEY_ACME_ELEMENT_LOCATIONS = "customize.acme.components";

    public static final String PROPKEY_DELEGATE_BEACONPERIOD       = "rainbow.delegate.beaconperiod";
    public static final String PROPKEY_DELEGATE_ID                 = "rainbow.delegate.id";
    public static final String PROPKEY_DEPLOYMENT_PORT_FACTORY     = "rainbow.deployment.factory.class";
    public static final String PROPKEY_MASTER_DEPLOYMENT_PORT      = "rainbow.master.deployment.port";
    public static final String PROPKEY_MASTER_CONNECTION_PORT      = "rainbow.master.connection.port";
}
