# Analysis report for RainbowConfigurationChecker

## Functionalities

### check Gauge

Configuration

- type: setup parameter not defined -> ERROR
- instance - type: not defined -> ERROR
- instance - assotiated model: not defined OR name not defined OR type not defined OR instance not exists -> ERROR
- instance - command: not exists -> ERROR
- instance - command: invalid(not referenced by the gauge type) -> WARNING
- instance - referenced probe: not exists -> ERROR
- instance - java class: not defined OR not exists -> WARNING
- instance - java class: invalid constructor -> ERROR
- instance - setup parameter: not defined -> WARNING
- instance - config parameter: not defined -> WARNING
        
### check Probe

Configuration

- alias: not defined -> ERROR
- alias: not referred by any gauges -> WARNING
- location: not defined OR not valid(starts with '$')-> ERROR
- kind - JAVA class: not defined -> ERROR
- kind - JAVA class not exists -> WARNING
- kind - SCRIPT path: not defined -> ERROR

### check Effector

Configuration

- type: not defined OR not exists -> ERROR
- location: not defined -> ERROR
- command: not defined -> ERROR
- identifier kind - JAVA class: not defined -> ERROR
- identifier kind - JAVA class: not exists -> WARNING
- identifier kind - SCRIPT path: not defined -> ERROR               

## Future Improvements

### check Probe
1. add Probe Checking: "kind - SCRIPT path: not exists -> ERROR"
2. add Probe Checking: "kind - JAVA: has undefined arguments -> WARNING"
3. add Probe Checking: "kind - SCRIPT arguments: invalid -> ERROR"

### check Effector
2. add Effector Checking: "identifier kind - SCRIPT path: not exists -> ERROR"
3. add Effector Checking: "identifier kind - SCRIPT argument: invalid -> ERROR"
4. add Effector Checking: "command: invalid -> ERROR"

### ADD check Adaption Manager