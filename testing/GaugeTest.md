



## How to write an Unit Test for Gauge

### Step I Test preparation: Initialize the mocked Rainbow, the mocked PortFactory and the GaugeDescription clase

Here is an example for setting a gauge test. This works for **all gauges**. For different gauges, you need to specify their different configuration yaml file.  Your should put the configuration file under the folder <path-to-your-testing>/resource/ 

```java
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
    File yamlFile = extractResource("/ServerEnablementGaugeTest/gauges.yml");
    GaugeDescription gdl = YamlUtil.loadGaugeSpecs(yamlFile);
    gd = gdl.instDescList().get(0);
}

```

### Step II Write test case for the *Gauge*

1. Now your create the unit test 

   ```java
   @test
   public void goodPath() throws Exception {
   }
   ```

   

2. In your test method, you can new a gauge instance using the GaugeDescription loaded from the yaml file. 

   For example:

   ````java
   Map<String, IRainbowOperation> mappings =  new HashMap<> ();
   mappings.putAll(gd.mappings());
   BlackholeGauge gauge = new BlackholeGauge(
     gd.gaugeName(), 10000L, new TypedAttribute(gd.gaugeName(), gd.gaugeType()),
     gd.modelDesc(), gd.setupParams(),mappings);
   ````

   The GaugeInstanceDescription class has the following parameters you can get:

   | parameter name  | Description                                       |
   | --------------- | ------------------------------------------------- |
   | gaugeName()     | The name fo the gauge.                            |
   | instanceComment |                                                   |
   | mappings()      | map from the operation name to its actual command |
   | beacon()        |                                                   |
   | modelDesc()     |                                                   |
   | setupParams()   |                                                   |

   However, for some gauges that needs to work with a Acme model, you need to set up these parameters on your own. Here is an example on how to set a private parameter for a gauge out side its constructor:

   ```java
   StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString ("<path-to-your-acme-model>/znn.acme");
   IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
   assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
   AcmeModelInstance mi = new BareAcmeModelInstance (sys);
   /*You can use Whitebox in Mockito to do this*/
   Whitebox.setInternalState(gauge, "m_model", mi);
   ```

   You stub the Acme model like this

   ```java
   
   private class BareAcmeModelInstance extends AcmeModelInstance {
       public BareAcmeModelInstance(IAcmeSystem sys) {
       super(sys, "");
       }
       @Override
       public AcmeModelCommandFactory getCommandFactory () {
       return null;
       }
   
       @Override
       protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
       return new BareAcmeModelInstance (sys);
       }
   }
   ```

3. Then you need to initialze the reporting port of gauge using the **initialze** method defined in **AbstractRainbowRunnable** class and pass the stubbed **IRainbowReportingPort** (the **LoggerRainbowReportingPort**) to it. 

   ```java
   gauge.initialize(new LoggerRainbowReportingPort());
   
   ```

4. Start the gauge

   ```java
   gauge.start()
   ```

5. Now you can creat your test case. Our testing library can only support the gauge to read input from a java gauge now. Here is an example for Blackhole gauge:

   ```java
   gauge.reportFromProbe(mockProbeIdentifier("mocked", "testing", "testing"), "1.1.1.1");
   ```

6. Read output of the gauge from the Utils the testing library provide **GaugeTestingUtil.waitForNextOperation()**. This util is blocked. So you will get all the operation commands at one time from the input of probe 

   ```
   IRainbowOperation operation = GaugeTestingUtil.waitForNextOperation();
   ```

7. You can now compare the output with your ideal output

   ```java
    assertEquals("setBlackholed", operation.getName());
    assertEquals("1.1.1.1", operation.getParameters()[0]);
   ```

### Here is how a complete unit test for gauge would look like:

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
        BlackholeGauge gauge = new BlackholeGauge(
                gd.gaugeName(), 10000L, new TypedAttribute(gd.gaugeName(), gd.gaugeType()),
                gd.modelDesc(), gd.setupParams(),mappings
        );
        /*initialize the logging port*/
        gauge.initialize(new LoggerRainbowReportingPort());
        /*Start the gauge*/
        gauge.start();
      
        gauge.reportFromProbe(mockProbeIdentifier("mocked", "testing", "testing"), "1.1.1.1");
        /*Use the GaugeTestingUtil we provide to wait for an gauge output*/
        IRainbowOperation operation = GaugeTestingUtil.waitForNextOperation();
        assertEquals("setBlackholed", operation.getName());
        assertEquals("1.1.1.1", operation.getParameters()[0]);
    }
}
```

