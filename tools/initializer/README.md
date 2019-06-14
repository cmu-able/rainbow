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
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar
```
(We have already included default initialization for you in ./templates, check metadata.yml for more details.)

### To custormize initialization

Use -t To load templates and -c to load configurations:
```
java -jar target/rainbow-initializer-1.0-SNAPSHOT-shaded.jar -t <path_to_template> -c <path_to_config> 
```
