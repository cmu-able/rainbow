# Rainbow Orange Specification and Deployment Guide.

January 2015

## Introduction

Rainbow is a self-adaptive framework that can be used to provide self-adaptation capabilities to an existing system. Rainbow adds a closed control loop on top of a system, and then monitors and effects changes on the system based on observations. 

To place Rainbow on top of a system requires customizing it with information about the system that it is managing. This includes:

* Information about the deployment layout of the system. This is required so that Rainbow knows which machines to run on.

* A model of the system. This is specified most commonly as an Acme model of the software architecture of the system, and a set of commands that change properties in the architecture (useful for analysis) and structural aspects of the system (useful for changes)

* The analyses to be done on the system. This is a class that is used to examine the models and derive information that can be used to select an adaptation strategy.

* Gauges. The monitors used get information out of the system.

* Strategies and Tactics: Scripts used to make changes to the system.

* Utilities and preferences. Information used to decide which adaptations to perform.

This document describes how to define and install these elements.

## Customizing Rainbow to a New System

### Defining a target

To customize Rainbow to work on a particular system, a target needs to be defined and placed in the targets directory of Rainbow. The structure of a target is as follows:

| File/Directory | Description |
|---------------------------|----------------------------|
| lib/                      | This contains the additional JAR files that are needed for probes, effectors, and gauges. Any JAR files in here will be automatically added to the classpath. |
| model/                    | This contains model-level information about Rainbow, including the Acme model of the system being managed, and mappings between architectural operators and effectors. |
| system/                   | This contains the elements of the system that get used by Rainbow. In this directory, the probes and effectors are defined. | 
| stitch/                   | This contains the strategies, tactics, and utility profiles for repairs. |
| rainbow.properties | This file defines the various configuration properties for Rainbow, including the location and ports of the various Rainbow services, the location of the Rainbow master, where gauges, etc. configuration files are located, and additional host locations. These properties are read into Rainbow on startup and can be accessed through the Rainbow.property method in the code. |




### Adding a model

Rainbow can manage multiple models, including models of the utilities, models of the system, and models of the environment. Rainbow models are specified in the rainbow.properties file, with the following applicable properties:

*rainbow.model.number:* Specifies the number of models to be loaded into Rainbow on startup

*rainbow.model.path_<#>*: The target relative path to the model file. # is a number that is < rainbow.model.number-1, and indicates which model the properties correspond to. If no path is specified, then the loading command should construct an empty model

*rainbow.model.load.class_<#>*: Indicates the model command factory that will be used to load the model

*rainbow.model.name_<#>*: The name for the model in the models manager.

*rainbow.model.saveOnClose_<#>*: (Optional) Indicates whether the model should be saved when Rainbow closes

*rainbow.model.saveLocation_<#>*: (Optional) Indicates the target relative path for the model to be saved in. If this property is not specified, but saveOnClose is, then the path is used instead.

Rainbow can handle models of different types (not just Acme models as in previous versions). Adding a new model requires implementing a number of Rainbow framework classes. 

* IModelInstance<T>: Provides the framework wrapper around models that are to be used in Rainbow. The IModelInstance provides methods to enable the ModelsManager to manage the model, including copying, deleting, and attribute information about the source, type, and name of the model.

* ModelCommandFactory<T>: Models in Rainbow are updated via operations, or commands. This allows the framework to manage transactions on the model, know when commands fail, announce events based on commands, and allow gauges, analyses, and adaptations to request model changes. Model instances contains a command factory so that commands can be created. The ModelsManager is usually the only element that executes the commands: other components may create commands and request that the ModelsManager execute them (by announcing them on the Model Upstream Bus). A command factory must provide the following operations:

    * loadCommand(ModelsManager mm, String modelName, InputStream str, String source): A static method that is used by the ModelsManager to create a new ModelInstance. The ModelsManager looks for this method through reflection on the command factory referred to in the rainbow.properties file above.

    * saveCommand: A method that will produce a command that saves the model when Rainbow exits.

    * generateCommand:  Given a command name and a set of arguments (where the first argument is usually the target), return the appropriate model operation. This method looks in the command map to find the appropriate implementation of IRainbowModelOperation to construct and return. If it is not found in the command map, reflection is used to search for methods on the command factory that match the name of the operation and constructs the operation based on the return type of this method. If an operation is called "xxx", reflection looks for a method called xxxCmd.

* AbstractRainbowModelOperation<T>: An operation that can be executed on models of type T. It should implement methods to execute the operation, as well as a constructor that renders the target and arguments of the command as Strings (so that they can easily be constructed from serialization in Rainbow). 

These framework classes are related as below.

![image alt text](imgs/image_0.png)

### Adding a new probe

Probes are instruments in the running system. There are two types of probes supported by Rainbow: script based probes and Java probes. In both cases, the probes.yml file needs to be updated with the new probe information. The probe information consists of the following fields:

|     |    |
|-----|----|
| <pre>probes:</pre> |   |
| <pre> NewProbe:</pre> |  The name of the probe  |
| <pre>    alias: xxx</pre> | The name that the probe as seen by the gauge. Gauges will put this in the targetProbeType field |
| <pre>    location:</pre>  | The location where the probe is deployed |
| <pre>    type:</pre>      |   Can be `java` or `script` |
| <pre>    javaInfo:</pre> | For java probes, this element is used |
| <pre>      class:</pre>  |   The class of the probe, implementing IProbe |
| <pre>      period:</pre> |   The  reporting period for the probe |
| <pre>      args.length</pre> | The number of arguments that will be passed to the constructor of the probe |
| <pre>      args.<n></pre> | The arguments to be passed, n=0..args.length-1 |
| <pre>    scriptInfo:</pre> | For script probes, this element is used |
| <pre>      path:</pre> |   The path of the script, which should exist on “location” |
| <pre>      argument:</pre> |   The argument to pass to the script |

For Java-based probes, the java class should be on the classpath. This means creating a JAR and placing it on the classpath.** **The JAR file can be placed in the target’s lib directory. For script-based probes, the path needs to exist on the machine. 

### Adding a new Gauge

While probes provide information about the system, the intent of gauges is to abstract this system information into architectural information, in the form of commands against the model. There are three kinds of gauges in the Rainbow system: gauges that receive information from probes; gauges that receive information from other gauges; and gauges that generate information without either of these (for example, time-based gauges or diagnostic gauges). 

In all cases, gauges must be specified in the gauges.yml file located in the models directory. There are two sections that are required in this file: the gauge-types section, which defines the kinds of gauges that the system can support, and the gauge-instances section that says which instances Rainbow should create, how they are attached to the model, and what (if any) probes they listen to. For example, a gauge type could be defined for reporting the processing time property for servers. The type would define what values (e.g., processing-time) are reported by the gauge, how to set up the gauge initially (e.g., the period of reporting), and how to configure the gauge when it is running (e.g., by changing the reporting units from seconds to milliseconds). The instance specification specifies where a particular gauge instance runs, what part of the model it is attached to, what probes it is listening to, etc.

The format of the gauge-type portion of the gauges.yml spec are:

| | |
|-|-|
| <pre>gauge-types:</pre> | |
| <pre>  NewGaugeT:</pre> | The name of the gauge type. |
| <pre>    commands:</pre> | The command names and command signature that will be reported by the gauge. The command signature is of the form <Type>.<command>(<type>...). Valid types are String, long, double, boolean, and sets of these specified by surrounding the type in {..}. Commands should be defined in the model. |
| <pre>    setupParams:</pre> |  The parameters used when the gauge is constructed. It is possible to define your own setup parameters, given values in the instance, but the required ones are: |
| <pre>      targetIP:</pre><pre>        type: String</pre><pre>        default: "localhost"</pre> |     Where a gauge instance will be run. Here the default is localhost. |
| <pre>      beaconPeriod:</pre> <pre>        type: long</pre><pre>        default: <value></pre> | How often the gauge will send a report of its liveness to rainbow, in ms |
| <pre>      javaClass:</pre><pre>        type: String</pre><pre>        default: <value></pre> |  The javaClass that implements the gauge. It should extend AbstractProbelessGauge or AbstractGauge (if it listens to probes) |
| <pre>    configParams:</pre> | The parameters used to configure the gauge, with values given in the instance. Gauges are configured in Rainbow when all the expected target locations have been created. It is possible to define your own parameters, but Rainbow understands the ones below: |
| <pre>      targetProbeType:</pre><pre>        type: String:</pre><pre>        default: ~</pre> | The probe that the gauge will listen to. This will need to be the name specified in the “alias” of the probe. |
| <pre>      targetProbeList:</pre><pre>        type: String</pre><pre>        default: ~</pre>  |  If the gauge listens to more than one probe, their aliases are specified in the comma separated list of this config param |
| <pre>      samplingPeriod</pre><pre>        type: long</pre><pre>        value: <value></pre> |     How often, in ms, the gauge will report a value. |


The instance specification for a gauge specifies its type, the model it is attached to, the mappings of values to properties on the model, and the values for any setup and configuration parameters.

| | |
|-|-|
|<pre>gauge-instances:</pre> | |
|<pre>  GaugeName:</pre> | The name that the gauge will have |
|<pre>    type: xxx</pre>      |   Which gauge type this gauge is an instance of |
|<pre>    model: xxx</pre>      | The model to which the gauge will be attached. E.g., “ZNewsSys.acme”. This will be an Acme model specified in the target. |
|<pre>    commands:</pre><pre>      "value": element.command(params)</pre> | For each command that the gauge issues, which instance and parameters should it be issued with? element: the architectural element instance (of the type specified in the gauge type) to issue the command against. This can be either a fully qualified model name, or a pattern understood by the gauge. The parameters are a list of values for each parameter of the operation. Parameters of the form ${...} are replaced by rainbow properties. Parameters of the form $<...> are replaced at runtime by the gauge. |
|<pre>    setupValues:</pre><pre>      setupParam: value</pre>|   For each setup value specified in the type, give the value to setup. If a value is not specified, then the default value specified in the type is used. |
|<pre>    configValues:</pre><pre>      configParam: value</pre> | Like the setup values, a value is specified for each config value in the type, otherwise the default value is used. |        


Gauges are only implemented using Java in the current framework. The javaClass specified in the type must be on the classpath for the target system. To implement the gauge, the following class hierarchy is provided by the Rainbow Infrastructure:

AbstractGauge: Implements basic Gauge management, such as heartbeat, configuration, setting up, etc. Gauges that do not listen to any probes should implement this class

AbstractGaugeWithProbes extends AbstractGauge: A gauge that handles the targetProbeType and targetProbeList configuration parameters. 

RegularExpressionGauge extends AbstractGaugeWithProbes: A gauge that processes probe reports that match one or more regular expressions. The constructor for the gauge should specify the regular expression patterns that are to be matched, and the gauge provides a default runAction that calls "doMatch" when a one of the expressions is matched. Extenders must implement this doMatch method to report the value.

### Adding a new Effector

### Adding Stitch

The little tricks necessary to do so, and the location of the reference documentation to create the stitch queries 

### Adding a new Architecture Evaluation

This is new... 

