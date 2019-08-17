
# Command Line Options

### `--help`
Lists all command line arguments

### `--prism-template=value`
Give the path to the prism template. Defaults to `../dartam/model/dart2`

## Adaptation Manager Settings

### `--lookahead-horizon=value`
Set the decision horizon

### `--non-latency-aware`
Makes the adaptation manager ignore the latency of its adaptations.
(It assumes all actions are instantaneous)

### `--probability-bound=value`
Set the minimum survival probability that the SDPRA adaptation manager will try
to achieve.

### `--stay-alive-reward=value`
Give a utility for each step that the drones remain alive.

### `--adapt-mgr=value`
Choose the adaptation manager that you want to use. Currently supports:
*   pmc
*   sdp

### `--change-alt-latency=value`
Adjusts the latency of changing altitude. Given in *seconds* where the decision
period is 60 seconds. This defaults to 60 (a single period).

## Environment Settings

### `--map-size=value`
Set the horizontal width of the map

### `--square-map`
Creates an additional dimension for the drones to travel on. The drones cover
the map using a lawn-mover pattern.

### `--num-targets=value`
Set the number of targets generated

### `--num-threats=value`
Set the number of threats generated

### `--altitude-levels=value`
Set the number of altitude levels that the drones can use.

### `--threat-range=value`
Set the range of the threats in altitude levels. Use `--auto-range` to
automatically adjust this to the number of altitude levels.

### `--threat-sensor-fpr=value`, `--threat-sensor-fnr=value`
Set the false positive and false negative rates (respectively) for the threat sensor.

### `--target-sensor-fpr=value`,`--target-sensor-fnr=value`
Set the false positive and false negative rates (respectively) for the target sensor.

### `--dl-target-sensor-range=value`
Set the range from which targets can be detected on the ground.
Use `--auto-range` to automatically adjust this to the number of altitude levels.

### `--auto-range`
Automatically sets the target and threat sensors to 100% and 75% of the number
of altitude levels respectively.

### `--seed=value`
Set the random seed that all random behavior deterministically stems from.

## Tactics

### `--ecm`
Add the ECM (Electronic Countermeasures) tactic. When used, it reduces the the
chance of both target detection and destruction by threats

### `--two-level-tactics`
Allow the adaptation manager to move 2 altitude levels in a single period of
 the altitude latency

### `--no-formation`
Remove the option for loose and tight formation tactics

## SDP Related Options

### `--reach-path=value`
### `--reach-model=value`
### `--distrib-approx=value`
### `--opt-test`
Run an optimality test if the adaptation manager supports it. Generates a
single plan at the beginning and runs it throughout the simulation.
Only SDP supports this option.
