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
For exam[ple, the file [properties.rbw](../deployments/rainbow-example/src/main/resources/rbw/rainbow-example/properties.rbw)
 
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

Properties may be referenced inside strings and on their own. To reference a property in a string, surrond it with `Â«...Â»`, e.g.,

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
def logging.path = "Â«event.log.pathÂ»/rainbow.out" 
def monitoring.log.path = "Â«event.log.pathÂ»/rainbow-data.log"
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
(org.sa.rainbow.core.models.commands.IRainbowModelOperation)[../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/models/commands/IRainbowOperation.java]. 
In practice, there are two possible classes that you will extend. 

1. If you are dealing with Acme models, you will extend 
   (org.sa.rainbow.model.acme.AcmeModelOpetation)[../rainbow/rainbow-acme-model/src/main/java/org/sa/rainbow/model/acme/AcmeModelOperation.java], 
   which is a generic class where you specify the result of the operation. You will 
   need to specify doConstructCommand, which returns the Acme command (IAcmeCommand) 
   that will operate on the Acme model.

2. If you are defining your own model class, you will extend 
   (org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation)[../rainbow/rainbow-core/src/main/java/org/sa/rainbow/core/models/commands/AbstractRainbowModelOperation.java].
   This is a generic class that takes two parameters: the class of the model object 
   managed by Rainbow and the type of the result of the operation. In most cases, you will need to specify methods for executing, redoing, and undoing 
the operations, as well as a method to ensure that the operation is valid on the model. 

For an example of an Acme operation, look at (org.sa.rainbow.model.acme.swim.commands.ActivateServerCmd)[]. 
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

### Probe Types

### Probe Instances

### Required Fields

## Gauge Specification

### Gauge Types

### Gauge Instances

### Regular Expression Gauges

### Required Fields

## Effector Specifications

### Effector Instances

### Required Fields

## Stitch and Utility Preferences

### Utility Specifications

#### Utility Model

#### Impact Models


## Installing

## Building Targets with Maven