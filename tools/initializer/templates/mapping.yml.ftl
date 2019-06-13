files:
  - model/gauges.yml: model/gauges.yml
  - model/[=project_name].[=model_file_extension]: model/example_project.acme
  - model/[=project_name].mtd: model/example_project.mtd
  - model/tsp.yml: model/tsp.yml
  - model/opera/kalmanFilter.dtd: model/opera/kalmanFilter.dtd
  - model/opera/brownout.kalman.config: model/opera/brownout.kalman.config
  - model/opera/brownout.model.pxl: model/opera/brownout.model.pxl
  - model/opera/opera.config: model/opera/opera.config
  - stitch/[=project_name]Strategies.s: stitch/example_projectStrategies.s
  - stitch/[=project_name]Tactics.t.s: stitch/example_projectTactics.t.s
  - stitch/utilities.yml: stitch/utilities.yml
  - system/effectors.yml: system/effectors.yml
  - system/probes.yml: system/probes.yml

  [#list effectors as effector]
    - system/effectors/[=effector].sh: system/effectors/defaultEffector.sh
  [/#list]

  [#list probes as probe]
    - system/probes/[=probe].sh: system/probes/defaultProbe.sh
  [/#list]

  - system/util/cmdhelper.sh: system/util/cmdhelper.sh
  - rainbow.properties: rainbow.properties
