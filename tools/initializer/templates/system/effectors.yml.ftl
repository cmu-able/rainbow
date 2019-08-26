# Effectors for the project
vars:
    _effectors.commonPath: "${rainbow.path}/system/effectors"

effectors:
[#if effectors??]
[#list effectors as one_effector]
    [=one_effector]:
        location:
        command:
        type:
        scriptInfo:
            path    :
            argument:
[/#list]
[/#if]

unused-effectors:
