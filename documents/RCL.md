# Rainbow Configuration Language and Editor

## Introduction

The Rainbow Configuration Language (RCL) is a DSL written using the [Xtext framework](https://www.eclipse.org/Xtext) 
that allows the definition of language compilation and Eclipse IDE support. RCL works 
with files that have the extension `.rbw`, and will attempt to compile these files 
into the specification files (YAML, properties) that are required by Rainbow target 
definitions. Previously, Rainbow target definitions involved writing these files by 
hand which was error prone for various reasons, not the least of which was that there 
is no consistency checking between these different files. For example, if a Gauge referred 
to a Probe, there was no guarantee that the probe was specified correctly in the target. 
These errors went undiscovered until errors occurred when running Rainbow.

With this new feature, consistency will be checked across the different `.rbw` files 
as long as they are imported, including checking references to Java classes that are on the classpath. 
This typechecking also happens when building and deploying targets using Maven, as 
is discussed in [Building Targets with Maven](#building-targets-with-maven). This document 
is organized as follows:

| Section   | Description   |
| --- | --- |
| [Target Definition](#target-definition-and-layout)  | Describes how to organize the 
target   |
| [RCL Syntax and Conventions](#features)  |  The Syntax of RCL |
| [Specifying Properties](#properties-files) | Specifying Rainbow Properties  |
| [Model Command Factories](#model-command-factory)  | Specifying (and generating) model command factories  |
| [Probes](#probe-specification) | How to define probes in the system |
| [Gauges](#gauge-specification) | How to specify gauges |
| [Effectors](#effector-specification) | How to specify effectors |
| [Stitch and Utilities](#stitch-and-utility-preferences) | Specifying adaptation with 
Stitch and utility preferences |
| [Installing the Eclipse plugin](#installing) | Using with Eclipse |
| [Building targets with Mavem](#building-targets-with-maven) | How to build the target 
using Maven |

## Target Definition and Layout

To get the most out of the Eclipse plugin, targets should be defined in Eclipse projects 
that have the implementation for the deployment (the Java classes implementing commands, 
adaptation managers, analyzer etc) on the classpath. This has led to the need to rethink 
the layout of target definitions. For target definitions to work inside of Eclipse, 
the Java project should be organized as follows:

```
+ Java project
+-- src/
  +-- main/
    +-- java/
```
        Contains the source code for the deployment
```
    +-- resources/
      +-- rbw/
        +-- <target>/
```
            This defines the files used to specify a target. 
             Can be given any names (and may be multiples)
```
          +-- model/
            +-- gauges.rbw
```
                Defines the gauges that are used for this target
```
          +-- stitch/
```
              This directory is optional but needed if you are using 
               Stitch-based adaptation
```
            +-- <strategy>.s
```
                Any file ending in .s is a Stitch file
```
            +-- utilities.rbw
```
                Specifies the Utility and Impact models for the target
```
          +-- system/
            +-- probes.rbw
```
                Specifies what probes are deployed and where
```
            +-- effectors.rbw
```
                Specifies what effectors are deployed and where
```
          +-- <properties.rbw>
```
             Defines the properties used to configure Rainbow
```
        +-- acme/
```
            The Acme files that specify the architecture for the system 
             of this deployment. (Only needed if Acme is being used.)
```
        +-- <command factories>.rbw
```
             Used to generate the command factories
```
```
If Command Factories are specified as above, then `src/main/java-gen` should be created 
and put on the build path in Eclipse. (Java files to be compiled into classes will 
be generated here.)

## Features

### General Syntax

A configuration file for the most part follows the conventions laid down by previous 
versions of Rainbow. Each file begins with a target name (specifying the name of the target 
it belongs to) and a set of import statements,  and ending with an export statement saying which file is generated as a result.
For example, the file [properties.rbw](../deployments/rainbow-example/src/main/resources/rbw/rainbow-example/properties.rbw)
 
```
target rainbow-example
import properties "model/gauges.rbw"

...

export * to "rainbow-gui.properties" 
```

The first line indicates that this is a file for the target `rainbow-example`. The second line imports properties defined in the file `model/gauges.rbw`. This means that
gauges defined in that file may be referred to in this file. The final line says to export all (`*`) properties to the file `rainbow-gui.properties`.

#### Property names and keywords

Names in RCL can be fully qualified. Unfortunately, because RCL also imports grammar elements from Stitch and Acme, 
it is often the case that names may clash with keywords. To overcome this, RCL uses the convention that if an element of a name is
a keyword, it can be preceded with a caret (`^`) which will then be ignored. So, for example, the property name:

```
def rainbow.deployment.^factory.class = ...
```

contains the keyword `factory`, but is exported as `rainbow.deployment.factory.class`. Admittedly this a a little messy, but it
does allow the incorporation of other grammars without a fear of name clashes. This can also be used when referring to Java classes. For example,
`acme` is a keyword in RCL and so to refer to the class `org.sa.rainbow.evaluator.acme.ArchEvaluator` you need to use `org.sa.rainbow.evaluator.^acme.ArchEvaluator`.


#### Referencing properties in other properties

Properties may be referenced inside strings and on their own. To reference a property in a string, surrond it with `«...»`, e.g.,

```
def event.log.path = "log"
def logging.path = "«event.log.path»/rainbow.out"
```

When referencing it outside a string, use double `««...»»`, e.g.,

```
def customize.system.^target.master = ««rainbow.deployment.location»»
```

To insert `«` type Ctrl-Shift-<, `»` with Ctrl-Shift-> (Cmd-Shift on MacOS). Also autocomplete 
will give these proposals.

### Acme and Stitch

Most of the work on Rainbow uses Stitch and architecture models represented by Acme in models and adaptation management and execution. RCL supports these languages out of the box.
The current support for these languages in the Eclipse editor is minimal. There is simple parsing and scoping, but no other typechecking. This allows RCL configuration files to reference
names defined in Acme and Stitch files, but little else. Tis may change in the future.

## Properties Files

The rest of this file contains definitions of properties for configuring Rainbow. The syntax for declaring a property is:

`def <propertyType>? <name> (= <value>)?`

There are eight types of properties that can be defined, and the type of the property indicates what subfields (if any) it should have as well as the contexts in which it can be referenced. There kinds of properties are:

| Keyword | Description |
| --- | --- |
| _none_ | This is a generic property that can have any kind of value. |
| model | Defines a Rainbow model, and needs to specify things like the command factory, how to load a model, etc.|
| analysis | Defines a Rainbow analysis. It needs to specify a class that implements `org.rainbow.core.analysis.IRainbowAnalysis`. Analyses provide information about about state of the models. |
| adaptation-manager | Defines a Rainbow adaptation manager, which chooses what to do if an adaptation is required. It must impliment `org.sa.rainbow.stitch.adaptation.IAdaptationManager`.|
| executor | Defines a Rainbow Executor, that executes adaptation plans generated by the adaptation manager. It must implement `org.sa.rainbow.core.adaptation.IAdaptationExecutor` |
| effector-manager | Defines the class used to manager Effectors. |
| utility | Defines the utility profile and scenarios (only useful if Stitch is used) |
| gui | Defines which class (and associated configuration options) to use for the Rainbow GUI. |


### General Properties

The Rainbow properties file defines properties for customizing the Rainbow deployment and architecture, as well as
properties that define the system that is being managed, like the location of components etc. These properties are used internally by Rainbow at startup, or may be used by 
components of Raibow (e.g., in models). If you look at [properties.rbw](../deployments/rainbow-example/src/main/resources/rbw/rainbow-example/properties.rbw), you may be able to
see that the file is divided into several sections, though this is done out of convention and is not mandated.

The first part is:

```
###
# Default values for location specific properties, meaning that, if the
# rainbow-<host>.properties file does not specify a value, the default value
# set here is used.
def rainbow.path # Default property defined by rainbow

### Utility mechanism configuration
#- Config for Log4J, with levels:  OFF,FATAL,ERROR,WARN,INFO,DEBUG,TRACE,ALL 
def logging.level = DEBUG
def event.log.path = "log"
def logging.path = "«event.log.path»/rainbow.out" 
def monitoring.log.path = "«event.log.path»/rainbow-data.log"
```

This section is unlikely to change, though you may want to change the logging level.

The next section specifies how Rainbow is being deployed and how different components should
communicate.

```
### Rainbow component customization
## Rainbow host info and communication infrastructure
#- Location information of the master and this deployment
def rainbow.master.location.host = "rainbow-example"
#- Location information of the deployed delegate
def rainbow.deployment.location = "rainbow-example"
#- default registry port; change if port-tunneling
def rainbow.master.location.^port = 1100
#- OS platform, supported modes are:  cygwin | linux
#  Use "cygwin" for Windows, "linux" for MacOSX
def rainbow.deployment.environment = "linux"

def rainbow.delegate.beaconperiod = 10000
def rainbow.deployment.^factory.class = org.sa.rainbow.core.ports.guava.GuavaRainbowPortFactory 

``` 

The first two properties need to contain the name or IP of the machine on which the Rainbow master will be running. Rainbow is composed of two main components - the Rainbow Master (or Oracle) which collates information from probes, stores the models, runs analysis and adaptation management, and manages the execution of adaptations.
There is also a Rainbow delegate, which is a component that manages running of probes, gauges, and effectors. There may be multiple instances of delegates running on different machines, thus enabling target systems to be 
probed and effected locally. Delegates need to know the location of the master, and this property is used to communicate that.

`def rainbow.master.location.^port` specifies which socket Rainbow opens up for communication.

`def rainbow.deployment.environment` specifies what OS is running the master, which helps with starting shell scripts. "linux" is currently the only one fully tested, and it should also be ok for using on MacOS.

`def rainbow.delegate.beaconperiod` specifies how often a delegate should send a heartbeat back to the master indicating it is alive. The Master will use this to make sure that Rainbow is deployed properly.

`def rainbow.deployment.^factory.class` specifies how Rainbow components should communicate. Currently, two options are supported:

1. `org.sa.rainbow.core.ports.guava.GuavaRainbowPortFactory` implements communication across a Guava Event Bus. It is intended to be used when Rainbow runs entirely on one machine.
2. `org.sa.rainbow.core.ports.eseb.ESEBRainbowPortFactory` implements a custom event bus that is used to communicate across machines using `rainbow.master.location.port`.

Skipping to the end of the file, this is where properties that are specific to the system being targeted, or where your own properties can be defined.

The following properties in most circumstances should not be changed:

```
## Translator customization
#- Gauge spec
def customize.gauges.path = "model/gauges.yml"
#- Probe spec
def customize.probes.path = "system/probes.yml"
#- Effector spec
def customize.effectors.path = "system/effectors.yml"
## Adaptation Manager
#- Directory of Stitch adaptation script
def customize.scripts.path = "stitch"
#- Utilities description file, Strategy evaluation config, and minimum score threshold
def customize.^utility.path = "stitch/utilities.yml"
def customize.^utility.trackStrategy = "uC"
def customize.^utility.score.minimum.threshold = 0.033
def customize.^utility.scenario = "scenario 1"

``` 

These properties specify where Rainbow should find information about probes, gauges, etc, that are now generated by RCL.
The last properties are to do with Stitch and utilities. The last three may be changed, depending on what you want tracked and what scenarion (defined now in `utilities.rbw` should be used).


The other properties towards the end (beginning with `customize` need to be customized to the target system that you are wanting Rainbow to manage. In the current example, they specify properties
associated with Rubis example.

### Models

Models are used in Rainbow to specify information that is kept by Rainbow about the 
managed (target) system and its environment. Models are updated by gauges using most 
often using information from probes and translating them into changes on models. Analyses 
can also update models. The adaptation manager can also query model information to help 
decide what adaptations to perform.

Models in Rainbow have two kinds of interface: a query interface that is used to read 
information from a model, and an operation interface that is used to update models. 
Operations on models are produced with command factories, specified by the rainbow configuration, 
and are managed by Rainbow. Rainbow publishes operations when models are updated so 
that interested components  can be notified about changes to models. Operations can also 
be undone by Rainbow so that if an operation fails, the model can be rolled back to 
the previous version.

To specify a model in RCL, you define a _model_ property, which may be referred to by 
other components such as gauges and effectors.

```
# Rainbow Acme model of SWIM
def model SwimSys= {
    ^type="Acme" 
    path="model/swim.acme"
    ^factory=««SWIM»»
    saveOnClose = true
    saveLocation="model/swim-post.acme" 
}
```

The `path` attribute indicates the file in the target that should be used to initialize the model. It is optional though most often should be specified. The model is loaded through the command factory, which is specified 
with the `factory` attribute. The factory attribute can either point to a factory specified in 
RCL  or a class that extends 
`org.sa.rainbow.core.models.commands.ModelCommandFactory` (see [Model Command Factory](#model_command_factory)).
The `type` indicates the type of the Model that should be registered with Rainbow. In this case, a model called `SwimSys` with the type `Acme` will be loaded into Rainbow.
If you want the model to be saved when Rainbow quits, you should specify this by setting `saveOnClose` and indicating where the model should be saved in `saveLocation`.  

### Other components

The other types of components are also specified by defining typed properties. For evaluators and effector managers, only the class needs to be specified. For adaptation managers and executors, the model on which they operate
also needs to specified, through reference to a model property. For example, 

```
def adaptation-manager AdaptationManager = {
    ^model=««SwimSys»»
    class=org.sa.rainbow.^stitch.adaptation.AdaptationManager
}
```

Specifies to start an Adaptation Manager with the name `AdaptationManager` using the
class `org.sa.rainbow.stitch.adaptation.AdaptationManager` and which is associated with the model
called `SwimSys`.

Similarly:

```
def executor StitchExecutor = {
    ^model=««SwimSys»»
    class=org.sa.rainbow.^stitch.adaptation.StitchExecutor
}
```

## Model Command Factories  
Model command factories are classes in Rainbow that are used to implement possible operations 
on models. They are used by gauges to update models, and they are used in executors 
to specify how to change the system - in this last case they are not performed directly 
on the model but are intercepted by effectors and translated into operations on the 
managed system. So, specifying a model command factory involves implementing/specifying 
two things: operations and command factories to create the operations.

### Implementing Rainbow Operations in Java

Operations in Rainbow need to implement the interface 
[org.sa.rainbow.core.models.commands.IRainbowModelOperation](../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/models/commands/IRainbowOperation.java). 
In practice, there are two possible classes that you will extend. 

1. If you are dealing with Acme models, you will extend 
   [org.sa.rainbow.model.acme.AcmeModelOpetation](../rainbow/rainbow-acme-model/src/main/java/org/sa/rainbow/model/acme/AcmeModelOperation.java), 
   which is a generic class where you specify the result of the operation. You will 
   need to specify doConstructCommand, which returns the Acme command (IAcmeCommand) 
   that will operate on the Acme model.

2. If you are defining your own model class, you will extend 
   [org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation](../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/models/commands/AbstractRainbowModelOperation.java).
   This is a generic class that takes two parameters: the class of the model object 
   managed by Rainbow and the type of the result of the operation. In most cases, you will need to specify methods for executing, redoing, and undoing 
the operations, as well as a method to ensure that the operation is valid on the model. 

For an example of an Acme operation, look at [org.sa.rainbow.model.acme.swim.commands.ActivateServerCmd](). 
For an example of a non-Acme operation, check out the operations in the _rainbow-utility-model_ 
project. 

### Specifying Command Factories

In prior versions of Rainbow, these classes had to be handwritten, which was often tedious
and error prone. With RCL, command factories can be specified and the Java class generated. 
If the model uses an existing command factory, then it should specify that class in 
the **factory** property. Otherwise the factory property will be a reference to a command 
factory specified in RCL.

```
model factory SWIM yields org.sa.rainbow.^model.^acme.swim.commands.SwimCommandFactory {
    extends org.sa.rainbow.^model.^acme.AcmeModelCommandFactory
    for org.sa.rainbow.^model.^acme.AcmeModelInstance
    
    command load is org.sa.rainbow.^model.^acme.swim.commands.SwimLoadModelCommand
    command setDimmer(acme::SwimFam.LoadBalancerT target, int dimmer) 
        is org.sa.rainbow.^model.^acme.swim.commands.SetDimmerCmd;
    ...
}
```

Above is a fragment of a command factory specified in a RCL. The name **SWIM** specified 
the name that will be used to refer to the factory in other RCL configuration file. 
The class after **yields** specifies what Java class will be generated (by default in 
the _src/main/java-gen_ directory) and what superclass it will have (optional). It also 
specifies the class that implements the model that this factory is **for**.

Following the **extends** and **for** specifications, two optional standard operations 
are specified. The **load** command is used by Rainbow at start up to initialize the 
model. The class that is specified must extend _org.sa.rainbow.core.models.commands.AbstractLoadModelCmd_. 
It is also possible to specify an operation to save a model when Rainbow is finished 
(using the **save** keyword instead of **load**). This operation must extend _org.sa.rainbow.core.models.commands.AbstractSaveoModelCmd_.

After this comes the list of operations that the model can produce, e.g.:

```
    command setDimmer(acme::SwimFam.LoadBalancerT target, int dimmer) 
        is org.sa.rainbow.^model.^acme.swim.commands.SetDimmerCmd;
```

The first part is the name of the operation (which may be referred to by gauges and 
effectors), e.g., _setDimmer_. Following this are the parameters for the operation. 
The first argument may be a **target** which specifies the kind of model element that 
the operation is defined on. The following list of arguments specify the operation paramters. 
Operation argument types can refer to Acme types (by prefixing the type name with **acme::*, 
Java types, or builtin types like **int**, **String**, etc.). Finally, the operation 
specifies the implementing class for the operation.

## Probe Specifications

Probes are instruments in the running system that provide system-specific information 
to Rainbow. Such information is converted to model information by _gauges_. There are 
two types of probes supported by Rainbow: script based probes and Java probes. 
In both cases, the `probes.rbw` file needs to be updated with the new probe information. 
This file usually lives in the `system` directory of a target. See [Required Probe Fields](#required_probe_fields) 
for a list of all the properties for a probe.

### Probe Types

Probe types specify information about probes that instances inherit (and can change 
in the instance). For example, 

```
probe type GenericProbeT = {  
    location = ««customize.system.^target.lb»»
    script = {
        mode = "continual" 
        path = "«probes.commonPath»/genericProbe.pl"
    }
} 
```

This defines a probe type called `GenericProbeT`. Instances of this probe type will 
by default define a `location` and a `script` property. The `script` property will define 
`mode` as `continual` and `path`.

### Probe Instances

Probe instances may indicate the probe type they are instances of. Otherwise, probes 
are defined similarly to probe types. For example, 

```
probe DimmerProbe -> GenericProbeT = {
    alias = "dimmer"
    script = {
        argument = "get_dimmer"
    }
}
```

This states that the `DimmerProbe` instance will get all the properties defined in `GenericProbeT` 
in addition to the once defined in `GenericProbeT`. To define the same probe instance 
without the probe type, you would need to specify:

```
probe DimmerProbe = {
    location = ««customize.system.^target.lb»»
    alias = "dimmer"
    script = {
        mode = "continual" 
        path = "«probes.commonPath»/genericProbe.pl"
        argument = "get_dimmer"
    }
}

```

### Required Probe Fields

| Property | Description |
| :-------- | :---------- |
| `alias`              | The name that the probe as seen by the gauge. Gauges will put this in the `targetProbeType` field |
| `location`          | The location where the probe is deployed (usually a property specified in `properties.rbw`) |
| `java = {...}`      | Indicates the probe is implemented by Java class |
| &nbsp; &nbsp;`class`     | The class of the probe, implementing `org.sa.rainbow.translator.IProbe` or one of its subclasses. |
| &nbsp; &nbsp;`period`    | The reporting period of the probe (in ms)|
| &nbsp;&nbsp;`args` | The arguments that will be passed to the constructor of the probe, 
in an array |
| `script = {...}`             | Indicates the probe is implemented by a script |
| &nbsp; &nbsp; `path`         | The path of the script, which should exist on `location` |
| &nbsp; &nbsp; `argument`     | The argument(s) to pass to the script |

For Java-based probes, the java class should be on the classpath.

## Gauge Specification

While probes provide information about the system, the intent of gauges is to abstract 
this system information into model information, in the form of commands against the model 
in Rainbow. There are three kinds of gauges in the Rainbow system: gauges that receive 
information from probes; gauges that receive information from other gauges; and gauges 
that generate information without either of these (for example, time-based gauges or 
diagnostic gauges). 

In all cases, gauges must be specified in the `gauges.rbw` file located in the models 
directory. This file contains both gauge types and gauge instances. Instances in Rainbow should create, 
how they are attached to the model, and what (if any) probes they listen to. 
For example, a gauge type could be defined for reporting the processing time property 
for servers. The type would define what values (e.g., processing-time) are reported 
by the gauge, how to set up the gauge initially (e.g., the period of reporting), and 
how to configure the gauge when it is running (e.g., by changing the reporting units 
from seconds to milliseconds). The instance specification specifies where a 
particular gauge instance runs, what part of the model it is attached to, what probes 
it is listening to, etc.

Gauge specifications (types or instances) contain three main sections:

1. Command and Model Information. This section specifies the model/model factory that 
the gauge is associated with, and the commands that the gauge is intended to produce. 
Gauges must produce at least one kind of command.

2. Setup parameters. This sections defines the parameters that are used when constructing 
the gauge, and are not intended to change after the gauge is created. This typically 
includes information like the location of where the gauge is to be run, the class that 
implements it, how often it should send a beacon to the master.

3. Configuration Parameters: This section specifies attributes about the gauge that 
may be changed later on through configuring the gauge. This might include information 
like the probe being listened to, the sampling frequency, or any other information that 
you may want to configure the gauge with.

Gauges are only implemented using Java in the current framework. The `javaClass` specified 
in the type must be on the classpath for the target system. To implement the gauge, 
the following class hierarchy is provided by the Rainbow Infrastructure:

  - `AbstractGauge`: Implements basic Gauge management, such as heartbeat, 
     configuration, setting up, etc. Gauges that do not listen to any probes 
     should implement this class

  - `AbstractGaugeWithProbes extends AbstractGauge`: A gauge that handles the 
     targetProbeType and targetProbeList configuration parameters. 

  - `RegularExpressionGauge extends AbstractGaugeWithProbes:` A gauge that processes 
    probe reports that match one or more regular expressions. The constructor for the 
    gauge should specify the regular expression patterns that are to be matched, and 
    the gauge provides a default runAction that calls "doMatch" when a one of the 
    expressions is matched. Extenders must implement this doMatch method to report the 
    value.

### Gauge Types

Gauge types specify 

1.the model factory that can be used to derive commands (specified 
as either a RCL model factory imported into the file or as a Java class on the classpath). 
2. The list of commands that instances will produce. In the type, these are given a 
name that can be referred to in the gauge implementation, and a signature (which must 
match the signature of the operation in the factory), with the target before the operation 
name (if it exists). Consider the example:

```
gauge type DimmerGaugeT = {
    model factory ««SWIM»»
    command ^dimmer = LoadBalancerT.setDimmer(int)
    setup = {
        targetIP = "localhost"
        beaconPeriod = 30000
        javaClass = org.sa.rainbow.translator.swim.gauges.SimpleGauge
    }
    config = {
        samplingFrequency = 1500
    }
    comment = "DimmerGaugeT measures and reports the dimmer value of the system"    
}
```

This says that `DimmerGaugeT` is associated with the model factory `SWIM` and has one 
command called `dimmer`. Notice that the command signature matches that in the factory:

```
   command setDimmer(acme::SwimFam.LoadBalancerT target, int dimmer)
```

as the signature: `LoadBalancerT.setDimmer(int)`.

There are then sections for setup and config that define possible parameters. These 
are described in [Required Gauge Fields](#required_gauge_fileds)
 

### Gauge Instances

Like probes, gauge instances may refer to their type definitions. In this case, they 
must specify a particular model (that has the same factory as declared in the type), 
and list commands that are in the type. An error will be given if the instance does 
not specity all commands or the signatures do not match. Consider the gauge:

```
gauge DimmerG0 -> DimmerGaugeT = {
    model ««SwimSys»»
    command dimmer = LB0.setDimmer($<dimmer>)
    setup = {
        targetIP = ««customize.system.^target.lb»»
    }
    config = {
        targetProbe = ««DimmerProbe»»
    }
    comment = "DimmerG0 is associated with the component LB0 of the System, SwimSys, defined as an Acme model"
}
```

This gauge declares the type `DimmerGaugeT` and that it refers to the model instance 
`SwimSys`. Note that this model instance has the factory `SWIM`. 

The command part refers to the command `dimmer` and has the definition of `LB0.setDimmer($<dimmer>)`.
`LB0` is defined in SwimSys as a component that has the type `LoadBalancerT`. In this 
signature, real values can also be included (e.g., `LB0.setDimmer(1)` means the command 
always sends the value `1` for the dimmer value). If a parameter is preceded is surround 
by `$<...>` (as is the case here), this means that the gauge implementation is intended 
to fill in this value - the type of this kind of parameter is not checked against the signature 
until run time.


### Regular Expression Gauges

One of the most common form of gauge is a RegularPatternGauge, which looks for matches 
on probe output and uses regular expressions to match and pick up parts of the data. 
Regular pattern gauges are now supported in RCL in the gauge type specification, and 
RCL generate the Java class that implements this gauge. For example, consider that the 
dimmer gauge should only process natural numbers, and so we only want to match data coming 
from the probe that matches the pattern `[0-9]+`. We can do this by declaring in the 
gauge type (eliding out unchanged details):

```
gauge type DimmerGaugeT = {
    ...
    command ^dimmer = "(?<dimmer>[0-9]+)" -> LoadBalancerT.setDimmer(int)
    setup = {
        ...
        generatedClass = org.sa.rainbow.translator.swim.gauges.DimmerGauge
    }
    ...  
}
```

The command specification part of the gauge type now specifies a regular expression 
using named captured groups. It also specifies an attribute `generatedClass` that specifies 
the class to generated when the project is built. 

Gauges of this type follow these rules:

- The regular expression must have named groups
- Gauge instances of the type should refer to them in the command call by name
- Such a gauge has to have a `generatedClass` attribute instead of a `javaClass` attribute
- `generatedClass` must be a string
- `generatedClass` should not be the name of an existing class on the classpath


### Required Gauge Fields

The format of the gauge-type portion of the `gauges.rbw` spec are:

| YAML field | Description |
| :---------- | :---------- |
| `setup`                   | The parameters used when the gauge is constructed. It is possible to define your own setup parameters, giving values in the instance, but the required ones are: |
| &nbsp; &nbsp; `targetIP`               | Where a gauge instance will be run. Here the default is localhost. This 
should be a String|
| &nbsp; &nbsp; `beaconPeriod:`           | How often the gauge will send a report of its liveness to rainbow, in ms |
| &nbsp; &nbsp; `javaClass:`              | The javaClass that implements the gauge. | 
| `config`                  |  The parameters used to configure the gauge, with values given in the instance. Gauges are configured in Rainbow when all the expected target locations have been created. It is possible to define your own parameters, but Rainbow understands the ones below: |
| &nbsp; &nbsp; `targetProbe:`        | The probe _type_ that the gauge will listen to. |
| &nbsp; &nbsp; `samplingFrequency:`         | How often, in ms, the gauge will report a value. |


Gauge instances should give values only for those properties defined in the type.

## Gauge Implementation

Gauges are implemented as separate thread extending [`AbstractGauge`](../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/gauges/AbstractGauge.java), 
[`AbstractGaugeWithProbe`](../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/gauges/AbstractGauge.java)  
or the commonly [`RegularPatternGauge`](../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/gauges/RegualrPatternGauge.java).
In the first two cases the `runAction` method should be implemented. We will go through 
a set of simple implementations for each kind of gauge. 

Let's consider a gauge that reports the time to a model. The gauge has the following 
type specification:

```
gauge type TimeGaugeT = {
    model factory ««Time»»
    command setTime = ClockComponent.setTime(long)
    ...
}
``` 

In this first example, we can assume that the gauge just generates the time by using 
Java's `Date` without involving any probes, and we will report the time every 10 seconds. 

The aim of `runAction` is to work out a value and issue a command using the build in 
`issueCommand` method of the gauge (or `issueCommands` if the gauge wants to transactionally 
execute multiple models). The signatures for these methods are:

```java
  public void issueCommand (IRainbowOperation cmd, Map<String, String> parameters) ...
  public void issueCommands ((List<IRainbowOperation> operations, List<Map<String, String>> parameters) ...
```

Let's look at the implementation of this gauge:

```java
public class TimeGauge extends AbstractGauge {

    public TimeGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            Map<String, IRainbowOperation> mappings) throws RainbowException {
        super("TimeGauge", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    }
    
    // Stores the last reported time
    protected long lastReportedTime = -1;
    
    @Override
    protected void runAction() {
        super.runAction();
        long currentTime = new Date().getTime();
        // We don't block the thread so that the gauge can send 
        // beacons as appropriate. So check if it has been
        // 10 seconds or so before sending
        if (currentTime - lastReportedTime > 10000) {
            // Get the set time operation defined in the gauges.rbw
            IRainbowOperation command = getCommand("setTime");
            // Get the list of parameters that we need to fill in
            Map<String,String> parameters = getParameters(command);
            // Fill in the first parameter with with the time
            parameters.put(command.getParameters()[0], Long.toString(currentTime));
            // Issue the command to Rainbow
            issueCommand(command, parameters);
            // Remember that we recorded this time
            lastReportedTime = currentTime;
        }
    }

}

```

This method gets the operation named `setTime` as defined in the gauge definition and 
fills in the time parameter for that command (the target is fixed as `ClockComponent` 
by the specification). Within rainbow, all parameters and targets are passed as Strings, 
which is why we convert the time to a string with `Long.toString(currentTime)`.

Now, let us consider that we have a probe that just reports the time. To keep it simple, 
we can assume that the probe just prints the long value of time that is needed for the 
model. In this case we need to extend `AbstractGaugeWithProbe`. In this case, we need 
to implement two methods: `reportFromProbe` and `runAction`. Because probes report 
in a different thread to the gauge, you should store the probe value in a data structure 
that can be read by the gauge. You may choose to just keep the latest value, or keep 
all values of the probe to do some averaging or filtering.

```java
public class TimeGaugeWithProbe extends AbstractGaugeWithProbes {

    protected TimeGaugeWithProbe(String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            Map<String, IRainbowOperation> mappings) throws RainbowException {
        super("TimeGauge", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    }

    private long lastTimeFromProbe = -1;
    private long lastReportedTime = -1;
    
    @Override
    public void reportFromProbe(IProbeIdentifier probe, String data) {
        super.reportFromProbe(probe, data);
        synchronized (this) {
            try {
                lastTimeFromProbe = Long.valueOf(data);
            } catch (NumberFormatException e) {
                // Report an error or ignore
            }
        }
    }
    
    @Override
    protected void runAction() {
        super.runAction();
        long currentTime = -1;
        syncrhonized(this) {
            currentTime = lastTimeFromProbe;
        }
        // the rest of the code is the same as for TimeGauge
        ...
    }
    
}
```
In this case `reportFromProbe` places the probe reported value into a field of the gauge 
(after converting it to a long). And `runAction` then just retrieves this value and 
sees if it should be reported. Be careful with synchronization here because we are dealing 
with different threads accessing the probe value.

Now let us assume that the probe reports the time value as a string that we need to 
patch some value (e.g., ROS publishes the clock as an event where the milliseconds are 
buried in the event). In such a case, we can use a regular expression to pull out the 
right value. To write this kind of gauge we need to:

1. Extend `RegularPatternGauge`
2. Register the regular expression, usually in the constructor
3. Override the `doMatch` method

This is a common kind of gauge in Rainbow and so probe data is handled automatically 
in the super class.

```java
public class TimeGaugeRegexp extends RegularPatternGauge {

    protected static final String CLOCK = "Clock";
    protected static final String CLOCK_PATTERN = "topic: /clock .*secs: ([0-9]*)";

    public TimeGaugeRegexp(String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
            throws RainbowException {
        super("TimeGauge", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        // Register the pattern (It has one group that is a number)
        addPattern(CLOCK, Pattern.compile(CLOCK_PATTERN));
    }
    
    private long lastReportedTime = -1;

    @Override
    protected void doMatch(String matchName, Matcher m) {
        if (CLOCK.equals(matchName)) {
            long currentTime = Long.valueOf(m.group(1));
            // The rest is the same as the other gauges
        }
    }

}
```

Of course, such a class could be generated by using the regular pattern kind of gauge 
type and specifying:

```
   command setTime = "topic: /clock .*secs: (?<time>[0-9]*)" -> ClockT.setTime(long)
```

## Effector Specifications

Effectors are scripts or code that are run on the target system to make a change
to the system (e.g., to add a server or to blacklist a client). Effectors map 
commands that are issued by the executor (that are expressed as model commands) and 
call the code when the model commands are matched. Effectors are system-specific and 
so are usually defined in the `effectors.rbw` file in the `system` directory of the 
target. Effectors are associated with models, and are listen to model commands issused 
by the executor that matche some pattern.

### Effector Instances

Consider the example effector specification:

```
effector setDimmer = { 
    model ««SwimSys»»
    command ««customize.system.^target.lb»».setDimmer($<dimmer>)  
    location = ««customize.system.^target.lb»» 
    script = {
        path = "«effectors.commonPath»/setDimmer.sh" 
        argument = "{0}"
    }
}
```

It is associated with executor commands intended to act on `SwimSys`. It listens for commands 
on the component indicated by the property `customize.system.^target.lb` which is converted 
from the `LB0` component in SwimSys through a property specifying the location. If `setDimmer` 
with any value for the first argument is sent, then this effector will fire. It will 
execute the command `"«effectors.commonPath»/setDimmer.sh {0}"` on the location machine, 
replacing {0} in this case with the first argument of the command.


### Required Fields

All of the fields in the example above are required.

## Stitch and Utility Preferences

### Utility Specifications

#### Utility Model

#### Impact Models


## Installing

## Building Targets with Maven