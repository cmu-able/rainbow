# initializer usage

## To run an initializer
1.Create a metadata.yml in your work directory to include all variables and files that you want to generate
  
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
2.Compile using maven
```
mvn package
```
3.Use the jar file to run the initializer 
- To see the help menu:
```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -h
```
- To load a template:
```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -t <path_or_uri_to_template> 
```
- To load a configuration:
```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -c <path_or_uri_to_config> 
```