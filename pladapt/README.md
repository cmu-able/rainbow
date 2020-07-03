# PLADAPT: Proactive Latency-Aware Adaptation Manager

This is a library that implements proactive latency-aware decision-making for self-adaptation. It includes the following decision-making approaches:

- PLA-PMC: class `PMCAdaptationManager`
- PLA-SDP: class `SDPAdaptationManager` and class `SDPRAAdaptationManager`

These approaches are described in the following publications:

[1] Gabriel A. Moreno, Javier Camara, David Garlan and Bradley Schmerl. "Proactive Self-Adaptation under Uncertainty: A Probabilistic Model Checking Approach." _Proceedings of the Joint Meeting of the European Software Engineering Conference and the Symposium on Foundations of Software Engineering (ESEC/FSE)_ (2015) [[link]](http://works.bepress.com/gabriel_moreno/25/)

[2] Gabriel A. Moreno, Javier Camara, David Garlan and Bradley Schmerl. "Efficient Decision-Making under Uncertainty for Proactive Self-Adaptation." _Proc. of the International Conference on Autonomic Computing_ (2016) [[link]](http://works.bepress.com/gabriel_moreno/28/)

[3] Gabriel A. Moreno. _Adaptation Timing in Self-Adaptive Systems_. PhD Thesis, Carnegie Mellon University (2017) [[link]](http://works.bepress.com/gabriel_moreno/31/)

The library does not implement a complete adaptation manager. There are different pieces that have to be customized to implement a complete adaptation manager. The elements to be customized are:

- models: the models are described in the publications listed above.
  + PLA-SDP: Alloy models (see `reach/model`, and [2,3])
  + PLA-PMC: PRISM models (see `examples/dart/dartam/model` and [1,3])
- configuration state: extending the class `Configuration`
- configuration space: extending the class `ConfigurationManager`
- environment state: extending the class `Environment`
- environment model: extending the class `EnvironmentDTMCPartitioned`
- utility function: extending the class `UtilityFunction`

Instead of extending these classes it is also possible to use the generic classes `Generic*`, although `GenericUtilityFunction` must be extended to override the methods `getGenAdditiveUtility()`, `getGenMultiplicativeUtility()`, and `getGenFinalReward()`. 


## Building the Library
The library has only been tested on Ubuntu 16.04.

In the following examples `$PLADAPT` refers to the top-level directory of this distribution, where this README is.

### Dependencies
To install the main dependencies for compiling the library use this command:

```
sudo apt install libboost-all-dev libyaml-cpp-dev make automake autoconf g++ default-jdk ant wget
```

#### Additional Dependencies
In order to use PLA-PMC (`PMCAdaptationManager`) it is necessary to have the PRISM model checker installed and in the execution path.
PRISM can be downloaded from its [website](http://www.prismmodelchecker.org).
This has been tested with PRISM version 4.3.1.

To use PLA-SDP, it is necessary to compile the tool that generates the reachability functions from the Alloy models.
The following instructions will download the necessary dependencies and build the program.
```
cd $PLADAPT
reach/build.sh
```

### Compiling the library
```
cd $PLADAPT
autoreconf -i
mkdir build; cd build
../configure
make
```

### Compiling the Java wrapper
This is only needed to use the library from Rainbow (or other Java-based self-adaptation frameworks).
In addition to the previous dependencies, [SWIG](http://www.swig.org/) version 3.0.12 must be installed.


After compiling the library do:
```
cd $PLADAPT/java
make
```

To install the Java wrapper in the local Maven repository, also do `make mvn-install`

## Example
An example that uses the library is included.
This example, called DART, simulates a team of drones that have to detect targets on the ground while avoiding threats.
The goal of the mission is to detect at least half of the targets without being destroyed.
DART is described in [3].

The example is divided into two projects located in the `examples/dart` directory
- `dartam` is the adaptation manager
- `dartsim` is the simulator that uses the adaptation manager to decide how the drones are going to adapt to the uncertain environment

### Building the example
First, build the adaptation manager.

```
cd $PLADAPT/examples/dart/dartam
autoreconf -i
mkdir build; cd build
../configure
make
```

Then, build the simulation

```
cd $PLADAPT/examples/dart/dartsim
make
```

### Running the example
There are several options to run the simulation and configure the adaptation manager.
Running `./run.sh --help` will list them.

```
./run.sh --seed 1234 --adapt-mgr sdp --stay-alive-reward=0.5 
```

The output will log the tactics executed at different times.
The end of the output looks similar to this:

```
Total targets detected: 2
#                                       
 # #    ##   ## #   # # # ## # # # ## *#
  * *  #  * #  # # # * # #  # # # #  *  
     **    #      *                     
 ^      ^       ^   ^ ^    ^            
       T   X     X          T           
out:destroyed=0
out:targetsDetected=2
out:missionSuccess=1
csv,2,0,39,1,4.26153,18.3524
```
The part with all the symbols is a 2D side view of the route of the team of drones.
The two-bottom lines represent the ground, and the lines before them represent the altitude level of the drones at the different positions in the route. The symbols have the following meaning.

Symbol | Meaning
-------|-------------------------
\#      | loose formation
\*      | tight formation
@      | loose formation, ECM on
0      | tight formation, ECM on
^      | threat
T      | target (not detected)
X      | target (detected)

The last line has a summary of the results in csv format:
```
csv, targets detected, team destroyed, last team position, mission success, decision time avg, decision time variance
```

To run the same example with PLA-SDP extended with probabilistic requirements, we can specify the required probability of surviving the mission, instead of having to select a reward for staying alive.
```
./run.sh --seed 1234 --adapt-mgr sdpra --probability-bound 0.95
```
