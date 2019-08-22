model/gauges.yml: model/gauges.yml.ftl
model/[=project_name].[=model_file_extension]: model/example_project.acme.ftl
model/[=project_name].mtd: model/example_project.mtd.ftl
model/tsp.yml: model/tsp.yml.ftl
model/opera/kalmanFilter.dtd: model/opera/kalmanFilter.dtd.ftl
model/opera/brownout.kalman.config: model/opera/brownout.kalman.config.ftl
model/opera/brownout.model.pxl: model/opera/brownout.model.pxl.ftl
model/opera/opera.config: model/opera/opera.config.ftl
stitch/[=project_name]Strategies.s: stitch/example_projectStrategies.s.ftl
stitch/[=project_name]Tactics.t.s: stitch/example_projectTactics.t.s.ftl
stitch/utilities.yml: stitch/utilities.yml.ftl
system/effectors.yml: system/effectors.yml.ftl
system/probes.yml: system/probes.yml.ftl

[#if effectors??]
[#list effectors as effector]
system/effectors/[=effector].sh: system/effectors/defaultEffector.sh.ftl
[/#list]
[/#if]

[#if probes??]
[#list probes as probe]
system/probes/[=probe].sh: system/probes/defaultProbe.sh.ftl
[/#list]
[/#if]

system/util/cmdhelper.sh: system/util/cmdhelper.sh.ftl
rainbow.properties: rainbow.properties.ftl
