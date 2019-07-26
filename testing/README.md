# Rainbow Testing Library 
This Testing Library provide a "mocking" library for performing unit test on rainbow component. 
Currently it only support Java type component. For example, you can't test a script probe with this testing library. 

| Name     | Type   | Support | Description                                   |
| -------- | ------ | ------- | --------------------------------------------- |
| Gauge    | Java   | Yes     | Problem with some gauges using an  Acme Model |
| Probe    | Java   | Yes     |                                               |
| Probe    | Script | No      |                                               |
| Effector | Java   | Yes     |                                               |

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
1. Go to your project's deployment directory

2. Go to the Java file you want to test

3. In IntelliJ, On the class name you would test, click`⌥⏎ `, select **Create Test**

   <img width="1042" alt="https://user-images.githubusercontent.com/4093814/61955253-944d0780-af88-11e9-851d-5517d415e0ed.png">

4. Under the src/test, you will see the generated test class. 

5. Now you can write the unit test. Here is a example on write a unit test for gauge:

   ```java
   public class BlackholeGaugeTest {
   
       private GaugeInstanceDescription gd;
       @Before
     	/*First prepare the mocked rainbow and prorfFactory and configuration for the gauge */
       public void setUp() throws Exception {
         	/*Initialze the RainbowMocker we provide to mock the rainbow instance*/
           RainbowMocker.injectRainbow();
         	/*Initialze the mocking portFactory we provide*/
           IRainbowConnectionPortFactory mockedPortFactory = mockConnectionPortFactory();
         	/*User method under the GaugeTestingUtil to subscribe the output port for gauge*/
           stubPortFactoryForGauge(mockedPortFactory);
         	/*Prepare the output port for gauge in mocked Rainbow*/
           RainbowMocker.injectPortFactory(mockedPortFactory);
   				/*Use the helper util "extractResource" we provide to load the yaml file*/
           File yamlFile = extractResource("/BlackholeGaugeTest/gauges.yml");
           GaugeDescription gdl = YamlUtil.loadGaugeSpecs(yamlFile);
           gd = gdl.instDescList().get(0);
       }
   
       @Test
     	/*Write test case for this gauge*/
       public void goodPath() throws Exception {
         	/*Initialize the gauge just like you did under normal situation*/
           Map<String, IRainbowOperation> mappings =  new HashMap<> ();
           mappings.putAll(gd.mappings());
           RegularPatternGauge gauge = new BlackholeGauge(
                   gd.gaugeName(), 10000L, new TypedAttribute(gd.gaugeName(), gd.gaugeType()),
                   gd.modelDesc(), gd.setupParams(),mappings
           );
         	/*initialize the logging port*/
           gauge.initialize(new LoggerRainbowReportingPort());
         	/*Start the gauge*/
           gauge.start();
         	/*Read input from a Probe, use the mockProbeIdentifier we provide to initialze a mocking probe*/
           gauge.reportFromProbe(mockProbeIdentifier("mocked", "testing", "testing"), "1.1.1.1");
         	/*Use the GaugeTestingUtil we provide to wait for an gauge output*/
           IRainbowOperation operation = GaugeTestingUtil.waitForNextOperation();
           assertEquals("setBlackholed", operation.getName());
           assertEquals("1.1.1.1", operation.getParameters()[0]);
       }
   }
   ```

   

   

