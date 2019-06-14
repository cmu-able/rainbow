# initializer usage

## Maven compile
```
mvn package
```
## Run the initializer 
### To see the help menu:
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
