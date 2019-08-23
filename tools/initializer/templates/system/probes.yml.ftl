vars:
    _probes.commonPath: "${rainbow.path}/system/probes"

probes:
[#if probes??]
[#list probes as one_probe]
    [=one_probe]:
        alias:
        location:
        type:
        scriptInfo:
        mode:
            path:
            argument:
[/#list]
[/#if]

unused-probes: