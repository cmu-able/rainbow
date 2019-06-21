# initializer usage

## Maven compile

```
mvn package
```

## Run the initializer

### To see the help menu

```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -h
```

### To use the default initialization

```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -t <path_to_templates_folder>
```

(If you would like to use the default option, <path_to_templates_folder> should point to
the /rainbow/tools/initializer/templates folder in your machine)

### To custormize initialization

Use -c to load configurations:

```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -t <path_to_templates_folder> -c <path_to_configuration_file>
```

### To custormize your target directory path:

use -p to load destination directory of which the new target would locate:

```
-p <path_to_target_destination>

```

## Template syntax

- Variable declaration

```
[=variable_name]
```

- If clause

```
[#if condition]
    // execution block
[/#if]
```

- Loop clause

```
[#list sequence as loopVariable]
  // repeatThis
[/#list]
```

For more details, please check https://freemarker.apache.org.

# Developer Guide

## Variable usage

To use a variable, you need to first declare it in templates/metadata.yml. 
Then you can configure its value in your_config.yml.

To add a variable in templates/metadata.yml, specify the variable name, 
description and default value in the following format:

```
  - name: project_name
    description: name of the project
    value: example_project
```

## Template file usage
To add an template file, you need to first declare it in templates/metadata.yml.
Then you can add your template file in templates/ folder.

To add a template in templates/metadata.yml, specify the file name(path) in the following
format:

```
- model/gauges.yml.ftl
```

In addition, specify which file should be loaded from which template in mapping.yml.ftl.