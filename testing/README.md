# Rainbow Testing Library 
This Testing Library provide a "mocking" library for performing unit test on rainbow component. 
Currently it only support Java type component. 

| Name         | Type   | Support | Description |
| ------------ | ------ | ------- | ----------- |
| **Gauge**    | Java   | Yes     |             |
|              | Java   | Yes     |             |
| **Probe**    | Script | No      |             |
| **Effector** | Java   | No      |             |
| **Command**  | Java   | Yes     |             |
| **Analyzer** | Java   | Yes     |             |

## Build and Install Testing Library locally

```
git clone https://github.com/cmu-able/rainbow.git
```
```
cd testing
```
```
mvn compile
mvn package
```
Check the built jar file under target
```
ls ./target
```
Install the testing library to your local repository
```
 mvn install:install-file -Dfile=<path-to-your-rainbow>/testing/target/rainbow-testing-1.0.0-SNAPSHOT.jar -DpomFile=<path-to-your-rainbow>/testing/pom.xml
```

## Write unit test with Testing Library
1. Go to your project's deployment directory. 
2. Creat the test case for the component you want to test
3. You can follow the folloing manual to create the unit test. 

| [How to write an Unit Test for Gauge]()       |
| --------------------------------------------- |
| **[How to write an Unit Test for Probe]()**   |
| **[How to write an Unit Test for Command]()** |







