## initializer usage

## metadata.yml
Include your self-defined variables and template files in the following format:

```
variables:
	- name: your_variable_name
  	  description: write your descriptions of the variable
  	  value: the default value of the variable
```

```
files:
	- your_initialization_file_name
```

## To run an initializer
1.Compile using maven
```
mvn package
```
2.Using the jar file to run the initializer
3.-t for template and -c for config