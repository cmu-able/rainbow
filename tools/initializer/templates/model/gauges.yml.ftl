#
# Gauge Type and Gauge Instance Specifications
#  - time periods generally in milliseconds
gauge-types:
  [=gaugeTypeName]:
    commands:
      load :
    setupParams:
      targetIP:
        type:
        default:
      beaconPeriod:
        type:
        default:
      javaClass:
        type:
        default:
    configParams:
      samplingFrequency:
        type:
        default:
      targetProbeType:
        type:
        default:
    comment:
gauge-instances:
  [=gaugeInstanceName]:
    type:
    model:
    commands:
      load :
    setupValues:
      targetIP:
    configValues:
      targetProbeType  :
    comment:
unused-gauge-instances: