Commands for both acme model update (rainbow side) and effector (swim) are both inside model/acme/swim/commands
model/acme/swim/SwimModelHelper.java implements a helper class that queries the acme model. Some calculations might need to be changed, such as dimmer leve, expected activation time, etc.
model/acme/swim/SwimCommandFactory.java defines how commands are constructed, server activation commands needs to be changed (for rainbow side) that directly adds to a server type.
AddServerWithTypeCmd, Dec(Inc)DimmerCmd, DivertTrafficCmd, RemoveServerWithTypeCmd are the ones used for swim effectors, the rest are for acme models.

swim/adaptation/ folder has classes that define the hybrid planning algorithm
AdaptationManagerDummy.java is a dummy manager that only increases and decreases server every period without doing planning
HPAdaptationManager.java is the implemented planning class. Important functions include:
initializeAdaptationMgr: executed at init, can put test code here for testing that is executed once at the start
computeDecisionHorizon: the way horizon calculation might needs to be modified, which is done here
EnvironmentDTMCPartitioned: defines the parameters that generates the DTMC
get_initial_state_str: some minor details regarding initial state string
checkAdaptationImpl: the function that implements the HP algorithm, which uses the two classes Reactive and Deliberative, which can be called on separate threads. 
runAction: the function that gets called each period. calls checkAdaptationImpl

SwimExtendedPlan: defines a plan class that gets extended
The plan classes are in this folder as well



# PLA Adaptation Manager for Rainbow
This adaptation manager for Rainbow has two variants of Proactive Latency-Aware (PLA) adaptation managers: PLA-SDP, PLA-SB, described in

Gabriel A. Moreno. _Adaptation Timing in Self-Adaptive Systems_. PhD Thesis, Carnegie Mellon University (2017) [[link]](http://works.bepress.com/gabriel_moreno/30/)

It is intended to be used with SWIM as the target system, which is described in

Gabriel A. Moreno, Bradley Schmerl and David Garlan. "SWIM: an exemplar for evaluation and comparison of self-adaptation approaches for web applications" _Proceedings of the 13th International Conference on Software Engineering for Adaptive and Self-Managing Systems_ (2018) [[link]](http://works.bepress.com/gabriel_moreno/35/)

SWIM is available [here](https://github.com/cps-sei/swim)

You can use this in two ways
1. Build as described below
2. Run the demo as a Docker image, as described [here](docker/INSTRUCTIONS.md)

## Building the Adaptation Manager for SWIM
The following instructions have been tested in Ubuntu 16.04 LTS.

### Installing Dependencies
To install the required packages execute this command:

```
sudo apt update && sudo apt install --no-install-recommends maven libboost-all-dev libyaml-cpp-dev make automake autoconf g++ default-jdk ant wget libpcre3-dev socat
```

The main dependency for this adaptation manager is the [PLADAT library](https://github.com/cps-sei/pladapt). Follow the instructions included with the library for compiling it, and make sure to compile and install the Java wrapper.

When using the PLA-SB variant of this adaptation manager, it is necessary to have the PRISM model checker installed and in the execution path. PRISM can be downloaded from its [website](http://www.prismmodelchecker.org/). This has been tested with PRISM version 4.3.1 and PRISM 4.4.

Additional required third-party libraries must be installed executing this script

```
deployments/rainbow-swim/deps/installdeps.sh
```

### Building Rainbow and the PLA Adaptation Manager
Rainbow and the adaptation manager can be built with the following command:

```
./build.sh -s -d rainbow-swim -t swim
```

After running this command, Rainbow with the adaptation manager and the files necessary to use SWIM as a target system can be found in `Rainbow-build`

## Running Rainbow with SWIM
The Rainbow adaptation manager for SWIM has a model of the target system simulated by SWIM.
This model includes some parameters that have to match the corresponding parameters in the simulation.
The values of the following properties in the model `Rainbow-build/targets/swim/model/swim.acme` have to match those in `swim/simulations/swim/swim.ini`. The following table shows the names of the corresponding properties in the two files. (*Note:* these properties already match in the default configurations)

| swim.acme              | swim.ini                     |
| ---------------------- | ---------------------------- |
| DIMMER\_LEVELS         | \*.numberOfBrownoutLevels    |
| DIMMER\_MARGIN         | \*.dimmerMargin              |
| THREADS\_PER\_SERVER   | \*\*.server\*.server.threads |
| MAX\_ARRIVAL\_CAPACITY | \*.maxServiceRate            |
| RT\_THRESHOLD          | \*.responseTimeThreshold     |


It is important to select the appropriate run number when running SWIM so that the latency to add a server matches in the adaptation manager and in SWIM. SWIM includes two traces of user requests. Each row in the following table shows the run number to be used for a particular latency for the addition of a server with each trace.

| trace\latency |   0 |  60 | 120 | 180 | 240 |
| ------------- | --- | --- | --- | --- | --- |        
| WorldCup      |   0 |   1 |   2 |   3 |   4 |
| ClarkNet      |   5 |   6 |   7 |   8 |   9 |

In `Rainbow-build/targets/swim/model/swim.acme`, the latency to add a server is set in the property `ADD_SERVER_LATENCY_SEC`

SWIM has to be run following its [instructions to run it with an another external adaptation manager](https://github.com/cps-sei/swim#how-to-run-simulation-with-another-external-adaptation-manager). For example, to run SWIM with the WorldCup trace and a latency for the addition of a server of 120 seconds, we have to execute the following command in `swim/simulations/swim`:

```
./run.sh sim 2
```
After SWIM is started, Rainbow can be started with the following commands in another terminal, assuming that Rainbow is installed in the directory specified in the environment variable `RAINBOW`.

```
cd $RAINBOW/Rainbow-build
./run-oracle.sh -p rainbow.properties swim
```
Once the Rainbow graphical user interface opens, select `Delegate|Start Probes`.

After the simulation run of SWIM finishes, Rainbow can be closed, and the results of the simulation can be plotted or processed following the instructions provided with SWIM.

The previous command ran Rainbow with the PLA-SDP adaptation manager. It is also possible to run Rainbow with the PLA-SB adaptation manager using the following command. *Note:* this requires [PRISM](http://www.prismmodelchecker.org/) installed and its `bin` directory in the PATH environment variable.

```
cd $RAINBOW/Rainbow-build
./run-oracle.sh -p rainbow-sb.properties swim
```

